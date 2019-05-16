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

import software.amazon.awssdk.enhanced.dynamodb.converter.attribute.ItemAttributeValueConverter;
import software.amazon.awssdk.enhanced.dynamodb.converter.item.ItemConverter;
import software.amazon.awssdk.enhanced.dynamodb.model.ConvertableItemAttributeValue;
import software.amazon.awssdk.enhanced.dynamodb.model.RequestItem;
import software.amazon.awssdk.enhanced.dynamodb.model.ResponseItem;
import software.amazon.awssdk.enhanced.dynamodb.model.TypeToken;
import software.amazon.awssdk.utils.Either;
import software.amazon.awssdk.utils.Validate;

public class StaticBeanItemConverter implements ItemConverter {
    private final BeanItemSchema schema;

    public StaticBeanItemConverter(BeanItemSchema schema) {
        this.schema = schema;
    }

    @Override
    public RequestItem toRequestItem(Object request) {
        RequestItem.Builder requestItem = RequestItem.builder();
        schema.attributeSchemas().forEach(attributeSchema -> {
            Object unmappedValue = attributeSchema.getter().apply(request);
            Object mappedValue = mapValue(attributeSchema.converter(), attributeSchema, unmappedValue);
            requestItem.putAttribute(attributeSchema.attributeName(), mappedValue);

        });
        return requestItem.build();
    }

    @Override
    public <T> T fromResponseItem(TypeToken<T> targetType, ResponseItem responseItem) {
        Object response = schema.constructor().get();
        Class<T> targetClass = targetType.rawClass();
        T castResponse = Validate.isAssignableFrom(targetClass, response.getClass(),
                                                   "Item constructor created a %s, but a %s was requested.",
                                                   response.getClass(), targetClass)
                                 .cast(response);

        schema.attributeSchemas().forEach(attributeSchema -> {
            ConvertableItemAttributeValue mappedValue = responseItem.attribute(attributeSchema.attributeName());
            Object unmappedValue = unmapValue(attributeSchema.converter(), attributeSchema, mappedValue);
            attributeSchema.setter().accept(castResponse, unmappedValue);
        });

        return castResponse;
    }

    private Object mapValue(Either<ItemAttributeValueConverter, ItemConverter> converters,
                            BeanAttributeSchema attributeSchema,
                            Object unmappedValue) {
        return converters.map(
                c -> c.toAttributeValue(unmappedValue, i -> i.attributeName(attributeSchema.attributeName())
                                                             .converter(c)),
                c -> c.toRequestItem(unmappedValue));
    }

    private Object unmapValue(Either<ItemAttributeValueConverter, ItemConverter> converters,
                              BeanAttributeSchema attributeSchema,
                              ConvertableItemAttributeValue mappedValue) {
        return converters.map(
                c -> c.fromAttributeValue(
                        mappedValue.attributeValue(),
                        attributeSchema.setterInputType(),
                        i -> i.attributeName(attributeSchema.attributeName()).converter(c)),
                c -> c.fromResponseItem(attributeSchema.setterInputType(), mappedValue.as(ResponseItem.class)));
    }
}
