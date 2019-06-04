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

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import software.amazon.awssdk.annotations.Immutable;
import software.amazon.awssdk.annotations.SdkPublicApi;
import software.amazon.awssdk.annotations.ThreadSafe;
import software.amazon.awssdk.enhanced.dynamodb.converter.string.DefaultStringConverter;
import software.amazon.awssdk.enhanced.dynamodb.model.ItemAttributeValue;
import software.amazon.awssdk.enhanced.dynamodb.model.TypeConvertingVisitor;
import software.amazon.awssdk.enhanced.dynamodb.model.TypeToken;
import software.amazon.awssdk.utils.Validate;

/**
 * A converter between {@link Map} subtypes and {@link ItemAttributeValue}.
 */
@SdkPublicApi
@ThreadSafe
@Immutable
public final class DynamicMapAttributeConverter implements SubtypeAttributeConverter<Map<?, ?>> {
    private static final TypeToken<Map<?, ?>> TYPE = new TypeToken<Map<?, ?>>() {};

    public static DynamicMapAttributeConverter create() {
        return new DynamicMapAttributeConverter();
    }

    @Override
    public TypeToken<Map<?, ?>> type() {
        return TYPE;
    }

    @Override
    public ItemAttributeValue toAttributeValue(Map<?, ?> input, ConversionContext context) {
        Map<String, ItemAttributeValue> result = new LinkedHashMap<>();
        input.forEach((key, value) -> result.put(DefaultStringConverter.instance().toString(key),
                                                 context.converter().toAttributeValue(value, context)));
        return ItemAttributeValue.fromMap(result);
    }

    @Override
    public <T extends Map<?, ?>> T fromAttributeValue(ItemAttributeValue input,
                                                      TypeToken<T> desiredType,
                                                      ConversionContext context) {
        Class<?> mapType = desiredType.rawClass();
        List<TypeToken<?>> mapTypeParameters = desiredType.rawClassParameters();

        Validate.isTrue(mapTypeParameters.size() == 2,
                        "The desired Map type appears to be parameterized with more than 2 types: %s", desiredType);
        TypeToken<?> keyType = mapTypeParameters.get(0);
        TypeToken<?> valueType = mapTypeParameters.get(1);

        return input.convert(new TypeConvertingVisitor<T>(Map.class, DynamicMapAttributeConverter.class) {
            @Override
            public T convertMap(Map<String, ItemAttributeValue> value) {
                Map<Object, Object> result = createMap(mapType);
                value.forEach((k, v) -> {
                    result.put(DefaultStringConverter.instance().fromString(keyType, k),
                               context.converter().fromAttributeValue(v, valueType, context));
                });
                // This is a safe cast - We know the values we added to the map match the type that the customer requested.
                return (T) result;
            }
        });
    }

    private Map<Object, Object> createMap(Class<?> mapType) {
        if (mapType.isInterface()) {
            Validate.isTrue(mapType.equals(Map.class), "Requested interface type %s is not supported.", mapType);
            return new LinkedHashMap<>();
        }

        try {
            return (Map<Object, Object>) mapType.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Failed to instantiate the requested type " + mapType.getTypeName() + ".", e);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Requested type " + mapType.getTypeName() + " is not supported, because it " +
                                            "does not have a zero-arg constructor.", e);
        }
    }
}
