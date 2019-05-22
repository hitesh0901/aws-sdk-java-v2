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

package software.amazon.awssdk.enhanced.dynamodb.converter.attribute.bundled;

import software.amazon.awssdk.annotations.Immutable;
import software.amazon.awssdk.annotations.SdkPublicApi;
import software.amazon.awssdk.annotations.ThreadSafe;
import software.amazon.awssdk.enhanced.dynamodb.converter.attribute.ConversionContext;
import software.amazon.awssdk.enhanced.dynamodb.converter.attribute.ExactInstanceOfConverter;
import software.amazon.awssdk.enhanced.dynamodb.model.ItemAttributeValue;
import software.amazon.awssdk.enhanced.dynamodb.model.TypeToken;

/**
 * Identity converter, allowing a customer to specify or request an {@link ItemAttributeValue} directly.
 */
@SdkPublicApi
@ThreadSafe
@Immutable
public final class IdentityConverter extends ExactInstanceOfConverter<ItemAttributeValue> {
    private IdentityConverter() {
        super(ItemAttributeValue.class);
    }

    public static IdentityConverter create() {
        return new IdentityConverter();
    }

    @Override
    protected ItemAttributeValue convertToAttributeValue(ItemAttributeValue input, ConversionContext context) {
        return input;
    }

    @Override
    protected ItemAttributeValue convertFromAttributeValue(ItemAttributeValue input,
                                                           TypeToken<?> desiredType,
                                                           ConversionContext context) {
        return input;
    }
}
