/*
 * Copyright 2017-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nebhale.r2dbc.postgresql.codec;

import com.nebhale.r2dbc.postgresql.client.Parameter;
import io.netty.buffer.ByteBufAllocator;

import java.net.URI;

import static java.util.Objects.requireNonNull;

final class UriCodec extends AbstractCodec<URI> {

    private final StringCodec delegate;

    UriCodec(ByteBufAllocator byteBufAllocator) {
        super(URI.class);
        this.delegate = new StringCodec(byteBufAllocator);
    }

    @Override
    public Parameter doEncode(URI value) {
        requireNonNull(value, "value must not be null");

        return this.delegate.doEncode(value.toString());
    }

}
