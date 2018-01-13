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
import com.nebhale.r2dbc.postgresql.client.TestClient;
import com.nebhale.r2dbc.postgresql.client.WindowCollector;
import com.nebhale.r2dbc.postgresql.message.backend.CommandComplete;
import com.nebhale.r2dbc.postgresql.message.backend.DataRow;
import com.nebhale.r2dbc.postgresql.message.backend.ParameterStatus;
import com.nebhale.r2dbc.postgresql.message.frontend.Query;
import com.nebhale.r2dbc.postgresql.message.frontend.Terminate;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Collections;

import static com.nebhale.r2dbc.IsolationLevel.READ_COMMITTED;
import static com.nebhale.r2dbc.Mutability.READ_ONLY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public final class PostgresqlConnectionTest {

    @Test
    public void begin() {
        Client client = TestClient.builder()
            .expectRequest(new Query("BEGIN")).thenRespond(new CommandComplete("BEGIN", null, null))
            .build();

        new PostgresqlConnection(client)
            .begin()
            .as(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void close() {
        Client client = TestClient.builder()
            .expectRequest(Terminate.INSTANCE).thenRespond()
            .expectClose()
            .build();

        new PostgresqlConnection(client)
            .close()
            .as(StepVerifier::create)
            .verifyComplete();
    }

    @Test
    public void constructorNoClient() {
        assertThatNullPointerException().isThrownBy(() -> new PostgresqlConnection(null))
            .withMessage("client must not be null");
    }

    @Test
    public void getParameterStatus() {
        Client client = TestClient.builder()
            .parameterStatus("test-key", "test-value")
            .build();

        assertThat(new PostgresqlConnection(client).getParameterStatus()).containsEntry("test-key", "test-value");
    }

    @Test
    public void query() {
        Client client = TestClient.builder()
            .expectRequest(new Query("test-query")).thenRespond(new CommandComplete("test", null, null))
            .build();

        new PostgresqlConnection(client)
            .query("test-query")
            .as(StepVerifier::create)
            .verifyComplete();
    }

    @Test
    public void queryNoQuery() {
        assertThatNullPointerException().isThrownBy(() -> new PostgresqlConnection(TestClient.NO_OP).query(null))
            .withMessage("query must not be null");
    }

    @Test
    public void setIsolationLevel() {
        Client client = TestClient.builder()
            .expectRequest(new Query("SET SESSION CHARACTERISTICS AS TRANSACTION ISOLATION LEVEL READ COMMITTED")).thenRespond(new CommandComplete("SET", null, null))
            .build();

        new PostgresqlConnection(client)
            .setIsolationLevel(READ_COMMITTED)
            .as(StepVerifier::create)
            .verifyComplete();
    }

    @Test
    public void setIsolationLevelNoIsolationLevel() {
        assertThatNullPointerException().isThrownBy(() -> new PostgresqlConnection(TestClient.NO_OP).setIsolationLevel(null))
            .withMessage("isolationLevel must not be null");
    }

    @Test
    public void setMutability() {
        Client client = TestClient.builder()
            .expectRequest(new Query("SET SESSION CHARACTERISTICS AS TRANSACTION READ ONLY")).thenRespond(new CommandComplete("SET", null, null))
            .build();

        new PostgresqlConnection(client)
            .setMutability(READ_ONLY)
            .as(StepVerifier::create)
            .verifyComplete();
    }

    @Test
    public void setMutabilityNoMutability() {
        assertThatNullPointerException().isThrownBy(() -> new PostgresqlConnection(TestClient.NO_OP).setMutability(null))
            .withMessage("mutability must not be null");
    }

    @Test
    public void withTransaction() {
        Client client = TestClient.builder()
            .expectRequest(new Query("BEGIN")).thenRespond(new CommandComplete("BEGIN", null, null))
            .expectRequest(new Query("test-query")).thenRespond(new DataRow(Collections.emptyList()), new CommandComplete("SELECT", null, null))
            .expectRequest(new Query("COMMIT")).thenRespond(new CommandComplete("COMMIT", null, null))
            .build();

        WindowCollector<PostgresqlRow> windows = new WindowCollector<>();

        new PostgresqlConnection(client)
            .withTransaction(transaction ->
                transaction.query("test-query"))
            .as(StepVerifier::create)
            .recordWith(windows)
            .expectNextCount(1)
            .verifyComplete();

        windows.next()
            .as(StepVerifier::create)
            .expectNext(new PostgresqlRow(Collections.emptyList()))
            .verifyComplete();
    }

    @Test
    public void withTransactionNoTransaction() {
        assertThatNullPointerException().isThrownBy(() -> new PostgresqlConnection(TestClient.NO_OP).withTransaction(null))
            .withMessage("transaction must not be null");
    }

    @Test
    public void withTransactionRollback() {
        Client client = TestClient.builder()
            .expectRequest(new Query("BEGIN")).thenRespond(new CommandComplete("BEGIN", null, null))
            .expectRequest(new Query("test-query")).thenRespond(Flux.error(new ServerErrorException(Collections.emptyList())))
            .expectRequest(new Query("ROLLBACK")).thenRespond(new CommandComplete("ROLLBACK", null, null))
            .build();

        new PostgresqlConnection(client)
            .withTransaction(transaction ->
                transaction.query("test-query"))
            .as(StepVerifier::create)
            .verifyError(ServerErrorException.class);
    }

}
