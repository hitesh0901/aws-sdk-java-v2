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

import java.util.LinkedHashMap;
import java.util.Map;
import software.amazon.awssdk.annotations.Immutable;
import software.amazon.awssdk.annotations.SdkPublicApi;
import software.amazon.awssdk.annotations.ThreadSafe;
import software.amazon.awssdk.enhanced.dynamodb.internal.model.DefaultConvertableItemAttributeValue;
import software.amazon.awssdk.enhanced.dynamodb.model.ConvertableItemAttributeValue;
import software.amazon.awssdk.enhanced.dynamodb.model.ItemAttributeValue;
import software.amazon.awssdk.enhanced.dynamodb.model.ResponseItem;
import software.amazon.awssdk.enhanced.dynamodb.model.TypeConvertingVisitor;
import software.amazon.awssdk.enhanced.dynamodb.model.TypeToken;

/**
 * A converter between {@link ResponseItem} and {@link ItemAttributeValue}.
 */
@SdkPublicApi
@ThreadSafe
@Immutable
public final class ResponseItemAttributeConverter implements AttributeConverter<ResponseItem> {
    public static ResponseItemAttributeConverter create() {
        return new ResponseItemAttributeConverter();
    }

    @Override
    public TypeToken<ResponseItem> type() {
        return TypeToken.from(ResponseItem.class);
    }

    @Override
    public ItemAttributeValue toAttributeValue(ResponseItem input, ConversionContext context) {
        throw new UnsupportedOperationException("Cannot convert a ResponseItem to an ItemAttributeValue.");
    }

    @Override
    public ResponseItem fromAttributeValue(ItemAttributeValue input, ConversionContext context) {
        return input.convert(new TypeConvertingVisitor<ResponseItem>(ResponseItem.class, ResponseItemAttributeConverter.class) {
            @Override
            public ResponseItem convertMap(Map<String, ItemAttributeValue> value) {
                Map<String, ConvertableItemAttributeValue> attributes = new LinkedHashMap<>();
                value.forEach((k, v) -> attributes.put(k, toConvertableAttribute(context, k, v)));

                return ResponseItem.builder()
                                   .putAttributes(attributes)
                                   .build();
            }
        });
    }

    private ConvertableItemAttributeValue toConvertableAttribute(ConversionContext context,
                                                                 String key,
                                                                 ItemAttributeValue value) {
        return DefaultConvertableItemAttributeValue.builder()
                                                   .conversionContext(context.toBuilder()
                                                                             .attributeName(key)
                                                                             .build())
                                                   .attributeValue(value)
                                                   .build();
    }
}
