# Этап 2. Сбор данных о прямых зависимостях

> Отчёт отражает состояние кода на коммите этапа 2: на следующем этапе
> вывод прямых зависимостей заменён построением транзитивного графа.

Цель этапа — получать информацию о прямых зависимостях выбранного npm-пакета
напрямую из реестра, без менеджера пакетов и без библиотек, предназначенных
для разрешения зависимостей.

## Реализация

- HTTP-клиент — Ktor с движком Curl (`KtorNpmRegistryClient`); сетевой стек
  не имеет отношения к разрешению зависимостей.
- Запрос: `GET <repository>/<package>`; для scoped-пакетов `/` кодируется как `%2F`.
- Из ответа берутся:
  - `dist-tags.latest` — актуальная версия;
  - `versions[latest].dependencies` — объявленные прямые зависимости.
- JSON разбирается вручную (`NpmPackumentParser`) средствами kotlinx.serialization;
  модель не доверяет типам полей: нестроковые значения зависимостей игнорируются.
- Таймауты: 30 с на запрос, 10 с на соединение.

## Демонстрация

```console
$ ./run.sh --config config.yaml
Package: express
Repository: https://registry.npmjs.org
Version: 5.3.0
Direct dependencies: 28
  accepts: ^2.0.0
  body-parser: ^2.3.0
  content-disposition: ^2.0.1
  ...
  vary: ^1.1.2
```

Маленький пакет:

```console
$ ./run.sh --config is-odd.yaml
Package: is-odd
Repository: https://registry.npmjs.org
Version: 3.0.1
Direct dependencies: 1
  is-number: ^6.0.0
```

## Обработка ошибок

| Ситуация | Сообщение | Код |
|---|---|---|
| пакет отсутствует (HTTP 404) | `error: package 'definitely-missing-package-xyz' was not found in registry 'https://registry.npmjs.org'` | 1 |
| реестр недоступен | `error: cannot reach npm registry at 'http://127.0.0.1:1': ... CURLE_COULDNT_CONNECT` | 1 |
| другой HTTP-код | `error: registry '<url>' responded with HTTP <code> for '<name>'` | 1 |
| ответ не JSON | `error: registry response is not valid JSON: ...` | 1 |
| нет `dist-tags.latest` | `error: registry response has no 'dist-tags.latest' field` | 1 |
| режим `test` | `error: repository_mode 'test' is not supported yet` | 1 |

## Автоматические проверки

Unit-тесты не обращаются к сети и используют сохранённый registry-документ
`tests/fixtures/npm/registry/sample-package`:

- разбор `dist-tags`, `versions`, манифеста;
- игнорирование нестроковых зависимостей;
- ошибки для битого JSON, отсутствующих полей и неверных типов.

Интеграционная проверка поднимает локальный HTTP-сервер над каталогом
`tests/fixtures/npm/registry` и сверяет вывод приложения, а также проверяет
обработку 404:

```console
$ make e2e
All end-to-end checks passed
```
