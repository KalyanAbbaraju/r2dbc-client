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

import com.nebhale.r2dbc.Row;
import com.nebhale.r2dbc.postgresql.message.backend.DataRow;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * An implementation of {@link Row} for a PostgreSQL database.
 */
public final class PostgresqlRow implements Row {

    private final List<PostgresqlColumn> columns;

    PostgresqlRow(List<PostgresqlColumn> columns) {
        this.columns = Objects.requireNonNull(columns, "columns must not be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PostgresqlRow that = (PostgresqlRow) o;
        return Objects.equals(this.columns, that.columns);
    }

    @Override
    public List<PostgresqlColumn> getColumns() {
        return this.columns;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.columns);
    }

    @Override
    public String toString() {
        return "PostgresqlRow{" +
            "columns=" + this.columns +
            '}';
    }

    static PostgresqlRow toRow(DataRow dataRow) {
        Objects.requireNonNull(dataRow, "dataRow must not be null");

        return new PostgresqlRow(dataRow.getColumns().stream()
            .map(PostgresqlColumn::new)
            .collect(Collectors.toList()));
    }

}
