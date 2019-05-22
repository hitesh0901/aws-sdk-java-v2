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

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import software.amazon.awssdk.enhanced.dynamodb.converter.attribute.ConversionContext;
import software.amazon.awssdk.enhanced.dynamodb.converter.attribute.ExactInstanceOfConverter;
import software.amazon.awssdk.enhanced.dynamodb.converter.attribute.ItemAttributeValueConverter;
import software.amazon.awssdk.enhanced.dynamodb.converter.attribute.InstanceOfConverter;
import software.amazon.awssdk.enhanced.dynamodb.model.ItemAttributeValue;
import software.amazon.awssdk.enhanced.dynamodb.model.TypeConvertingVisitor;
import software.amazon.awssdk.enhanced.dynamodb.model.TypeToken;

public class StaticListConverter extends InstanceOfConverter<List<?>> {
    private final Class<?> elementType;
    private final ItemAttributeValueConverter elementConverter;

    private StaticListConverter(Class<?> elementType, ItemAttributeValueConverter elementConverter) {
        super(List.class);
        this.elementType = elementType;
        this.elementConverter = elementConverter;
    }

    public static StaticListConverter create(ExactInstanceOfConverter<?> elementConverter) {
        return new StaticListConverter(elementConverter.type(), elementConverter);
    }

    @Override
    protected ItemAttributeValue convertToAttributeValue(List<?> input, ConversionContext conversionContext) {
        return ItemAttributeValue.fromListOfAttributeValues(input.stream()
                                                                 .map(e -> elementToAttributeValue(e, conversionContext))
                                                                 .collect(toList()));
    }

    @Override
    protected List<?> convertFromAttributeValue(ItemAttributeValue input, TypeToken<?> desiredType, ConversionContext context) {
        return input.convert(new TypeConvertingVisitor<List<?>>(List.class, StaticListConverter.class) {
            @Override
            public List<?> convertListOfAttributeValues(Collection<ItemAttributeValue> value) {
                return value.stream()
                            .map(attribute -> elementFromAttributeValue(attribute, context))
                            .collect(toList());
            }
        });
    }

    private ItemAttributeValue elementToAttributeValue(Object element, ConversionContext conversionContext) {
        return elementConverter.toAttributeValue(element, c -> c.attributeName(conversionContext.attributeName().orElse(null))
                                                                .converter(elementConverter));
    }

    private Object elementFromAttributeValue(ItemAttributeValue attribute, ConversionContext context) {
        return elementConverter.fromAttributeValue(
                attribute,
                TypeToken.from(elementType),
                ctx -> ctx.attributeName(context.attributeName().orElse(null))
                          .converter(elementConverter));
    }
}
