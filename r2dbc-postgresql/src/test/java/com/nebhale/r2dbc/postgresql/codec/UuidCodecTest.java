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
import com.nebhale.r2dbc.postgresql.type.PostgresqlObjectId;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Test;

import java.util.UUID;

import static com.nebhale.r2dbc.postgresql.message.Format.BINARY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public final class UuidCodecTest {

    @Test
    public void constructorNoByteBufAllocator() {
        assertThatNullPointerException().isThrownBy(() -> new UuidCodec(null))
            .withMessage("byteBufAllocator must not be null");
    }

    @Test
    public void doEncode() {
        UUID uuid = UUID.randomUUID();

        assertThat(new UuidCodec(UnpooledByteBufAllocator.DEFAULT).doEncode(uuid))
            .isEqualTo(new Parameter(BINARY, PostgresqlObjectId.UUID.getObjectId(), Unpooled.buffer().writeLong(uuid.getMostSignificantBits()).writeLong(uuid.getLeastSignificantBits())));
    }

    @Test
    public void doEncodeNoValue() {
        assertThatNullPointerException().isThrownBy(() -> new UuidCodec(UnpooledByteBufAllocator.DEFAULT).doEncode(null))
            .withMessage("value must not be null");
    }

}
