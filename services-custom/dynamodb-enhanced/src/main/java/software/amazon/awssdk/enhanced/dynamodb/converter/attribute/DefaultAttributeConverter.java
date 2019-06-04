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

package software.amazon.awssdk.enhanced.dynamodb.converter.attribute;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import software.amazon.awssdk.annotations.Immutable;
import software.amazon.awssdk.annotations.SdkPublicApi;
import software.amazon.awssdk.annotations.ThreadSafe;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.internal.converter.AttributeConverterChain;
import software.amazon.awssdk.enhanced.dynamodb.model.ItemAttributeValue;
import software.amazon.awssdk.enhanced.dynamodb.model.RequestItem;
import software.amazon.awssdk.enhanced.dynamodb.model.ResponseItem;
import software.amazon.awssdk.enhanced.dynamodb.model.TypeToken;

/**
 * A {@link AttributeConverter} that includes all of the converters built into the SDK.
 *
 * <p>
 * This is the root converter for all created {@link DynamoDbEnhancedClient}s and {@link DynamoDbEnhancedAsyncClient}s.
 *
 * <p>
 * Supported Number Types:
 * <ul>
 *     <li>{@link Instant}</li>
 *     <li>{@link Integer}</li>
 * </ul>
 *
 * <p>
 * Supported String Types:
 * <ul>
 *     <li>{@link String}</li>
 * </ul>
 *
 * <p>
 * Supported List Types:
 * <ul>
 *     <li>{@link List} (plus subtypes)</li>
 * </ul>
 *
 * <p>
 * Supported Item Types:
 * <ul>
 *     <li>{@link Map} (plus subtypes)</li>
 *     <li>{@link RequestItem} (plus subtypes)</li>
 *     <li>{@link ResponseItem} (plus subtypes)</li>
 *     <li>{@link ItemAttributeValue}</li>
 * </ul>
 *
 * <p>
 * This can be created via {@link #create()}.
 */
@SdkPublicApi
@ThreadSafe
@Immutable
public final class DefaultAttributeConverter implements SubtypeAttributeConverter<Object> {
    private static final SubtypeAttributeConverter<Object> CHAIN;

    static {
        CHAIN = AttributeConverterChain.builder()
                                       // Exact InstanceOf Converters

                                       .addConverter(InstantAttributeConverter.create())
                                       .addConverter(IntegerAttributeConverter.create())
                                       .addConverter(StringAttributeConverter.create())
                                       .addConverter(AttributeAttributeConverter.create())

                                       // InstanceOf Converters
                                       // Potential optimization: allow InstanceOf converters to specify a set of
                                       // types that should be cached in an eager fashion (e.g. DefaultRequestItem)
                                       .addConverter(RequestItemAttributeConverter.create())
                                       .addConverter(ResponseItemAttributeConverter.create())
                                       .addSubtypeConverter(DynamicListAttributeConverter.create())
                                       .addSubtypeConverter(DynamicMapAttributeConverter.create())
                                       .build();
    }

    private DefaultAttributeConverter() {}

    /**
     * Create a default convert chain that contains all of the converters built into the SDK.
     *
     * <p>
     * This call should never fail with an {@link Exception}.
     *
     * TODO: Default attribute converter uses create(), and default string converter uses instance().
     */
    public static DefaultAttributeConverter create() {
        return new DefaultAttributeConverter();
    }

    @Override
    public TypeToken<Object> type() {
        return TypeToken.from(Object.class);
    }

    @Override
    public ItemAttributeValue toAttributeValue(Object input, ConversionContext context) {
        return CHAIN.toAttributeValue(input, context);
    }

    @Override
    public <U> U fromAttributeValue(ItemAttributeValue input, TypeToken<U> desiredType, ConversionContext context) {
        return CHAIN.fromAttributeValue(input, desiredType, context);
    }
}
