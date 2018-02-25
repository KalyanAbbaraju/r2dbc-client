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

import com.nebhale.r2dbc.postgresql.client.Client;
import com.nebhale.r2dbc.postgresql.client.SimpleQueryMessageFlow;
import com.nebhale.r2dbc.postgresql.codec.Codecs;
import com.nebhale.r2dbc.postgresql.message.backend.CommandComplete;
import com.nebhale.r2dbc.postgresql.message.backend.EmptyQueryResponse;
import com.nebhale.r2dbc.postgresql.message.backend.ErrorResponse;
import reactor.core.publisher.Flux;

import static com.nebhale.r2dbc.postgresql.client.ExtendedQueryMessageFlow.PARAMETER_SYMBOL;
import static com.nebhale.r2dbc.postgresql.util.PredicateUtils.or;
import static java.util.Objects.requireNonNull;

final class SimpleQueryPostgresqlStatement implements PostgresqlStatement {

    private final Client client;

    private final Codecs codecs;

    private final String sql;

    SimpleQueryPostgresqlStatement(Client client, Codecs codecs, String sql) {
        this.client = requireNonNull(client, "client must not be null");
        this.codecs = requireNonNull(codecs, "codecs must not be null");
        this.sql = requireNonNull(sql, "sql must not be null");
    }

    @Override
    public SimpleQueryPostgresqlStatement add() {
        return this;
    }

    @Override
    public SimpleQueryPostgresqlStatement bind(Object identifier, Object value) {
        throw getUnsupportedOperationException();
    }

    @Override
    public SimpleQueryPostgresqlStatement bind(Integer index, Object value) {
        throw getUnsupportedOperationException();
    }

    @Override
    public SimpleQueryPostgresqlStatement bindNull(Object identifier, Object type) {
        throw getUnsupportedOperationException();
    }

    @Override
    public Flux<PostgresqlResult> execute() {
        return SimpleQueryMessageFlow
            .exchange(this.client, this.sql)
            .windowUntil(or(CommandComplete.class::isInstance, EmptyQueryResponse.class::isInstance, ErrorResponse.class::isInstance))
            .map(dataRow -> PostgresqlResult.toResult(this.codecs, dataRow));
    }

    @Override
    public String toString() {
        return "SimpleQueryPostgresqlStatement{" +
            "client=" + this.client +
            ", codecs=" + this.codecs +
            ", sql='" + this.sql + '\'' +
            '}';
    }

    static boolean supports(String sql) {
        requireNonNull(sql, "sql must not be null");

        return sql.trim().isEmpty() || !PARAMETER_SYMBOL.matcher(sql).matches();
    }

    private UnsupportedOperationException getUnsupportedOperationException() {
        return new UnsupportedOperationException(String.format("Binding parameters is not supported for the statement '%s'", this.sql));
    }

}
