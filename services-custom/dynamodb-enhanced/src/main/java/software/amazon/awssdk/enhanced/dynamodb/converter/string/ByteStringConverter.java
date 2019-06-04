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

import software.amazon.awssdk.enhanced.dynamodb.model.TypeToken;

public class ByteStringConverter implements StringConverter<Byte> {
    private ByteStringConverter() { }

    public static ByteStringConverter create() {
        return new ByteStringConverter();
    }

    @Override
    public TypeToken<Byte> type() {
        return Byte.class;
    }

    @Override
    public Byte fromString(String string) {
        return Byte.valueOf(string);
    }
}
