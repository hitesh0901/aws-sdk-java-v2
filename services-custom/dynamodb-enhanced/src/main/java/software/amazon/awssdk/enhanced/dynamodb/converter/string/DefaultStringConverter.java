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

package software.amazon.awssdk.enhanced.dynamodb.converter.string;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import software.amazon.awssdk.enhanced.dynamodb.model.TypeToken;

public class DefaultStringConverter implements SubtypeStringConverter<Object> {
    private final Map<Class<?>, StringConverter<?>> converters;

    private static final DefaultStringConverter INSTANCE = new DefaultStringConverter();

    public static DefaultStringConverter instance() {
        return INSTANCE;
    }

    private DefaultStringConverter() {
        Map<Class<?>, StringConverter<?>> converters = new HashMap<>();

        // Primitive Types
        putConverter(converters, boolean.class, BooleanStringConverter.create());
        putConverter(converters, short.class, ShortStringConverter.create());
        putConverter(converters, int.class, IntegerStringConverter.create());
        putConverter(converters, long.class, LongStringConverter.create());
        putConverter(converters, float.class, FloatStringConverter.create());
        putConverter(converters, double.class, DoubleStringConverter.create());
        putConverter(converters, char.class, CharacterStringConverter.create());
        putConverter(converters, byte.class, ByteStringConverter.create());

        // Primitive Array Types
        putConverter(converters, ByteArrayStringConverter.create());
        putConverter(converters, CharacterArrayStringConverter.create());

        // Boxed Primitive Types
        putConverter(converters, BooleanStringConverter.create());
        putConverter(converters, ShortStringConverter.create());
        putConverter(converters, IntegerStringConverter.create());
        putConverter(converters, LongStringConverter.create());
        putConverter(converters, FloatStringConverter.create());
        putConverter(converters, DoubleStringConverter.create());
        putConverter(converters, CharacterStringConverter.create());
        putConverter(converters, ByteStringConverter.create());

        // String Types
        putConverter(converters, StringStringConverter.create());
        putConverter(converters, CharSequenceStringConverter.create());
        putConverter(converters, StringBufferStringConverter.create());
        putConverter(converters, StringBuilderStringConverter.create());

        // Number Types
        putConverter(converters, BigIntegerStringConverter.create());
        putConverter(converters, BigDecimalStringConverter.create());

        // Atomic Types
        putConverter(converters, AtomicLongStringConverter.create());
        putConverter(converters, AtomicIntegerStringConverter.create());
        putConverter(converters, AtomicBooleanStringConverter.create());

        // Optional Types
        putConverter(converters, OptionalIntStringConverter.create());
        putConverter(converters, OptionalLongStringConverter.create());
        putConverter(converters, OptionalDoubleStringConverter.create());

        // Time Types
        putConverter(converters, InstantStringConverter.create());
        putConverter(converters, DurationStringConverter.create());
        putConverter(converters, LocalDateStringConverter.create());
        putConverter(converters, LocalTimeStringConverter.create());
        putConverter(converters, LocalDateTimeStringConverter.create());
        putConverter(converters, OffsetTimeStringConverter.create());
        putConverter(converters, OffsetDateTimeStringConverter.create());
        putConverter(converters, ZonedDateTimeStringConverter.create());
        putConverter(converters, YearStringConverter.create());
        putConverter(converters, YearMonthStringConverter.create());
        putConverter(converters, MonthDayStringConverter.create());
        putConverter(converters, PeriodStringConverter.create());
        putConverter(converters, ZoneOffsetStringConverter.create());
        putConverter(converters, ZoneIdStringConverter.create());

        // Other
        putConverter(converters, UuidStringConverter.create());
        putConverter(converters, UrlStringConverter.create());
        putConverter(converters, UriStringConverter.create());

        this.converters = Collections.unmodifiableMap(converters);
    }

    @Override
    public TypeToken<Object> type() {
        return TypeToken.from(Object.class);
    }

    @Override
    public String toString(Object input) {
        if (input == null) {
            return null;
        }

        return convertToString(input);
    }

    @Override
    public <T> T fromString(TypeToken<T> targetType, String input) {
        if (input == null) {
            return null;
        }

        return convertFromString(targetType.rawClass(), input);
    }

    private <T> void putConverter(Map<Class<?>, StringConverter<?>> converters, StringConverter<T> converter) {
        converters.put(converter.type().rawClass(), converter);
    }

    private <T> void putConverter(Map<Class<?>, StringConverter<?>> converters, Class<T> type, StringConverter<T> converter) {
         converters.put(type, converter);
    }

    @SuppressWarnings("unchecked") // Cast is safe, thanks to using putConverter during initialization
    private <U> String convertToString(U input) {
        Class<U> clazz = (Class<U>) input.getClass();
        return getConverter(clazz).toString(input);
    }

    private <T> T convertFromString(Class<T> targetType, String input) {
        return getConverter(targetType).fromString(input);
    }

    @SuppressWarnings("unchecked") // Cast is safe, thanks to using putConverter during initialization
    private <T> StringConverter<T> getConverter(Class<T> clazz) {
        StringConverter<T> converter = (StringConverter<T>) converters.get(clazz);
        if (converter == null) {
            throw new IllegalArgumentException("No string converter exists for " + clazz);
        }
        return converter;
    }
}
