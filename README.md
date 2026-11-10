# pacman

Инструмент визуализации графа зависимостей для менеджера пакетов npm.
Учебный проект по дисциплине «Конфигурационное управление», вариант 4.

Приложение на Kotlin/Native (`linux/app`), собирается Kotlin toolchain (ex-Amper).
Готовые менеджеры пакетов и библиотеки для получения зависимостей не используются:
информация о пакетах извлекается из ответов npm registry самостоятельно.

## Текущее состояние

| Этап | Содержание | Статус |
|---|---|---|
| 1 | Минимальный прототип с конфигурацией | реализован |
| 2 | Сбор данных о прямых зависимостях | не реализован |
| 3 | Граф зависимостей (BFS, циклы, тестовый репозиторий) | не реализован |
| 4 | Порядок загрузки зависимостей | не реализован |

## Требования

- Linux x86_64.
- JDK 21+ для запуска CLI Kotlin toolchain (скомпилированное приложение JDK не требует).
- Сеть — только для запуска в режиме `registry`; unit- и e2e-тесты работают офлайн.

## Сборка и запуск

```sh
make build          # сборка нативного исполняемого файла
make run            # запуск с config.yaml
make test           # unit-тесты (Kotlin/Native test)
make e2e            # интеграционные проверки CLI
make clean          # удаление артефактов сборки
```

Запуск вручную:

```sh
./run.sh --config config.yaml
```

`run.sh` вызывает wrapper `./kotlin` из репозитория, поэтому отдельно
устанавливать Kotlin toolchain не нужно.

## Конфигурация

Параметры задаются в YAML-файле, путь передаётся опцией `--config`
(по умолчанию `config.yaml`). Формат — плоский список `ключ: значение`.

| Параметр | Тип | Назначение |
|---|---|---|
| `package` | строка | имя анализируемого пакета |
| `repository` | строка | URL npm registry или путь к файлу тестового репозитория |
| `repository_mode` | `registry` или `test` | режим работы с репозиторием |
| `image_file` | строка | имя файла с изображением графа (`.png`) |
| `ascii_tree` | `true`/`false` | вывод зависимостей в виде ASCII-дерева |
| `filter` | строка | подстрока для исключения пакетов; пустая строка отключает фильтр |

Пример `config.yaml`:

```yaml
package: express
repository: "https://registry.npmjs.org"
repository_mode: registry
image_file: graph.png
ascii_tree: false
filter: ""
```

Значения, содержащие `://`, нужно брать в кавычки: так требует YAML-парсер
`yamlkt`, который иначе трактует `https://...` как вложенный словарь.

### Обработка ошибок

Проверяется каждый параметр; сообщение содержит имя параметра и ожидаемое значение.
Примеры:

```text
error: parameter 'ascii_tree' must be a boolean ('true' or 'false'), got 'yes'
error: parameter 'package': 'Express' is not a valid npm package name; ...
error: parameter 'repository_mode': unknown value 'remote', expected one of registry|test
error: parameter 'filter' is required
error: unknown parameter(s) 'pakage'; supported parameters: ...
```

Коды возврата: `0` — успех, `1` — ошибка конфигурации или выполнения,
`2` — неверные аргументы командной строки.

## Тесты

- `test/` — unit-тесты Kotlin: загрузка и валидация конфигурации,
  разбор аргументов, форматирование вывода.
- `tests/` — интеграционные проверки: fixture-файлы, тестовые репозитории,
  ожидаемый вывод и скрипт `run_e2e.sh`.

Unit-тесты запускаются командой `make test`, интеграционные — `make e2e`.

## Структура репозитория

```
src/              исходный код (Kotlin/Native)
test/             unit-тесты Kotlin (каталог задан Kotlin toolchain)
tests/            интеграционные тесты и тестовые данные
docs/             отчёты по этапам работы
module.yaml       конфигурация модуля Kotlin toolchain
libs.versions.toml каталог версий зависимостей
run.sh, Makefile  скрипты запуска и сборки
config.yaml       конфигурация по умолчанию
```

## Документация по этапам

- [Этап 1. Конфигурация](docs/stage1-configuration.md)
