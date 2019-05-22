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

package software.amazon.awssdk.enhanced.dynamodb.converter.attribute.bundled;

import java.util.LinkedHashMap;
import java.util.Map;
import software.amazon.awssdk.annotations.Immutable;
import software.amazon.awssdk.annotations.SdkPublicApi;
import software.amazon.awssdk.annotations.ThreadSafe;
import software.amazon.awssdk.enhanced.dynamodb.converter.attribute.ConversionContext;
import software.amazon.awssdk.enhanced.dynamodb.converter.attribute.ItemAttributeValueConverter;
import software.amazon.awssdk.enhanced.dynamodb.converter.attribute.InstanceOfConverter;
import software.amazon.awssdk.enhanced.dynamodb.internal.converter.ItemAttributeValueConverterChain;
import software.amazon.awssdk.enhanced.dynamodb.model.ItemAttributeValue;
import software.amazon.awssdk.enhanced.dynamodb.model.RequestItem;
import software.amazon.awssdk.enhanced.dynamodb.model.TypeToken;

/**
 * A converter between {@link RequestItem} and {@link ItemAttributeValue}.
 */
@SdkPublicApi
@ThreadSafe
@Immutable
public final class RequestItemConverter extends InstanceOfConverter<RequestItem> {
    private RequestItemConverter() {
        super(RequestItem.class);
    }

    public static RequestItemConverter create() {
        return new RequestItemConverter();
    }

    @Override
    protected ItemAttributeValue convertToAttributeValue(RequestItem input, ConversionContext conversionContext) {
        ItemAttributeValueConverter converter = ItemAttributeValueConverterChain.builder()
                                                                                .addConverters(input.converters())
                                                                                .parent(conversionContext.converter())
                                                                                .build();

        ConversionContext.Builder conversionContextBuilder = conversionContext.toBuilder()
                                                                              .converter(converter);

        Map<String, ItemAttributeValue> result = new LinkedHashMap<>();
        input.attributes().forEach((key, value) -> result.put(key, toItemAttributeValue(conversionContextBuilder, key, value)));
        return ItemAttributeValue.fromMap(result);
    }

    private ItemAttributeValue toItemAttributeValue(ConversionContext.Builder contextBuilder, String key, Object value) {
        ConversionContext context = contextBuilder.attributeName(key).build();
        return context.converter().toAttributeValue(value, context);
    }

    @Override
    protected RequestItem convertFromAttributeValue(ItemAttributeValue input,
                                                    TypeToken<?> desiredType,
                                                    ConversionContext context) {
        throw new UnsupportedOperationException("Cannot convert an ItemAttributeValue to a RequestItem.");
    }
}
