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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import software.amazon.awssdk.enhanced.dynamodb.model.TypeToken;
import software.amazon.awssdk.utils.Validate;
import software.amazon.awssdk.utils.builder.CopyableBuilder;
import software.amazon.awssdk.utils.builder.ToCopyableBuilder;

public final class BeanItemSchema<B> implements ToCopyableBuilder<BeanItemSchema.Builder<B>, BeanItemSchema<B>> {
    private final TypeToken<B> beanType;
    private final Supplier<? extends B> constructor;
    private final Map<String, BeanAttributeSchema<B, ?>> attributeSchemas;

    private BeanItemSchema(Builder<B> builder) {
        this.beanType = Validate.paramNotNull(builder.beanType, "beanType");
        this.constructor = Validate.paramNotNull(builder.constructor, "constructor");
        this.attributeSchemas = builder.attributeSchemas.stream().collect(Collectors.toMap(s -> s.attributeName(), s -> s));
    }

    public static <B> Builder<B> builder(Class<B> beanType) {
        return new Builder<>(beanType);
    }

    public static <B> Builder<B> builder(TypeToken<B> beanType) {
        return new Builder<>(beanType);
    }

    public TypeToken<B> beanType() {
        return beanType;
    }

    public Supplier<? extends B> constructor() {
        return constructor;
    }

    public BeanAttributeSchema<B, ?> attributeSchema(String attributeName) {
        return attributeSchemas.get(attributeName);
    }

    public Collection<BeanAttributeSchema<B, ?>> attributeSchemas() {
        return Collections.unmodifiableCollection(attributeSchemas.values());
    }

    @Override
    public Builder<B> toBuilder() {
        return builder(beanType).constructor(constructor)
                                .addAttributeSchemas(attributeSchemas.values());
    }

    public static final class Builder<B> implements CopyableBuilder<BeanItemSchema.Builder<B>, BeanItemSchema<B>> {
        private final TypeToken<B> beanType;
        private Supplier<? extends B> constructor;
        private Collection<BeanAttributeSchema<B, ?>> attributeSchemas = new ArrayList<>();

        private Builder(Class<B> beanType) {
            this(TypeToken.from(beanType));
        }

        private Builder(TypeToken<B> beanType) {
            this.beanType = beanType;
        }

        public Builder<B> constructor(Supplier<? extends B> constructor) {
            this.constructor = constructor;
            return this;
        }

        public Builder<B> addAttributeSchemas(Collection<? extends BeanAttributeSchema<B, ?>> attributeSchemas) {
            Validate.paramNotNull(attributeSchemas, "attributeSchemas");
            Validate.noNullElements(attributeSchemas, "Attribute schemas must not be null.");
            this.attributeSchemas.addAll(attributeSchemas);
            return this;
        }

        public Builder<B> addAttributeSchema(BeanAttributeSchema<B, ?> attributeSchema) {
            Validate.paramNotNull(attributeSchema, "attributeSchema");
            this.attributeSchemas.add(attributeSchema);
            return this;
        }

        public <A> Builder<B> addAttributeSchema(Class<A> attributeType,
                                                 Consumer<? super BeanAttributeSchema.Builder<B, A>> attributeSchemaConsumer) {
            return addAttributeSchema(TypeToken.from(attributeType), attributeSchemaConsumer);
        }

        public <A> Builder<B> addAttributeSchema(TypeToken<A> attributeType,
                                                 Consumer<? super BeanAttributeSchema.Builder<B, A>> attributeSchemaConsumer) {
            Validate.paramNotNull(attributeSchemaConsumer, "attributeSchemaConsumer");
            BeanAttributeSchema.Builder<B, A> schemaBuilder = BeanAttributeSchema.builder(beanType, attributeType);
            attributeSchemaConsumer.accept(schemaBuilder);
            return addAttributeSchema(schemaBuilder.build());
        }

        public Builder<B> clearAttributeSchemas() {
            this.attributeSchemas.clear();
            return this;
        }

        public BeanItemSchema<B> build() {
            return new BeanItemSchema<>(this);
        }
    }
}
