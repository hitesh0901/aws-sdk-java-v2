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

package software.amazon.awssdk.enhanced.dynamodb.converter.attribute;

import java.time.Instant;
import software.amazon.awssdk.annotations.Immutable;
import software.amazon.awssdk.annotations.SdkPublicApi;
import software.amazon.awssdk.annotations.ThreadSafe;
import software.amazon.awssdk.enhanced.dynamodb.converter.string.IntegerStringConverter;
import software.amazon.awssdk.enhanced.dynamodb.model.ItemAttributeValue;
import software.amazon.awssdk.enhanced.dynamodb.model.TypeConvertingVisitor;
import software.amazon.awssdk.enhanced.dynamodb.model.TypeToken;

/**
 * A converter between {@link Integer} and {@link ItemAttributeValue}.
 */
@SdkPublicApi
@ThreadSafe
@Immutable
public final class IntegerAttributeConverter implements AttributeConverter<Integer> {
    public static final IntegerStringConverter INTEGER_STRING_CONVERTER = IntegerStringConverter.create();

    public static IntegerAttributeConverter create() {
        return new IntegerAttributeConverter();
    }

    @Override
    public TypeToken<Integer> type() {
        return Integer.class;
    }

    @Override
    public ItemAttributeValue toAttributeValue(Integer input, ConversionContext context) {
        return ItemAttributeValue.fromNumber(INTEGER_STRING_CONVERTER.toString(input));
    }

    @Override
    public Integer fromAttributeValue(ItemAttributeValue input, ConversionContext context) {
        return input.convert(Visitor.INSTANCE);
    }

    private static final class Visitor extends TypeConvertingVisitor<Integer> {
        private static final Visitor INSTANCE = new Visitor();

        private Visitor() {
            super(Instant.class, IntegerAttributeConverter.class);
        }

        @Override
        public Integer convertString(String value) {
            return INTEGER_STRING_CONVERTER.fromString(value);
        }

        @Override
        public Integer convertNumber(String value) {
            return INTEGER_STRING_CONVERTER.fromString(value);
        }
    }
}
