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

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import software.amazon.awssdk.enhanced.dynamodb.model.ItemAttributeValue;
import software.amazon.awssdk.enhanced.dynamodb.model.TypeConvertingVisitor;
import software.amazon.awssdk.enhanced.dynamodb.model.TypeToken;

public class StaticListAttributeConverter<T> implements AttributeConverter<List<T>> {
    private final AttributeConverter<T> elementConverter;

    private StaticListAttributeConverter(AttributeConverter<T> elementConverter) {
        this.elementConverter = elementConverter;
    }

    public static <T> StaticListAttributeConverter<T> create(AttributeConverter<T> elementConverter) {
        return new StaticListAttributeConverter<>(elementConverter);
    }

    @Override
    public TypeToken<List<T>> type() {
        return TypeToken.listOf(elementConverter.type());
    }

    @Override
    public ItemAttributeValue toAttributeValue(List<T> input, ConversionContext context) {
        return ItemAttributeValue.fromListOfAttributeValues(input.stream()
                                                                 .map(e -> elementConverter.toAttributeValue(e, context))
                                                                 .collect(toList()));
    }

    @Override
    public List<T> fromAttributeValue(ItemAttributeValue input, ConversionContext context) {
        return input.convert(new TypeConvertingVisitor<List<T>>(List.class, StaticListAttributeConverter.class) {
            @Override
            public List<T> convertListOfAttributeValues(Collection<ItemAttributeValue> value) {
                return value.stream()
                            .map(attribute -> elementConverter.fromAttributeValue(attribute, context))
                            .collect(Collectors.toList());
            }
        });
    }
}
