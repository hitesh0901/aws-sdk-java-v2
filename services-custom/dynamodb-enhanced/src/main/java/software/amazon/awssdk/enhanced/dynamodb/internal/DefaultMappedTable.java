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

package software.amazon.awssdk.enhanced.dynamodb.internal;

import software.amazon.awssdk.annotations.SdkInternalApi;
import software.amazon.awssdk.annotations.ThreadSafe;
import software.amazon.awssdk.enhanced.dynamodb.MappedTable;
import software.amazon.awssdk.enhanced.dynamodb.Table;
import software.amazon.awssdk.enhanced.dynamodb.converter.item.ItemConverter;
import software.amazon.awssdk.enhanced.dynamodb.model.ResponseItem;
import software.amazon.awssdk.enhanced.dynamodb.model.TypeToken;
import software.amazon.awssdk.utils.builder.Buildable;

/**
 * The default implementation of {@link Table}.
 */
@SdkInternalApi
@ThreadSafe
public class DefaultMappedTable implements MappedTable {
    private final Table table;
    private final ItemConverter converter;

    private DefaultMappedTable(Builder builder) {
        this.table = builder.table;
        this.converter = builder.converter;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String name() {
        return table.name();
    }

    @Override
    public <T extends U, U> T getItem(Class<T> outputType, U key) {
        ResponseItem responseItem = table.getItem(converter.toRequestItem(key));
        return converter.fromResponseItem(TypeToken.from(outputType), responseItem);
    }

    @Override
    public void putItem(Object item) {
        table.putItem(converter.toRequestItem(item));
    }

    public static class Builder implements Buildable {
        private Table table;
        private ItemConverter converter;

        public Builder table(Table table) {
            this.table = table;
            return this;
        }

        public Builder converter(ItemConverter converter) {
            this.converter = converter;
            return this;
        }

        @Override
        public DefaultMappedTable build() {
            return new DefaultMappedTable(this);
        }
    }
}
