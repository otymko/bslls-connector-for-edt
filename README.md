# Коннектор BSLLS для 1С:EDT

Плагин включает проверки [BSL LS](https://github.com/1c-syntax/bsl-language-server) в среде разработки [1С:EDT](https://edt.1c.ru/).
Это добавляет `128+` [диагностик](https://1c-syntax.github.io/bsl-language-server/diagnostics/).

## Возможности

- [x] Проверки кода
- [ ] Быстрые исправления
- [ ] Произвольные ссылки

## Установка

1. Откройте `Справка` -> `Установить новое ПО`.
2. Введите ссылку:
```
https://otymko.github.io/bslls-connector-for-edt/update/bslls-connector-for-edt/latest/
```
3. Нажмите `Добавить`.
4. Установите флажок на `BSL LS connector for EDT` и `LSP4J SDK`.
5. Убедитесь, что установлен фложок `Обращаться во время инсталяции ко всем сайтам обновления для поиска требуемого ПО`.
6. Нажмите `Далее` -> `Готово`.
7. Перезапустите 1С:EDT.

### Первый запуск

При первом запуске нужно загрузить BSL LS.
1. Отркройте  `Окно` -> `Параметры`.
2. Перейдите на вкладку `Коннектор BSLLS`.
3. Убедитесь что запущено задание `Загрузка BSL LS`.

Загрузка выполняется в каталог `%USER_HOME%/.bsl-connector-for-edt/bsl-language-server`.

Для настройки проверки используется файл [.bsl-language-server.json](https://1c-syntax.github.io/bsl-language-server/features/ConfigurationFile/).

Шаблон файла `.bsl-language-server.json` можно взять [example/.bsl-language-server.json](/example/.bsl-language-server.json).

Для работы плагина требуется:
* Значение `computeTrigger` в `onSave`
* Путь к метаданным проекта в свойстве `configurationRoot`

### Просмотр списка найденных проблем

Проверки, выполняемые 1С:EDT и текущим плагином используют разные панели отображения ошибок. Панель 1С:EDT разработана отдельно, называется `Проблемы конфигурации`. Плагин использует типовую панель Eclipse `Проблемы`.

### Установка из архива

Аналогична установки по адресу.
При выполнении шага 2 нажмите `Архив`.

## Разработчикам

Для разработки требуется:
* Java 11
* Eclipse for Committer 2020-06 / 2020-09
* Плагин lombok (https://projectlombok.org/setup/eclipse)

### Локальная сборка плагина на Windows

Сборка с использованием [lombok](https://projectlombok.org/setup/ecj):

> `tycho-compiler-plugin` не умеет обрабатывать аннотации `lombok` и дополнять байт-код вне приложения `eclipse` (там свой java-agent).
> Поэтому нужно вручную переопределить `javaagent`-а для корректной сборки проекта.
> Ниже сборка простым заявленным путем от вендора `lombok`

1. Очистите переменную среды `MAVEN_OPTS` от `javaagent` (пункт 3)

```
set MAVEN_OPTS=
```

2. Скачайте `lombok`:

```
mvn clean dependency:copy@get-lombok
```

3. Назначьте `javaagent` в переменную окружения

```
set MAVEN_OPTS=-javaagent:target/lombok.jar=ECJ
```

4. Проверьте и соберите проект

```
mvn verify -PSDK,find-bugs -Dtycho.localArtifacts=ignore
```
