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

public final class BeanItemSchema implements ToCopyableBuilder<BeanItemSchema.Builder, BeanItemSchema> {
    private final TypeToken<?> beanType;
    private final Supplier<Object> constructor;
    private final Map<String, BeanAttributeSchema> attributeSchemas;

    private BeanItemSchema(Builder builder) {
        this.beanType = Validate.paramNotNull(builder.beanType, "beanType");
        this.constructor = Validate.paramNotNull(builder.constructor, "constructor");
        this.attributeSchemas = builder.attributeSchemas.stream().collect(Collectors.toMap(s -> s.attributeName(), s -> s));
    }

    public static Builder builder() {
        return new Builder();
    }

    public TypeToken<?> beanType() {
        return beanType;
    }

    public Supplier<Object> constructor() {
        return constructor;
    }

    public BeanAttributeSchema attributeSchema(String attributeName) {
        return attributeSchemas.get(attributeName);
    }

    public Collection<BeanAttributeSchema> attributeSchemas() {
        return Collections.unmodifiableCollection(attributeSchemas.values());
    }

    @Override
    public Builder toBuilder() {
        return builder().beanType(beanType).constructor(constructor).addAttributeSchemas(attributeSchemas.values());
    }

    public static final class Builder implements CopyableBuilder<BeanItemSchema.Builder, BeanItemSchema> {
        private TypeToken<?> beanType;
        private Supplier<Object> constructor;
        private Collection<BeanAttributeSchema> attributeSchemas = new ArrayList<>();

        private Builder() {}

        public Builder beanType(TypeToken<?> beanType) {
            this.beanType = beanType;
            return this;
        }

        public Builder constructor(Supplier<Object> constructor) {
            this.constructor = constructor;
            return this;
        }

        public Builder addAttributeSchemas(Collection<? extends BeanAttributeSchema> attributeSchemas) {
            Validate.paramNotNull(attributeSchemas, "attributeSchemas");
            Validate.noNullElements(attributeSchemas, "Attribute schemas must not be null.");
            this.attributeSchemas.addAll(attributeSchemas);
            return this;
        }

        public Builder addAttributeSchema(BeanAttributeSchema attributeSchema) {
            Validate.paramNotNull(attributeSchema, "attributeSchema");
            this.attributeSchemas.add(attributeSchema);
            return this;
        }

        public Builder addAttributeSchema(Consumer<BeanAttributeSchema.Builder> attributeSchemaConsumer) {
            Validate.paramNotNull(attributeSchemaConsumer, "attributeSchemaConsumer");
            BeanAttributeSchema.Builder schemaBuilder = BeanAttributeSchema.builder();
            attributeSchemaConsumer.accept(schemaBuilder);
            return addAttributeSchema(schemaBuilder.build());
        }

        public Builder clearAttributeSchemas() {
            this.attributeSchemas.clear();
            return this;
        }

        public BeanItemSchema build() {
            return new BeanItemSchema(this);
        }
    }
}
