# scala-rest

Собирается gradle.

gradle clean build run -> запуск приложения

gradle clean build test -> выполнить тесты

| URl                                  | Get           | Post  |Put    | Delete |
| -------------                        |:-------------:| -----:|------:|-------:|
| /warehouse                           |      +        |   +   |   +   |    +   |
| /warehouse/{storageName}             |      +        |   +   |   +   |    +   |
| /warehouse/storageName/item/{item_id}|      +        |   -   |   -   |    +   |
