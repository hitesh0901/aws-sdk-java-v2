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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DefaultStringConverter implements UntypedStringConverter {
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
        putConverter(converters, byte[].class, ByteArrayStringConverter.create());
        putConverter(converters, char[].class, CharacterArrayStringConverter.create());

        // Boxed Primitive Types
        putConverter(converters, Boolean.class, BooleanStringConverter.create());
        putConverter(converters, Short.class, ShortStringConverter.create());
        putConverter(converters, Integer.class, IntegerStringConverter.create());
        putConverter(converters, Long.class, LongStringConverter.create());
        putConverter(converters, Float.class, FloatStringConverter.create());
        putConverter(converters, Double.class, DoubleStringConverter.create());
        putConverter(converters, Character.class, CharacterStringConverter.create());
        putConverter(converters, Byte.class, ByteStringConverter.create());

        // String Types
        putConverter(converters, String.class, StringStringConverter.create());
        putConverter(converters, CharSequence.class, CharSequenceStringConverter.create());
        putConverter(converters, StringBuffer.class, StringBufferStringConverter.create());
        putConverter(converters, StringBuilder.class, StringBuilderStringConverter.create());

        // Number Types
        putConverter(converters, BigInteger.class, BigIntegerStringConverter.create());
        putConverter(converters, BigDecimal.class, BigDecimalStringConverter.create());

        // Atomic Types
        putConverter(converters, AtomicLong.class, AtomicLongStringConverter.create());
        putConverter(converters, AtomicInteger.class, AtomicIntegerStringConverter.create());
        putConverter(converters, AtomicBoolean.class, AtomicBooleanStringConverter.create());

        // Optional Types
        putConverter(converters, OptionalInt.class, OptionalIntStringConverter.create());
        putConverter(converters, OptionalLong.class, OptionalLongStringConverter.create());
        putConverter(converters, OptionalDouble.class, OptionalDoubleStringConverter.create());

        // Time Types
        putConverter(converters, Instant.class, InstantStringConverter.create());
        putConverter(converters, Duration.class, DurationStringConverter.create());
        putConverter(converters, LocalDate.class, LocalDateStringConverter.create());
        putConverter(converters, LocalTime.class, LocalTimeStringConverter.create());
        putConverter(converters, LocalDateTime.class, LocalDateTimeStringConverter.create());
        putConverter(converters, OffsetTime.class, OffsetTimeStringConverter.create());
        putConverter(converters, OffsetDateTime.class, OffsetDateTimeStringConverter.create());
        putConverter(converters, ZonedDateTime.class, ZonedDateTimeStringConverter.create());
        putConverter(converters, Year.class, YearStringConverter.create());
        putConverter(converters, YearMonth.class, YearMonthStringConverter.create());
        putConverter(converters, MonthDay.class, MonthDayStringConverter.create());
        putConverter(converters, Period.class, PeriodStringConverter.create());
        putConverter(converters, ZoneOffset.class, ZoneOffsetStringConverter.create());
        putConverter(converters, ZoneId.class, ZoneIdStringConverter.create());

        // Other
        putConverter(converters, UUID.class, UuidStringConverter.create());
        putConverter(converters, URL.class, UrlStringConverter.create());
        putConverter(converters, URI.class, UriStringConverter.create());

        this.converters = Collections.unmodifiableMap(converters);
    }

    @Override
    public String toString(Object input) {
        if (input == null) {
            return null;
        }

        return convertToString(input);
    }

    @Override
    public <T> T fromString(Class<T> targetType, String input) {
        if (input == null) {
            return null;
        }

        return convertFromString(targetType, input);
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
