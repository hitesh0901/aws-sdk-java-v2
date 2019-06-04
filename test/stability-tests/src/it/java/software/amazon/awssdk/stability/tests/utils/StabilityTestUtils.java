/*
 * Copyright 2010-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.stability.tests.utils;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntFunction;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.stability.tests.ExceptionCounter;
import software.amazon.awssdk.stability.tests.TestResult;
import software.amazon.awssdk.utils.Logger;

public final class StabilityTestUtils {

    private static final Logger LOGGER = Logger.loggerFor(StabilityTestUtils.class);

    private StabilityTestUtils() {
    }

    /**
     * Process the result. Throws exception if SdkClientExceptions were thrown or 5% requests failed of
     * SdkServiceException or IOException.
     *
     * @param testResult the result to process.
     */
    public static void processResult(TestResult testResult) {
        LOGGER.info(() -> "TestResult: " + testResult);

        int clientExceptionCount = testResult.clientExceptionCount();

        int expectedExceptionCount = testResult.ioExceptionCount() + testResult.serviceExceptionCount();

        double ratio = expectedExceptionCount / (double) testResult.totalRequestCount();
        if (clientExceptionCount > 0) {
            throw new RuntimeException("SdkClientExceptions were thrown, failing the tests");
        }

        if (ratio > 0.05) {
            throw new RuntimeException("More than 5% requests failed of SdkServiceException or IOException, failing the tests");
        }
    }

    /**
     * Handle the exceptions of executing the futures.
     *
     * @param future the future to be executed
     * @param exceptionCounter the exception counter
     * @return the completable future
     */
    public static CompletableFuture<?> handleException(CompletableFuture<?> future, ExceptionCounter exceptionCounter) {

        return future.handle((r, t) -> {
            if (t != null) {
                if (t instanceof SdkServiceException) {
                    exceptionCounter.addServiceException();
                } else if (t instanceof IOException) {
                    exceptionCounter.addIoException();
                } else if (t instanceof SdkClientException) {
                    exceptionCounter.addClientException();
                } else {
                    LOGGER.error(() -> "Unknown exception was thrown ", t);
                    exceptionCounter.addUnknownException();
                }
            }
            return r;
        });
    }


    /**
     * Helper method to run the async tests and process the results.
     *
     * @param futureFactory the function to provide the CompletableFuture
     * @param totalRequestNumber the total number of the request
     * @param testName the name of the tests
     */
    public static void runAsyncTests(IntFunction<CompletableFuture<?>> futureFactory, int totalRequestNumber, String testName) {
        ExceptionCounter exceptionCounter = new ExceptionCounter();
        CompletableFuture[] completableFutures = new CompletableFuture[totalRequestNumber];

        for (int i = 0; i < totalRequestNumber; i++) {
            CompletableFuture<?> future = futureFactory.apply(i);
            completableFutures[i] = handleException(future, exceptionCounter);
        }

        CompletableFuture.allOf(completableFutures).join();

        TestResult result = TestResult.builder()
                                      .testName(testName)
                                      .clientExceptionCount(exceptionCounter.clientExceptionCount())
                                      .serviceExceptionCount(exceptionCounter.serviceExceptionCount())
                                      .ioExceptionCount(exceptionCounter.ioExceptionCount())
                                      .totalRequestCount(totalRequestNumber)
                                      .unknownExceptionCount(exceptionCounter.unknownExceptionCount())
                                      .build();

        processResult(result);
    }
}
