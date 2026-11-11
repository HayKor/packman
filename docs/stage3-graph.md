# Этап 3. Граф зависимостей

Цель этапа — построить транзитивный граф зависимостей, применить фильтр,
корректно обработать циклы и поддержать работу с тестовым репозиторием.

## Алгоритм

- Обход — BFS с очередью `ArrayDeque`, без рекурсии.
- Узел графа идентифицируется как `имя@версия` (для тестового репозитория — `ИМЯ`),
  поэтому один и тот же пакет не обрабатывается повторно.
- Версия зависимости выбирается по объявленному диапазону:
  поддерживаются `^`, `~`, точные версии, `>=`, `>`, `<`, `<=`, `=`,
  частичные версии (`1`, `1.2`, `1.2.x`), диапазоны через дефис (`1.2.3 - 2.0.0`)
  и альтернативы (`1.x || >=3.0.0`). Выбирается наибольшая подходящая версия.
  Pre-release версии не подходят, если диапазон их явно не упоминает.
- Документ реестра запрашивается один раз на имя пакета и кэшируется в памяти.
- Фильтр — регистронезависимая подстрока в имени пакета; пакет исключается
  вместе со всем поддеревом. Если под фильтр попадает сам анализируемый пакет,
  выводится ошибка.
- Циклы: обход BFS не зацикливается за счёт множества посещённых узлов,
  а после построения графа циклы находятся итеративным DFS и печатаются.
  Цикл выводится один раз: `A -> B -> C -> A`.

## Тестовый репозиторий

В режиме `repository_mode: test` параметр `repository` указывает на текстовый
файл, где каждая строка описывает пакет:

```text
# комментарий
A: B, C
B: C
C:
```

- имена пакетов — заглавные латинские буквы, цифры, `_`, `-`;
- зависимости перечисляются через пробел или запятую;
- пустые строки и текст после `#` игнорируются;
- повторное объявление пакета, неверное имя и строка без `:` — ошибка с номером строки;
- ссылка на необъявленный пакет обнаруживается при анализе и сообщается явно.

Примеры файлов: `tests/fixtures/repos/`.

## Демонстрация

Линейная цепочка `A -> B -> C`:

```console
$ ./run.sh --config tests/fixtures/configs/test-linear.yaml
Dependency graph for A
Nodes: 3
Edges: 2

A -> B
B -> C
C -> (no dependencies)

Excluded by filter: none
Cycles: none
```

Ромб `A -> B, C; B, C -> D`:

```console
A -> B, C
B -> D
C -> D
D -> (no dependencies)
```

Цикл `A -> B -> C -> A`:

```console
Dependency graph for A
Nodes: 3
Edges: 3

A -> B
B -> C
C -> A

Excluded by filter: none
Cycles: A -> B -> C -> A
```

Самозависимость `A -> A, B`:

```console
A -> A, B
B -> (no dependencies)

Cycles: A -> A
```

Фильтр `filter: "d"` для графа `A -> B, C; B, C -> D; D -> E`
(пакет `D` исключается вместе с поддеревом, регистр не важен):

```console
Dependency graph for A
Nodes: 3
Edges: 2

A -> B, C
B -> (no dependencies)
C -> (no dependencies)

Excluded by filter: D
Cycles: none
```

Реестр npm (локальный сервер с тестовыми пакетами, тот же прогон в e2e):

```console
Dependency graph for root-package@1.0.0
Nodes: 4
Edges: 4

leaf-lib@1.2.0 -> (no dependencies)
other-lib@1.0.5 -> leaf-lib@1.2.0
root-package@1.0.0 -> other-lib@1.0.5, shared-lib@2.0.0
shared-lib@2.0.0 -> leaf-lib@1.2.0
```

Реальный пакет `express` (фрагмент, 68 узлов, 127 рёбер):

```console
Dependency graph for express@5.3.0
Nodes: 68
Edges: 127

accepts@2.0.0 -> mime-types@3.0.2, negotiator@1.1.0
body-parser@2.4.0 -> bytes@3.1.2, content-type@2.1.0, debug@4.4.3, ...
...
```

## Обработка ошибок

| Ситуация | Сообщение |
|---|---|
| под фильтр попал корень | `error: package 'A' matches filter 'a', nothing to analyse` |
| пакета нет в тестовом репозитории | `error: package 'Z' is not declared in the test repository` |
| ошибка формата репозитория | `error: test repository line 2: invalid package name 'a'; ...` |
| нет подходящей версии | `error: no published version satisfies range '^9.0.0' (N versions available)` |
| неподдерживаемый диапазон | `error: unsupported version range 'npm:other@1.0.0'; ...` |
| файл репозитория не читается | `error: cannot read test repository '<path>': ...` |

## Автоматические проверки

Unit-тесты (офлайн):

- `SemVerTest`, `VersionRangeTest`, `VersionResolverTest` — сравнение версий,
  все формы диапазонов, выбор версии, ошибки;
- `TestRepositoryParserTest` — формат, комментарии, дубликаты, неверные имена;
- `NpmDependencySourceTest` — разрешение `latest` и диапазонов, кэш документов;
- `DependencyGraphBuilderTest` — цепочка, ромб, цикл, фильтр, дедупликация рёбер;
- `CycleDetectorTest` — цикл, самозависимость, независимые циклы, ромб;
- `DependencyGraphReportTest`, `GraphCommandTest`.

Интеграционные проверки (`make e2e`) прогоняют все тестовые репозитории,
локальный registry и ошибочные конфигурации.
