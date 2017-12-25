/*
 * Copyright 2017-2017 the original author or authors.
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

package com.nebhale.r2dbc.postgresql.message;

import io.netty.buffer.ByteBuf;

import java.util.Arrays;

import static io.netty.util.CharsetUtil.UTF_8;

final class ByteBufUtils {

    static final int MESSAGE_OVERHEAD = 5;

    private static final int LENGTH_PLACEHOLDER = 0;

    private static final byte TERMINAL = 0;

    private ByteBufUtils() {
    }

    static ByteBuf getBody(ByteBuf in) {
        return in.readSlice(in.readInt() - 4);
    }

    static String readCStringUTF8(ByteBuf src) {
        String s = src.readCharSequence(src.bytesBefore(TERMINAL), UTF_8).toString();
        src.readByte();
        return s;
    }

    static ByteBuf writeByte(ByteBuf out, int... values) {
        Arrays.stream(values)
            .forEach(out::writeByte);
        return out;
    }

    static ByteBuf writeBytes(ByteBuf out, ByteBuf in) {
        out.writeBytes(in);
        return out;
    }

    static ByteBuf writeCString(ByteBuf out, ByteBuf in) {
        out.writeBytes(in, in.readerIndex(), in.readableBytes());
        out.writeByte(TERMINAL);
        return out;
    }

    static ByteBuf writeCStringUTF8(ByteBuf out, String s) {
        out.writeCharSequence(s, UTF_8);
        out.writeByte(TERMINAL);
        return out;
    }

    static ByteBuf writeLengthPlaceholder(ByteBuf out) {
        out.writeInt(LENGTH_PLACEHOLDER);
        return out;
    }

    static ByteBuf writeShort(ByteBuf out, int... values) {
        Arrays.stream(values)
            .forEach(out::writeShort);
        return out;
    }

    static ByteBuf writeSize(ByteBuf out) {
        int start = out.readerIndex();
        out.setInt(start, out.writerIndex() - start);
        return out;
    }

}
