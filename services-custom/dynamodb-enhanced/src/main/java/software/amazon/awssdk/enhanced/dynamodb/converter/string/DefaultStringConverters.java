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
import java.net.MalformedURLException;
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
import java.util.function.Function;
import software.amazon.awssdk.utils.BinaryUtils;
import software.amazon.awssdk.utils.Validate;

public class DefaultStringConverters implements StringConverters {
    private static DefaultStringConverters INSTANCE = new DefaultStringConverters();

    private final Map<Class<Object>, StringConverter<Object>> converters;

    public static StringConverters instance() {
        return INSTANCE;
    }

    private DefaultStringConverters() {
        Map<Class<Object>, StringConverter<Object>> converters = new HashMap<>();

        // Primitive Types
        addConverter(converters, boolean.class, Object::toString, this::stringToBoolean);
        addConverter(converters, short.class, Object::toString, Short::valueOf);
        addConverter(converters, int.class, Object::toString, Integer::valueOf);
        addConverter(converters, long.class, Object::toString, Long::valueOf);
        addConverter(converters, float.class, Object::toString, Float::valueOf);
        addConverter(converters, double.class, Object::toString, Double::valueOf);
        addConverter(converters, char.class, Object::toString, this::stringToChar);
        addConverter(converters, byte.class, Object::toString, Byte::valueOf);

        // Primitive Array Types
        addConverter(converters, byte[].class, BinaryUtils::toBase64, BinaryUtils::fromBase64);
        addConverter(converters, char[].class, String::new, String::toCharArray);

        // Boxed Primitive Types
        addConverter(converters, Boolean.class, Object::toString, this::stringToBoolean);
        addConverter(converters, Short.class, Object::toString, Short::valueOf);
        addConverter(converters, Integer.class, Object::toString, Integer::valueOf);
        addConverter(converters, Long.class, Object::toString, Long::valueOf);
        addConverter(converters, Float.class, Object::toString, Float::valueOf);
        addConverter(converters, Double.class, Object::toString, Double::valueOf);
        addConverter(converters, Character.class, Object::toString, this::stringToChar);
        addConverter(converters, Byte.class, Object::toString, Byte::valueOf);

        // String Types
        addConverter(converters, String.class, Function.identity(), Function.identity());
        addConverter(converters, CharSequence.class, Object::toString, Function.identity());
        addConverter(converters, StringBuffer.class, Object::toString, StringBuffer::new);
        addConverter(converters, StringBuilder.class, Object::toString, StringBuilder::new);

        // Number Types
        addConverter(converters, BigInteger.class, Object::toString, BigInteger::new);
        addConverter(converters, BigDecimal.class, Object::toString, BigDecimal::new);

        // Atomic Types
        addConverter(converters, AtomicLong.class, Object::toString, s -> new AtomicLong(Long.parseLong(s)));
        addConverter(converters, AtomicInteger.class, Object::toString, s -> new AtomicInteger(Integer.parseInt(s)));
        addConverter(converters, AtomicBoolean.class, Object::toString, s -> new AtomicBoolean(stringToBoolean(s)));

        // Optional Types
        addConverter(converters, OptionalInt.class, this::optionalIntToString, this::stringToOptionalInt);
        addConverter(converters, OptionalLong.class, this::optionalLongToString, this::stringToOptionalLong);
        addConverter(converters, OptionalDouble.class, this::optionalDoubleToString, this::stringToOptionalDouble);

        // Time Types
        addConverter(converters, Instant.class, Object::toString, Instant::parse);
        addConverter(converters, Duration.class, Object::toString, Duration::parse);
        addConverter(converters, LocalDate.class, Object::toString, LocalDate::parse);
        addConverter(converters, LocalTime.class, Object::toString, LocalTime::parse);
        addConverter(converters, LocalDateTime.class, Object::toString, LocalDateTime::parse);
        addConverter(converters, OffsetTime.class, Object::toString, OffsetTime::parse);
        addConverter(converters, OffsetDateTime.class, Object::toString, OffsetDateTime::parse);
        addConverter(converters, ZonedDateTime.class, Object::toString, ZonedDateTime::parse);
        addConverter(converters, Year.class, Object::toString, Year::parse);
        addConverter(converters, YearMonth.class, Object::toString, YearMonth::parse);
        addConverter(converters, MonthDay.class, Object::toString, MonthDay::parse);
        addConverter(converters, Period.class, Object::toString, Period::parse);
        addConverter(converters, ZoneOffset.class, Object::toString, ZoneOffset::of);
        addConverter(converters, ZoneId.class, Object::toString, ZoneId::of);

        // Other
        addConverter(converters, UUID.class, Object::toString, UUID::fromString);
        addConverter(converters, URL.class, Object::toString, this::stringToUrl);
        addConverter(converters, URI.class, Object::toString, URI::create);

        this.converters = Collections.unmodifiableMap(converters);
    }

