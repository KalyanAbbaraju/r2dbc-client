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

import com.nebhale.r2dbc.ConnectionFactory;
import com.nebhale.r2dbc.postgresql.authentication.AuthenticationHandler;
import com.nebhale.r2dbc.postgresql.authentication.PasswordAuthenticationHandler;
import com.nebhale.r2dbc.postgresql.client.Client;
import com.nebhale.r2dbc.postgresql.client.ReactorNettyClient;
import com.nebhale.r2dbc.postgresql.client.StartupMessageFlow;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * An implementation of {@link ConnectionFactory} for creating connections to a PostgreSQL database.
 */
public final class PostgresqlConnectionFactory implements ConnectionFactory {

    private final Supplier<Client> clientFactory;

    private final PostgresqlConnectionConfiguration configuration;

    /**
     * Creates a new connection factory.
     *
     * @param configuration the configuration to use connections
     * @throws NullPointerException if {@code configuration} is {@code null}
     */
    public PostgresqlConnectionFactory(PostgresqlConnectionConfiguration configuration) {
        this(() -> new ReactorNettyClient(configuration.getHost(), configuration.getPort()), configuration);
    }

    PostgresqlConnectionFactory(Supplier<Client> clientFactory, PostgresqlConnectionConfiguration configuration) {
        this.clientFactory = Objects.requireNonNull(clientFactory, "clientFactory must not be null");
        this.configuration = Objects.requireNonNull(configuration, "configuration must not be null");
    }

    @Override
    public Mono<PostgresqlConnection> create() {
        return Mono.just(this.clientFactory.get())
            .delayUntil(client ->
                StartupMessageFlow
                    .exchange(this.configuration.getApplicationName(), getAuthenticationHandler(this.configuration), client, this.configuration.getDatabase(), this.configuration.getUsername()))
            .map(client -> new PostgresqlConnection(client, DefaultPortalNameSupplier.INSTANCE, new IndefiniteStatementCache(client)));
    }

    @Override
    public String toString() {
        return "PostgresqlConnectionFactory{" +
            "clientFactory=" + this.clientFactory +
            ", configuration=" + this.configuration +
            '}';
    }

    private AuthenticationHandler getAuthenticationHandler(PostgresqlConnectionConfiguration configuration) {
        return new PasswordAuthenticationHandler(configuration.getPassword(), configuration.getUsername());
    }

}
