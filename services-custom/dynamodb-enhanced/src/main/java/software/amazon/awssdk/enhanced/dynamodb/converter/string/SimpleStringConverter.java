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

package software.amazon.awssdk.enhanced.dynamodb.converter.string;

import java.util.function.Function;

public class SimpleStringConverter<T> implements StringConverter<T> {
    private final Function<? super T, String> toString;
    private final Function<String, ? extends T> fromString;

    public SimpleStringConverter(Function<? super T, String> toString,
                                 Function<String, ? extends T> fromString) {
        this.toString = toString;
        this.fromString = fromString;
    }

    @Override
    public String toString(T object) {
        return toString.apply(object);
    }

    @Override
    public T fromString(String string) {
        return fromString.apply(string);
    }
}
