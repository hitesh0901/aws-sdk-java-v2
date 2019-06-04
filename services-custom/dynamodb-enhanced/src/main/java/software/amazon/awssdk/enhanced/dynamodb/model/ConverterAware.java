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

package software.amazon.awssdk.enhanced.dynamodb.model;

import java.util.Collection;
import java.util.List;
import software.amazon.awssdk.annotations.Immutable;
import software.amazon.awssdk.annotations.NotThreadSafe;
import software.amazon.awssdk.annotations.SdkPublicApi;
import software.amazon.awssdk.annotations.ThreadSafe;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.converter.attribute.DefaultAttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.converter.attribute.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.converter.attribute.SubtypeAttributeConverter;

/**
 * An interface applied to all objects that wish to expose their underlying {@link AttributeConverter}s.
 *
 * <p>
 * See {@link AttributeConverter} for a detailed explanation of how the enhanced client converts between Java types
 * and DynamoDB types.
 */
@SdkPublicApi
@ThreadSafe
@Immutable
public interface ConverterAware<T> {
    /**
     * Retrieve all converters that were directly configured on this object.
     *
     * <p>
     * This call should never fail with an {@link Exception}.
     */
    List<AttributeConverter<? extends T>> converters();

    List<SubtypeAttributeConverter<? extends T>> subtypeConverters();

    /**
     * An interface applied to all objects that can be configured with {@link AttributeConverter}s.
     *
     * <p>
     * See {@link AttributeConverter} for a detailed explanation of how the enhanced client converts between Java types
     * and DynamoDB types.
     *
     * <p>
     * This call should never fail with an {@link Exception}.
     */
    @NotThreadSafe
    interface Builder<T> {
        /**
         * Add all of the provided converters to this builder, in the order of the provided collection.
         *
         * <p>
         * Converters earlier in the provided list take precedence over the ones later in the list, even if the later ones
         * refer to a more specific type. Converters should be added in an order from most-specific to least-specific.
         *
         * <p>
         * Converters configured in {@link RequestItem.Builder} always take precedence over the ones configured in
         * {@link DynamoDbEnhancedClient.Builder}.
         *
         * <p>
         * Converters configured in {@link DynamoDbEnhancedClient.Builder} always take precedence over the ones provided by the
         * {@link DefaultAttributeConverter}.
         *
         * <p>
         * Reasons this call may fail with a {@link RuntimeException}:
         * <ol>
         *     <li>The provided converter collection or one of its members is null.</li>
         *     <li>If this or any other {@code converter}-modifying method is called in parallel with this one.
         *     This method is not thread safe.</li>
         * </ol>
         *
         * @see AttributeConverter
         */
        Builder<T> addConverters(Collection<? extends AttributeConverter<? extends T>> converters);

        /**
         * Add a converter to this builder.
         *
         * <p>
         * Converters added earlier take precedence over the ones added later, even if the later ones refer to
         * a more specific type. Converters should be added in an order from most-specific to least-specific.
         *
         * <p>
         * Converters configured in {@link RequestItem.Builder} always take precedence over the ones configured in
         * {@link DynamoDbEnhancedClient.Builder}.
         *
         * <p>
         * Converters configured in {@link DynamoDbEnhancedClient.Builder} always take precedence over the ones provided by the
         * {@link DefaultAttributeConverter}.
         *
         * <p>
         * Reasons this call may fail with a {@link RuntimeException}:
         * <ol>
         *     <li>The provided converter is null.</li>
         *     <li>If this or any other {@code converter}-modifying method is called in parallel from multiple threads.
         *     This method is not thread safe.</li>
         * </ol>
         */
        Builder<T> addConverter(AttributeConverter<? extends T> converter);

        Builder<T> addSubtypeConverters(Collection<? extends SubtypeAttributeConverter<? extends T>> converters);

        Builder<T> addSubtypeConverter(SubtypeAttributeConverter<? extends T> converter);

        /**
         * Reset the converters that were previously added with {@link #addConverters(Collection)} or
         * {@link #addConverter(AttributeConverter)}.
         *
         * <p>
         * This <b>does not</b> reset converters configured elsewhere. Converters configured in other locations, such as in the
         * {@link DefaultAttributeConverter}, will still be used.
         *
         * <p>
         * Reasons this call may fail with a {@link RuntimeException}:
         * <ol>
         *     <li>If this or any other {@code converter}-modifying method is called in parallel from multiple threads.
         *     This method is not thread safe.</li>
         * </ol>
         */
        Builder<T> clearConverters();

        Builder<T> clearSubtypeConverters();
    }
}