    private String optionalIntToString(OptionalInt input) {
        if (!input.isPresent()) {
            return null;
        }
        return Integer.toString(input.getAsInt());
    }

    private OptionalInt stringToOptionalInt(String input) {
        if (input == null) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(Integer.parseInt(input));
    }

    private String optionalLongToString(OptionalLong input) {
        if (!input.isPresent()) {
            return null;
        }
        return Long.toString(input.getAsLong());
    }

    private OptionalLong stringToOptionalLong(String input) {
        if (input == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(Long.parseLong(input));
    }

    private String optionalDoubleToString(OptionalDouble input) {
        if (!input.isPresent()) {
            return null;
        }
        return Double.toString(input.getAsDouble());
    }

    private OptionalDouble stringToOptionalDouble(String input) {
        if (input == null) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(Double.parseDouble(input));
    }

    private Character stringToChar(String input) {
        Validate.isTrue(input.length() == 1, "Character string was not of length 1: %s", input);
        return input.charAt(0);
    }

    private boolean stringToBoolean(String input) {
        switch (input) {
            case "true": return true;
            case "false": return false;
            default: throw new IllegalArgumentException("Boolean string was not 'true' or 'false': " + input);
        }
    }

    private URL stringToUrl(String input) {
        try {
            return new URL(input);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("URL format was incorrect: " + input, e);
        }
    }

    @Override
    public String toString(Object input) {
        if (input == null) {
            return null;
        }

        return getConverter(input.getClass()).toString(input);
    }

    @Override
    public <T> T fromString(Class<T> targetType, String input) {
        if (input == null) {
            return null;
        }

        return targetType.cast(getConverter(targetType).fromString(input));
    }

    private StringConverter<Object> getConverter(Class<?> clazz) {
        StringConverter<Object> converter = converters.get(clazz);
        if (converter == null) {
            throw new IllegalArgumentException("No string converter exists for " + clazz);
        }
        return converter;
    }

    @SuppressWarnings("unchecked") // The method signature ensures this is safe
    private <T> void addConverter(Map<Class<Object>, StringConverter<Object>> converters,
                                  Class<T> convertedType,
                                  Function<T, String> toString,
                                  Function<String, ? extends T> fromString) {
        converters.put((Class<Object>) convertedType,
                       (SimpleStringConverter<Object>) new SimpleStringConverter<>(toString, fromString));
    }

    private static final class SimpleStringConverter<T> implements StringConverter<T> {
        private final Function<T, String> toString;
        private final Function<String, ? extends T> fromString;

        public SimpleStringConverter(Function<T, String> toString,
                                     Function<String, ? extends T> fromString) {
            this.toString = toString;
            this.fromString = fromString;
        }

        @Override
        public String toString(T object) {
            return toString.apply(object);
        }

        @Override
        public T fromString(String string) {
            return fromString.apply(string);
        }
    }
}
