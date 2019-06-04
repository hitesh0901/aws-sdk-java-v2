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

package software.amazon.awssdk.enhanced.dynamodb.internal.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import software.amazon.awssdk.annotations.SdkInternalApi;
import software.amazon.awssdk.annotations.ThreadSafe;
import software.amazon.awssdk.enhanced.dynamodb.converter.attribute.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.converter.attribute.ConversionContext;
import software.amazon.awssdk.enhanced.dynamodb.converter.attribute.SubtypeAttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.model.ConverterAware;
import software.amazon.awssdk.enhanced.dynamodb.model.ItemAttributeValue;
import software.amazon.awssdk.enhanced.dynamodb.model.TypeToken;
import software.amazon.awssdk.utils.Logger;
import software.amazon.awssdk.utils.Validate;

/**
 * A chain of converters, invoking the underlying converters based on the precedence defined in the
 * {@link AttributeConverter} documentation.
 *
 * <p>
 * Given an input, this will identify a converter that can convert the specific Java type and invoke it. If a converter cannot
 * be found, it will invoke a "parent" converter, which would be expected to be able to convert the value (or throw an exception).
 */
@SdkInternalApi
@ThreadSafe
public final class AttributeConverterChain<T> implements SubtypeAttributeConverter<T> {
    private static final Logger log = Logger.loggerFor(AttributeConverterChain.class);

    private final TypeToken<T> type;

    private final List<SubtypeAttributeConverter<? extends T>> subtypeConverters = new ArrayList<>();

    private final ConcurrentHashMap<Class<? extends T>, AttributeConverterUnion<? extends T>> converterCache =
            new ConcurrentHashMap<>();

    /**
     * The "default converter" to invoke if no converters can be found in this chain supporting a specific type.
     */
    private final SubtypeAttributeConverter<? super T> parent;

    private AttributeConverterChain(Builder<T> builder) {
        this.type = builder.type;
        this.parent = builder.parent;
    }

    public static Builder<Object> builder() {
        return new Builder<>(TypeToken.from(Object.class));
    }

    public static <T> Builder<T> builder(TypeToken<T> typeBound) {
        return new Builder<>(typeBound);
    }

    /**
     * A simplified way of invoking {@code builder().addAll(converters).build()}.
     */
    public static <T> AttributeConverterChain<T> create(TypeToken<T> upperBound, ConverterAware<T> converters) {
        return builder(upperBound).addConverters(converters.converters())
                                  .addSubtypeConverters(converters.subtypeConverters())
                                  .build();
    }

    @Override
    public TypeToken<T> type() {
        return type;
    }

    @Override
    public ItemAttributeValue toAttributeValue(T input, ConversionContext context) {
        // TODO: Is this safe? It's not technically true...
        return findRequiredConverter((Class<T>) input.getClass())
                .apply(c -> c.toAttributeValue(input, context), c -> c.toAttributeValue(input, context));
    }

    @Override
    public <U extends T> U fromAttributeValue(ItemAttributeValue input, TypeToken<U> desiredType, ConversionContext context) {
        return findRequiredConverter(desiredType.rawClass())
                .apply(c -> c.fromAttributeValue(input, context), c -> c.fromAttributeValue(input, desiredType, context));
    }

    /**
     * Find a converter that matches the provided type. If one cannot be found, throw an exception.
     */
    private <U extends T> AttributeConverterUnion<U> findRequiredConverter(Class<U> type) {
        return findConverter(type).orElseThrow(() -> new IllegalStateException("Converter not found for " + type));
    }

