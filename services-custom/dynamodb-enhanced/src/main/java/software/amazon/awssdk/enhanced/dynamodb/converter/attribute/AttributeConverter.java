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

import java.util.function.Consumer;
import software.amazon.awssdk.annotations.SdkPublicApi;
import software.amazon.awssdk.annotations.ThreadSafe;
import software.amazon.awssdk.enhanced.dynamodb.converter.Converter;
import software.amazon.awssdk.enhanced.dynamodb.model.ItemAttributeValue;
import software.amazon.awssdk.enhanced.dynamodb.model.TypeToken;

@SdkPublicApi
@ThreadSafe
public interface AttributeConverter<T> extends Converter<T> {
    ItemAttributeValue toAttributeValue(T input, ConversionContext context);

    T fromAttributeValue(ItemAttributeValue input, ConversionContext context);

    default ItemAttributeValue toAttributeValue(T input, Consumer<ConversionContext.Builder> contextConsumer) {
        ConversionContext.Builder context = ConversionContext.builder();
        contextConsumer.accept(context);
        return toAttributeValue(input, context.build());
    }

    default Object fromAttributeValue(ItemAttributeValue input,
                                      Consumer<ConversionContext.Builder> contextConsumer) {
        ConversionContext.Builder context = ConversionContext.builder();
        contextConsumer.accept(context);
        return fromAttributeValue(input, context.build());
    }
}
