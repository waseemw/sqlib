/*
 * Copyright 2019 Waseem Wasaya
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.waseem.sqlib

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.statement.Query
import org.jdbi.v3.core.statement.SqlStatement
import org.jdbi.v3.core.statement.Update
import org.slf4j.impl.SimpleLogger
import kotlin.reflect.KClass

@Suppress("unused", "MemberVisibilityCanBePrivate")
class SqlibConnection(
    url: String,
    username: String,
    password: String,
    maxPoolSize: Int = 5,
    logLevel: String = "Error"
) {
    private var threadBinds: ThreadLocal<Array<out Any>> = ThreadLocal()
    private val jdbi: Jdbi

    init {
        System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, logLevel)
        val hikariConfig = HikariConfig()
        hikariConfig.jdbcUrl = url
        hikariConfig.username = username
        hikariConfig.password = password
        hikariConfig.maximumPoolSize = maxPoolSize
        jdbi = Jdbi.create(HikariDataSource(hikariConfig))
    }

    fun addThreadMap(vararg binds: Any) {
        threadBinds.set(binds)
    }

    fun clear() {
        threadBinds.remove()
    }

    fun <T : Any> fetch(type: KClass<T>, sql: String, vararg binds: Any): List<T> {
        jdbi.open().use { return bind<Query>(it.createQuery(sql), *binds).mapTo(type.java).list() }
    }

    fun execute(sql: String, vararg binds: Any): Int {
        jdbi.open().use { return bind<Update>(it.createUpdate(sql), *binds).execute(); }
    }

    fun <T : Any> fetchOne(type: KClass<T>, sql: String, vararg binds: Any, default: T): T? {
        return fetch(type, sql, *binds).getOrElse(0) { default }
    }

    private fun <T : SqlStatement<T>> bind(query: T, vararg binds: Any): T {
        fun bind(arr: Array<out Any>) {
            for (bind in arr)
                if (bind is Pair<*, *>) {
                    val (key, value) = bind
                    if (key is Int)
                        query.bind(key, value)
                    else if (key is String && !key.startsWith("_"))
                        query.bind(key, value)
                } else query.bindBean(bind)
        }
        if (threadBinds.get() != null)
            bind(threadBinds.get())
        bind(binds)
        return query
    }
}
