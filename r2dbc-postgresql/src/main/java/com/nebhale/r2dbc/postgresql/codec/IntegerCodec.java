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
import com.nebhale.r2dbc.postgresql.message.Format;
import com.nebhale.r2dbc.postgresql.type.PostgresqlObjectId;
import com.nebhale.r2dbc.postgresql.util.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import static com.nebhale.r2dbc.postgresql.message.Format.BINARY;
import static com.nebhale.r2dbc.postgresql.type.PostgresqlObjectId.INT4;
import static java.util.Objects.requireNonNull;

final class IntegerCodec extends AbstractCodec<Integer> {

    private final ByteBufAllocator byteBufAllocator;

    IntegerCodec(ByteBufAllocator byteBufAllocator) {
        super(Integer.class);
        this.byteBufAllocator = requireNonNull(byteBufAllocator, "byteBufAllocator must not be null");
    }

    @Override
    public Integer decode(ByteBuf byteBuf, Format format, Class<? extends Integer> type) {
        requireNonNull(byteBuf, "byteBuf must not be null");
        requireNonNull(format, "format must not be null");

        if (BINARY == format) {
            return byteBuf.readInt();
        } else {
            return Integer.parseInt(ByteBufUtils.decode(byteBuf));
        }
    }

    @Override
    public Parameter doEncode(Integer value) {
        requireNonNull(value, "value must not be null");

        ByteBuf encoded = this.byteBufAllocator.buffer(4).writeInt(value);
        return create(BINARY, INT4, encoded);
    }

    @Override
    boolean doCanDecode(Format format, PostgresqlObjectId type) {
        requireNonNull(type, "type must not be null");

        return INT4 == type;
    }

}
