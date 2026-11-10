# Этап 1. Минимальный прототип с конфигурацией

> Отчёт отражает состояние кода на коммите этапа 1. На следующих этапах
> приложение вместо печати параметров выполняет анализ зависимостей.

Цель этапа — создать CLI-приложение, которое читает настраиваемые параметры
из YAML-файла, проверяет их и выводит в формате «ключ: значение».

## Реализация

- Точка входа — `src/main.kt`, сборка — Kotlin/Native `linux/app` через Kotlin toolchain.
- Разбор аргументов (`--config`, `--help`) — `src/ru/haykor/pacman/cli/Arguments.kt`.
- Загрузка YAML и валидация — `src/ru/haykor/pacman/config/`.
- Печать параметров — `src/ru/haykor/pacman/config/ConfigPrinter.kt`.
- Коды возврата: `0` — успех, `1` — ошибка конфигурации, `2` — ошибка аргументов.

## Параметры и правила проверки

| Параметр | Проверки |
|---|---|
| `package` | не пустой; в режиме `registry` — синтаксис npm (строчные буквы, цифры, `.`, `_`, `~`, `-`, префикс `@scope/`, не длиннее 214 символов); в режиме `test` — заглавные латинские буквы, цифры, `_`, `-` |
| `repository` | в режиме `registry` — URL с схемой `http`/`https` и хостом; в режиме `test` — путь к существующему обычному файлу |
| `repository_mode` | одно из значений `registry`, `test` |
| `image_file` | не пустой; расширение `.png`; без разделителей каталогов; не длиннее 255 символов |
| `ascii_tree` | только `true` или `false` |
| `filter` | не длиннее 64 символов; без переводов строк; пустое значение допустимо и отключает фильтр |
| файл конфигурации | существует, является обычным файлом, не пуст, корень — словарь, ключи только поддерживаемые |

## Демонстрация

Запуск с корректной конфигурацией:

```console
$ ./run.sh --config config.yaml
package: express
repository: https://registry.npmjs.org
repository_mode: registry
image_file: graph.png
ascii_tree: false
filter: ""
```

### Ошибки параметров

| Случай | Команда | Вывод (stderr) | Код |
|---|---|---|---|
| файл не найден | `--config missing.yaml` | `error: configuration file 'missing.yaml' does not exist` | 1 |
| пустой файл | `--config empty.yaml` | `error: configuration file is empty` | 1 |
| битый YAML | `--config broken.yaml` | `error: configuration file is not valid YAML: ... at line 2, column 41; note that values containing '://' must be quoted` | 1 |
| неизвестный параметр | `--config unknown.yaml` | `error: unknown parameter(s) 'pakage'; supported parameters: 'ascii_tree', 'filter', 'image_file', 'package', 'repository', 'repository_mode'` | 1 |
| пропущен параметр | `--config missing-filter.yaml` | `error: parameter 'filter' is required` | 1 |
| неверное имя пакета | `--config package.yaml` | `error: parameter 'package': 'Express' is not a valid npm package name; use lowercase letters, digits, '.', '_', '~', '-' and an optional @scope/ prefix` | 1 |
| неверный URL | `--config repository.yaml` | `error: parameter 'repository': 'ftp://example.com' is not a valid HTTP(S) URL, for example https://registry.npmjs.org` | 1 |
| неверный режим | `--config mode.yaml` | `error: parameter 'repository_mode': unknown value 'remote', expected one of registry|test` | 1 |
| неверное имя файла | `--config image.yaml` | `error: parameter 'image_file': 'graph.svg' must have the '.png' extension` | 1 |
| неверный boolean | `--config boolean.yaml` | `error: parameter 'ascii_tree' must be a boolean ('true' or 'false'), got 'yes'` | 1 |
| слишком длинный фильтр | `--config filter.yaml` | `error: parameter 'filter': value must not be longer than 64 characters` | 1 |
| неверный аргумент CLI | `--unknown` | `error: unknown argument '--unknown'` и подсказка про `--help` | 2 |

## Автоматические проверки

Unit-тесты покрывают все перечисленные случаи:

```console
$ make test
...
Passed rejectsInvalidBoolean
Passed rejectsTooLongFilter
Completed ru.haykor.pacman.config.ConfigLoaderTest
```

Интеграционные проверки:

```console
$ make e2e
All end-to-end checks passed
```
