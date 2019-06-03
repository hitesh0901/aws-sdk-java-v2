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

package software.amazon.awssdk.enhanced.dynamodb.converter.item.bundled.bean;

import java.util.LinkedHashMap;
import java.util.Map;
import software.amazon.awssdk.enhanced.dynamodb.converter.attribute.ConversionCondition;
import software.amazon.awssdk.enhanced.dynamodb.converter.attribute.ConversionContext;
import software.amazon.awssdk.enhanced.dynamodb.converter.attribute.ItemAttributeValueConverter;
import software.amazon.awssdk.enhanced.dynamodb.converter.attribute.ExactInstanceOfAttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.model.ItemAttributeValue;
import software.amazon.awssdk.enhanced.dynamodb.model.TypeConvertingVisitor;
import software.amazon.awssdk.enhanced.dynamodb.model.TypeToken;
import software.amazon.awssdk.utils.Validate;

public class StaticBeanItemAttributeConverter<T> extends ExactInstanceOfAttributeConverter<T> {
    private final BeanItemSchema<T> schema;

    private StaticBeanItemAttributeConverter(BeanItemSchema<T> schema) {
        super(schema.beanType().rawClass());
        this.schema = schema;
    }

    public static <T> StaticBeanItemAttributeConverter<T> create(BeanItemSchema<T> schema) {
        return new StaticBeanItemAttributeConverter<>(schema);
    }

    @Override
    public ConversionCondition conversionCondition() {
        return ConversionCondition.isExactInstanceOf(schema.beanType().rawClass());
    }

    @Override
    public ItemAttributeValue convertToAttributeValue(T input, ConversionContext context) {
        Map<String, ItemAttributeValue> mappedValues = new LinkedHashMap<>();
        schema.attributeSchemas().forEach(attributeSchema -> {
            Object unmappedValue = attributeSchema.getter().apply(input);
            if (unmappedValue != null) {
                ItemAttributeValue mappedValue =
                        attributeSchema.converter()
                                       .toAttributeValue(unmappedValue, ctx -> ctx.attributeName(attributeSchema.attributeName())
                                                                                  .converter(attributeSchema.converter()));

                mappedValues.put(attributeSchema.attributeName(), mappedValue);
            }
        });
        return ItemAttributeValue.fromMap(mappedValues);
    }

    @Override
    public T convertFromAttributeValue(ItemAttributeValue input, TypeToken<?> desiredType, ConversionContext context) {
        return input.convert(new Visitor(desiredType.rawClass()));
    }

    private class Visitor extends TypeConvertingVisitor<T> {
        private Visitor(Class<?> targetType) {
            super(targetType, StaticBeanItemAttributeConverter.class);
        }

        @Override
        public T convertMap(Map<String, ItemAttributeValue> value) {
            T response = schema.constructor().get();

            Validate.isInstanceOf(targetType, response,
                                  "Item constructor created a %s, but a %s was requested.",
                                  response.getClass(), targetType);

            schema.attributeSchemas().forEach(attributeSchema -> {
                ItemAttributeValue mappedValue = value.get(attributeSchema.attributeName());
                convertAndSet(mappedValue, response, attributeSchema);
            });

            return response;
        }

        private <U> void convertAndSet(ItemAttributeValue mappedValue,
                                       T response,
                                       BeanAttributeSchema<T, U> attributeSchema) {
            ItemAttributeValueConverter converter = attributeSchema.converter();
            Object unmappedValue =
                    converter.fromAttributeValue(
                            mappedValue,
                            attributeSchema.attributeType(),
                            ctx -> ctx.attributeName(attributeSchema.attributeName()).converter(converter));

            attributeSchema.setter().accept(response, attributeSchema.attributeType().rawClass().cast(unmappedValue));
        }
    }
}
