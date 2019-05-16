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

import java.util.function.BiConsumer;
import java.util.function.Function;
import software.amazon.awssdk.enhanced.dynamodb.converter.attribute.ItemAttributeValueConverter;
import software.amazon.awssdk.enhanced.dynamodb.converter.item.ItemConverter;
import software.amazon.awssdk.enhanced.dynamodb.model.TypeToken;
import software.amazon.awssdk.utils.Either;
import software.amazon.awssdk.utils.Validate;
import software.amazon.awssdk.utils.builder.CopyableBuilder;
import software.amazon.awssdk.utils.builder.ToCopyableBuilder;

public final class BeanAttributeSchema implements ToCopyableBuilder<BeanAttributeSchema.Builder, BeanAttributeSchema> {
    private final String attributeName;
    private final Getter getter;
    private final Setter setter;
    private final TypeToken<?> setterInputType;
    private final Either<ItemAttributeValueConverter, ItemConverter> converter;

    private BeanAttributeSchema(Builder builder) {
        this.attributeName = Validate.paramNotBlank(builder.attributeName, "attributeName");
        this.getter = Validate.paramNotNull(builder.getter, "getter");
        this.setter = Validate.paramNotNull(builder.setter, "setter");
        this.setterInputType = Validate.paramNotNull(builder.setterInputType, "setterInputType");

        Validate.isTrue(builder.itemAttributeValueConverter == null || builder.itemConverter == null,
                        "Only one converter type can be specified.");

        this.converter = Either.fromNullable(builder.itemAttributeValueConverter, builder.itemConverter)
                               .orElseThrow(() -> new IllegalStateException("Converter must not be null."));
    }

    public static Builder builder() {
        return new Builder();
    }

    public String attributeName() {
        return attributeName;
    }

    public Getter getter() {
        return getter;
    }

    public Setter setter() {
        return setter;
    }

    public TypeToken<?> setterInputType() {
        return setterInputType;
    }

    public Either<ItemAttributeValueConverter, ItemConverter> converter() {
        return converter;
    }

    @Override
    public Builder toBuilder() {
        return builder().attributeName(attributeName).getter(getter).setter(setter);
    }

    @FunctionalInterface
    public interface Setter extends BiConsumer<Object, Object> {}

    @FunctionalInterface
    public interface Getter extends Function<Object, Object> {}

    public static final class Builder implements CopyableBuilder<Builder, BeanAttributeSchema> {
        private String attributeName;
        private Getter getter;
        private Setter setter;
        private TypeToken<?> setterInputType;
        private ItemAttributeValueConverter itemAttributeValueConverter;
        private ItemConverter itemConverter;

        private Builder() {}

        public Builder attributeName(String attributeName) {
            this.attributeName = attributeName;
            return this;
        }

        public Builder getter(Getter getter) {
            this.getter = getter;
            return this;
        }

        public Builder setter(Setter setter) {
            this.setter = setter;
            return this;
        }

        public Builder setterInputType(TypeToken<?> setterInputType) {
            this.setterInputType = setterInputType;
            return this;
        }

        public Builder converter(ItemAttributeValueConverter itemAttributeValueConverter) {
            this.itemAttributeValueConverter = itemAttributeValueConverter;
            return this;
        }

        public Builder converter(ItemConverter itemConverter) {
            this.itemConverter = itemConverter;
            return this;
        }

        public BeanAttributeSchema build() {
            return new BeanAttributeSchema(this);
        }
    }
}
