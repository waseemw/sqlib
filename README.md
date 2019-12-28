# Sqlib
###### Query databases, complete with HikariCP pool, thread-specific SQL binds as well as a built in logger!


## Usage
```kotlin
fun main() {
    val password = "password"
    val username = "username"
    val url = "jdbc:mysql://1.2.3.4:3306/databaseName?allowMultiQueries=true"
    val con = SqlibConnection(url, username, password);
    val list = con.fetch(String::class, "SELECT text FROM simpletable");
        for (i in list) {
        println(i)
    }
    con.execute("UPDATE simpletable SET text=? AND name=:name", 0 to "example", "name" to "John")
}
```


## License

    Copyright 2019 Waseem Wasaya
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
