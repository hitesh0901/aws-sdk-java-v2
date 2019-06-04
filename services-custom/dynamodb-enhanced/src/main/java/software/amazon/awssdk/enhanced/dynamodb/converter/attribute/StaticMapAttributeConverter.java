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
import software.amazon.awssdk.enhanced.dynamodb.converter.string.StringConverter;
import software.amazon.awssdk.enhanced.dynamodb.model.ItemAttributeValue;
import software.amazon.awssdk.enhanced.dynamodb.model.TypeConvertingVisitor;
import software.amazon.awssdk.enhanced.dynamodb.model.TypeToken;

public class StaticMapAttributeConverter<K, V> implements AttributeConverter<Map<K, V>> {
    private final StringConverter<K> keyConverter;
    private final AttributeConverter<V> valueConverter;

    private StaticMapAttributeConverter(StringConverter<K> keyConverter,
                                        AttributeConverter<V> valueConverter) {
        this.keyConverter = keyConverter;
        this.valueConverter = valueConverter;
    }

    public static <K, V> StaticMapAttributeConverter<K, V> create(StringConverter<K> keyConverter,
                                                                  AttributeConverter<V> valueConverter) {
        return new StaticMapAttributeConverter<>(keyConverter, valueConverter);
    }

    @Override
    public TypeToken<Map<K, V>> type() {
        return TypeToken.mapOf(keyConverter.type(), valueConverter.type());
    }

    @Override
    public ItemAttributeValue toAttributeValue(Map<K, V> input, ConversionContext context) {
        Map<String, ItemAttributeValue> result = new LinkedHashMap<>();
        input.forEach((k, v) -> result.put(keyConverter.toString(k), valueConverter.toAttributeValue(v, context)));
        return ItemAttributeValue.fromMap(result);
    }

    @Override
    public Map<K, V> fromAttributeValue(ItemAttributeValue input, ConversionContext context) {
        return input.convert(new TypeConvertingVisitor<Map<K, V>>(Map.class, StaticMapAttributeConverter.class) {
            @Override
            public Map<K, V> convertMap(Map<String, ItemAttributeValue> value) {
                Map<K, V> result = new LinkedHashMap<>();
                value.forEach((k, v) -> result.put(keyConverter.fromString(k), valueConverter.fromAttributeValue(v, context)));
                return result;
            }
        });
    }
}
