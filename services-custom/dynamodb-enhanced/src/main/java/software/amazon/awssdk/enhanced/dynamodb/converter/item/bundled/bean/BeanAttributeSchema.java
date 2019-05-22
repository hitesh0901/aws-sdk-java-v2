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
import software.amazon.awssdk.enhanced.dynamodb.model.TypeToken;
import software.amazon.awssdk.utils.Validate;
import software.amazon.awssdk.utils.builder.CopyableBuilder;
import software.amazon.awssdk.utils.builder.ToCopyableBuilder;

public final class BeanAttributeSchema<B, A>
        implements ToCopyableBuilder<BeanAttributeSchema.Builder<B, A>, BeanAttributeSchema<B, A>> {
    private final TypeToken<B> beanType;
    private final TypeToken<A> attributeType;
    private final String attributeName;
    private final Getter<B, A> getter;
    private final Setter<B, A> setter;
    private final ItemAttributeValueConverter converter;

    private BeanAttributeSchema(Builder<B, A> builder) {
        this.beanType = Validate.notNull(builder.beanType, "beanType");
        this.attributeType = Validate.paramNotNull(builder.attributeType, "setterInputType");
        this.attributeName = Validate.paramNotBlank(builder.attributeName, "attributeName");
        this.getter = Validate.paramNotNull(builder.getter, "getter");
        this.setter = Validate.paramNotNull(builder.setter, "setter");
        this.converter = Validate.paramNotNull(builder.converter, "converter");
    }

    public static <B, A> Builder<B, A> builder(Class<B> beanType, Class<A> attributeType) {
        return builder(TypeToken.from(beanType), TypeToken.from(attributeType));
    }

    public static <B, A> Builder<B, A> builder(TypeToken<B> beanType, TypeToken<A> attributeType) {
        return new Builder<>(beanType, attributeType);
    }

    public TypeToken<A> attributeType() {
        return attributeType;
    }

    public String attributeName() {
        return attributeName;
    }

    public Getter<B, A> getter() {
        return getter;
    }

    public Setter<B, A> setter() {
        return setter;
    }

    public ItemAttributeValueConverter converter() {
        return converter;
    }

    @Override
    public Builder<B, A> toBuilder() {
        return builder(beanType, attributeType).attributeName(attributeName)
                                               .getter(getter)
                                               .setter(setter)
                                               .converter(converter);
    }

    @FunctionalInterface
    public interface Setter<B, A> extends BiConsumer<B, A> {}

    @FunctionalInterface
    public interface Getter<B, A> extends Function<B, A> {}

    public static final class Builder<B, A> implements CopyableBuilder<Builder<B, A>, BeanAttributeSchema<B, A>> {
        private final TypeToken<B> beanType;
        private final TypeToken<A> attributeType;
        private String attributeName;
        private Getter<B, A> getter;
        private Setter<B, A> setter;
        private ItemAttributeValueConverter converter;

        private Builder(TypeToken<B> beanType, TypeToken<A> attributeType) {
            this.beanType = beanType;
            this.attributeType = attributeType;
        }

        public Builder<B, A> attributeName(String attributeName) {
            this.attributeName = attributeName;
            return this;
        }

        public Builder<B, A> getter(Getter<B, A> getter) {
            this.getter = getter;
            return this;
        }

        public Builder<B, A> setter(Setter<B, A> setter) {
            this.setter = setter;
            return this;
        }

        public Builder<B, A> converter(ItemAttributeValueConverter converter) {
            this.converter = converter;
            return this;
        }

        public BeanAttributeSchema<B, A> build() {
            return new BeanAttributeSchema<>(this);
        }
    }
}
