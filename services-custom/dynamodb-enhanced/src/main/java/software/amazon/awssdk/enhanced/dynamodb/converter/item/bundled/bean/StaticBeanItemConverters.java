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

import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import software.amazon.awssdk.enhanced.dynamodb.converter.item.ItemConverter;
import software.amazon.awssdk.enhanced.dynamodb.model.RequestItem;
import software.amazon.awssdk.enhanced.dynamodb.model.ResponseItem;
import software.amazon.awssdk.enhanced.dynamodb.model.TypeToken;

public class StaticBeanItemConverters implements ItemConverter {
    private final Map<Class<?>, StaticBeanItemConverter> schemas;

    public StaticBeanItemConverters(List<? extends BeanItemSchema> schemas) {
        this.schemas = schemas.stream().collect(toMap(s -> s.beanType().rawClass(), StaticBeanItemConverter::new));
    }

    @Override
    public RequestItem toRequestItem(Object request) {
        return schemas.get(request.getClass()).toRequestItem(request);
    }

    @Override
    public <T> T fromResponseItem(TypeToken<T> targetType, ResponseItem responseItem) {
        return schemas.get(targetType.rawClass()).fromResponseItem(targetType, responseItem);
    }
}
