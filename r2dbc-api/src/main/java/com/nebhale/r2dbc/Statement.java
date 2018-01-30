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

package com.nebhale.r2dbc;

import org.reactivestreams.Publisher;

/**
 * A statement that can be executed multiple times in a prepared and optimized way.
 */
public interface Statement {

    /**
     * Bind a set of arguments the statement.
     *
     * @param parameters the parameters to bind
     * @return this {@link Statement}
     */
    Statement bind(Iterable<Object> parameters);

    /**
     * Executes the statement multiple times.
     *
     * @return the results, if any, returned by each execution of the statement
     */
    Publisher<? extends Result> execute();

}