    /**
     * Find a converter that matches the provided type. If one cannot be found, return empty.
     */
    private <U extends T> Optional<AttributeConverterUnion<U>> findConverter(Class<U> type) {
        log.debug(() -> "Loading converter for " + type + ".");

        @SuppressWarnings("unchecked") // We initialized correctly, so this is safe.
        AttributeConverterUnion<U> converter = (AttributeConverterUnion<U>) converterCache.get(type);
        if (converter != null) {
            return Optional.of(converter);
        }

        log.debug(() -> "Converter not cached for " + type + ". Checking for a subtype converter match.");

        converter = findSubtypeConverter(type).orElse(null);

        if (converter == null && parent != null) {
            log.debug(() -> "Converter not found in this chain for " + type + ". Parent will be used.");
            converter = new AttributeConverterUnion<>(parent);
        }

        if (converter != null && shouldCache(type)) {
            this.converterCache.put(type, converter);
        }

        return Optional.ofNullable(converter);
    }

    private boolean shouldCache(Class<?> type) {
        // Do not cache anonymous classes, to prevent memory leaks.
        return !type.isAnonymousClass();
    }

    private <U extends T> Optional<AttributeConverterUnion<U>> findSubtypeConverter(Class<U> type) {
        for (SubtypeAttributeConverter<? extends T> subtypeConverter : subtypeConverters) {
            if (subtypeConverter.type().rawClass().isAssignableFrom(type)) {
                SubtypeAttributeConverter<U> result = (SubtypeAttributeConverter<U>) subtypeConverter;
                return Optional.of(new AttributeConverterUnion<>(result));
            }
        }

        return Optional.empty();
    }

    private static class AttributeConverterUnion<U> {
        private final AttributeConverter<U> converter;
        private final SubtypeAttributeConverter<? super U> subtypeConverter;

        private AttributeConverterUnion(AttributeConverter<U> converter) {
            this.converter = converter;
            this.subtypeConverter = null;
        }

        private AttributeConverterUnion(SubtypeAttributeConverter<? super U> converter) {
            this.converter = null;
            this.subtypeConverter = converter;
        }

        public <V> V apply(Function<AttributeConverter<U>, V> converterFunction,
                           Function<SubtypeAttributeConverter<? super U>, V> subtypeConverterFunction) {
            if (converter != null) {
                return converterFunction.apply(converter);
            } else if (subtypeConverter != null){
                return subtypeConverterFunction.apply(subtypeConverter);
            } else {
                throw new IllegalStateException();
            }
        }
    }

    public static class Builder<T> implements ConverterAware.Builder<T> {
        private final TypeToken<T> type;
        private List<AttributeConverter<? extends T>> converters = new ArrayList<>();
        private List<SubtypeAttributeConverter<? extends T>> subtypeConverters = new ArrayList<>();
        private SubtypeAttributeConverter<? super T> parent;

        private Builder(TypeToken<T> type) {
            this.type = type;
        }

        @Override
        public Builder<T> addConverters(Collection<? extends AttributeConverter<? extends T>> converters) {
            Validate.paramNotNull(converters, "converters");
            Validate.noNullElements(converters, "Converters must not contain null members.");
            this.converters.addAll(converters);
            return this;
        }

        @Override
        public Builder<T> addConverter(AttributeConverter<? extends T> converter) {
            Validate.paramNotNull(converter, "converter");
            this.converters.add(converter);
            return this;
        }

        @Override
        public Builder<T> addSubtypeConverters(Collection<? extends SubtypeAttributeConverter<? extends T>> converters) {
            Validate.paramNotNull(converters, "converters");
            Validate.noNullElements(converters, "Converters must not contain null members.");
            this.subtypeConverters.addAll(converters);
            return this;
        }

        @Override
        public Builder<T> addSubtypeConverter(SubtypeAttributeConverter<? extends T> converter) {
            Validate.paramNotNull(converter, "converter");
            this.subtypeConverters.add(converter);
            return this;
        }

        @Override
        public Builder<T> clearConverters() {
            this.converters.clear();
            return this;
        }

        @Override
        public Builder<T> clearSubtypeConverters() {
            this.subtypeConverters.clear();
            return this;
        }

        public Builder<T> parent(SubtypeAttributeConverter<? super T> parent) {
            Validate.paramNotNull(parent, "parent");
            this.parent = parent;
            return this;
        }

        public AttributeConverterChain<T> build() {
            return new AttributeConverterChain<>(this);
        }
    }
}
