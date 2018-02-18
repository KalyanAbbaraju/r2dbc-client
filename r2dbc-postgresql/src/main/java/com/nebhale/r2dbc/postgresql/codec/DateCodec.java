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

import java.util.Date;

import static java.util.Objects.requireNonNull;

final class DateCodec extends AbstractCodec<Date> {

    private final InstantCodec delegate;

    DateCodec(ByteBufAllocator byteBufAllocator) {
        super(Date.class);
        this.delegate = new InstantCodec(byteBufAllocator);
    }

    @Override
    public Parameter doEncode(Date value) {
        requireNonNull(value, "value must not be null");

        return this.delegate.doEncode(value.toInstant());
    }

}
