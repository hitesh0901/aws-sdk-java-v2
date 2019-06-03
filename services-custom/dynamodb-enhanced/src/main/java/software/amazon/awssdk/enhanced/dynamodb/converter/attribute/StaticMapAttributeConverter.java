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
import java.util.List;
import java.util.Map;
import software.amazon.awssdk.enhanced.dynamodb.converter.string.StringConverter;
import software.amazon.awssdk.enhanced.dynamodb.model.ItemAttributeValue;
import software.amazon.awssdk.enhanced.dynamodb.model.TypeConvertingVisitor;
import software.amazon.awssdk.enhanced.dynamodb.model.TypeToken;

public class StaticMapAttributeConverter extends InstanceOfAttributeConverter<Map<?, ?>> {
    private final Class<?> valueType;
    private final StringConverter<?> keyConverter;
    private final ExactInstanceOfAttributeConverter<?> valueConverter;

    private StaticMapAttributeConverter(StringConverter<?> keyConverter,
                                        ExactInstanceOfAttributeConverter<?> valueConverter) {
        super(List.class);
        this.keyConverter = keyConverter;
        this.valueType = valueConverter.type();
        this.valueConverter = valueConverter;
    }

    public static StaticMapAttributeConverter create(StringConverter<?> keyConverter,
                                                     ExactInstanceOfAttributeConverter<?> valueConverter) {
        return new StaticMapAttributeConverter(keyConverter, valueConverter);
    }

    @Override
    protected ItemAttributeValue convertToAttributeValue(Map<?, ?> input, ConversionContext context) {
        Map<String, ItemAttributeValue> result = new LinkedHashMap<>();
        input.forEach((k, v) -> {
            result.put(keyConverter.fromString(k),
                       valueConverter.toAttributeValue(v, ctx -> ctx.attributeName(context.attributeName().orElse(null))
                                                                    .converter(valueConverter)));
        });
        return ItemAttributeValue.fromMap(result);
    }

    @Override
    protected Map<?, ?> convertFromAttributeValue(ItemAttributeValue input, TypeToken<?> desiredType, ConversionContext context) {
        return input.convert(new TypeConvertingVisitor<Map<?, ?>>(Map.class, StaticMapAttributeConverter.class) {
            @Override
            public Map<?, ?> convertMap(Map<String, ItemAttributeValue> value) {
                Map<Object, Object> result = new LinkedHashMap<>();
                result.put()
                return super.convertMap(value);
            }
        });
    }

    private ItemAttributeValue elementToAttributeValue(Object element, ConversionContext conversionContext) {
        return valueConverter.toAttributeValue(element, c -> c.attributeName(conversionContext.attributeName().orElse(null))
                                                              .converter(valueConverter));
    }

    private Object elementFromAttributeValue(ItemAttributeValue attribute, ConversionContext context) {
        return valueConverter.fromAttributeValue(
                attribute,
                TypeToken.from(valueType),
                ctx -> ctx.attributeName(context.attributeName().orElse(null))
                          .converter(valueConverter));
    }
}
