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

import software.amazon.awssdk.annotations.SdkPublicApi;
import software.amazon.awssdk.annotations.ThreadSafe;
import software.amazon.awssdk.enhanced.dynamodb.model.ItemAttributeValue;
import software.amazon.awssdk.enhanced.dynamodb.model.TypeToken;
import software.amazon.awssdk.utils.Validate;

/**
 * A base class that simplifies the process of implementing an {@link AttributeConverter} with the
 * {@link ConversionCondition#isInstanceOf(Class)} conversion type. This handles casting to/from the mapped type and
 * validates that the converter is being invoked with the correct types.
 */
@SdkPublicApi
@ThreadSafe
public abstract class InstanceOfAttributeConverter<T> implements AttributeConverter {
    private final Class<T> type;

    protected InstanceOfAttributeConverter(Class<?> type) {
        this.type = (Class<T>) type;
    }

    @Override
    public final ConversionCondition conversionCondition() {
        return ConversionCondition.isInstanceOf(type);
    }

    @Override
    public final ItemAttributeValue toAttributeValue(Object input, ConversionContext context) {
        T typedInput = Validate.isInstanceOf(type, input,
                                             "Input type %s could not be converted to a %s.",
                                             input.getClass(), type);

        return convertToAttributeValue(typedInput, context);
    }

    @Override
    public final Object fromAttributeValue(ItemAttributeValue input, TypeToken<?> desiredType, ConversionContext context) {
        Validate.isAssignableFrom(type, desiredType.rawClass(),
                                  "Requested type %s is not a subtype of %s.",
                                  desiredType, type);

        return convertFromAttributeValue(input, desiredType, context);
    }

    public TypeToken type() {
        return type;
    }

    protected abstract ItemAttributeValue convertToAttributeValue(T input,
                                                                  ConversionContext conversionContext);

    protected abstract T convertFromAttributeValue(ItemAttributeValue input,
                                                   TypeToken<?> desiredType,
                                                   ConversionContext context);
}
