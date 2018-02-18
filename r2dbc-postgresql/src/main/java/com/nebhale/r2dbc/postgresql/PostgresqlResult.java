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

package com.nebhale.r2dbc.postgresql;

import com.nebhale.r2dbc.postgresql.codec.Codecs;
import com.nebhale.r2dbc.postgresql.message.backend.BackendMessage;
import com.nebhale.r2dbc.postgresql.message.backend.CommandComplete;
import com.nebhale.r2dbc.postgresql.message.backend.DataRow;
import com.nebhale.r2dbc.postgresql.message.backend.RowDescription;
import com.nebhale.r2dbc.spi.Result;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.util.Objects.requireNonNull;

/**
 * An implementation of {@link Result} representing the results of a query against a PostgreSQL database.
 */
public final class PostgresqlResult implements Result {

    private final Codecs codecs;

    private final Mono<PostgresqlRowMetadata> rowMetadata;

    private final Flux<PostgresqlRow> rows;

    private final Mono<Integer> rowsUpdated;

    PostgresqlResult(Codecs codecs, Mono<PostgresqlRowMetadata> rowMetadata, Flux<PostgresqlRow> rows, Mono<Integer> rowsUpdated) {
        this.codecs = requireNonNull(codecs, "codecs must not be null");
        this.rowMetadata = requireNonNull(rowMetadata, "rowMetadata must not be null");
        this.rows = requireNonNull(rows, "rows must not be null");
        this.rowsUpdated = requireNonNull(rowsUpdated, "rowsUpdated must not be null");
    }

    @Override
    public Mono<PostgresqlRowMetadata> getRowMetadata() {
        return this.rowMetadata;
    }

    @Override
    public Flux<PostgresqlRow> getRows() {
        return this.rows;
    }

    @Override
    public Mono<Integer> getRowsUpdated() {
        return this.rowsUpdated;
    }

    @Override
    public String toString() {
        return "PostgresqlResult{" +
            "codecs=" + this.codecs +
            ", rowMetadata=" + this.rowMetadata +
            ", rows=" + this.rows +
            ", rowsUpdated=" + this.rowsUpdated +
            '}';
    }

    static PostgresqlResult toResult(Codecs codecs, Flux<BackendMessage> messages) {
        requireNonNull(codecs, "codecs must not be null");
        requireNonNull(messages, "messages must not be null");

        EmitterProcessor<BackendMessage> processor = EmitterProcessor.create(false);
        Flux<BackendMessage> firstMessages = processor.take(3).cache();

        Mono<PostgresqlRowMetadata> rowMetadata = firstMessages
            .ofType(RowDescription.class)
            .singleOrEmpty()
            .map(PostgresqlRowMetadata::toRowMetadata);

        Flux<PostgresqlRow> rows = processor
            .startWith(firstMessages)
            .ofType(DataRow.class)
            .map(dataRow -> PostgresqlRow.toRow(codecs, dataRow));

        Mono<Integer> rowsUpdated = firstMessages
            .ofType(CommandComplete.class)
            .singleOrEmpty()
            .flatMap(commandComplete -> Mono.justOrEmpty(commandComplete.getRows()));

        messages
            .handle(PostgresqlServerErrorException::handleErrorResponse)
            .subscribe(processor);

        return new PostgresqlResult(codecs, rowMetadata, rows, rowsUpdated);
    }

}
