# WorkoutDiary

Android-приложение для ведения дневника тренировок.

## Описание проекта

Приложение позволяет:

- добавлять и удалять упражнения;
- выбирать упражнения перед началом тренировки;
- проводить тренировку с указанием количества подходов и повторений;
- отслеживать время выполнения каждого упражнения;
- сохранять тренировки в локальную базу данных;
- просматривать историю тренировок с датой, списком упражнений и общим временем;
- генерировать тренировку через AI и сохранять предложенные упражнения в локальную базу.

## Release Notes

### Версия 1.0

- `versionName`: `1.0`
- `versionCode`: `1`

- добавлены экран тренировки и сохранение истории занятий;
- добавлена AI-генерация тренировок через OpenRouter;
- настроена публикация в RuStore через Gradle task `publishToRuStore`;
- добавлена release-подпись сборки через параметры из `local.properties`.

### Шаблон для следующей версии

### Версия X.Y

- `versionName`: `X.Y`
- `versionCode`: `N`
- описание изменений...

## Скриншоты

<p align="center">
  <img src="screenshots/Screenshot%202026-03-23%20143635.png" alt="Главный экран" width="30%" />
  <img src="screenshots/Screenshot_20260323_144609.png" alt="История тренировок" width="30%" />
  <img src="screenshots/Screenshot_20260323_144824.png" alt="Редактор упражнений" width="30%" />
</p>

<p align="center">
  <img src="screenshots/Screenshot_20260323_144857.png" alt="Список упражнений" width="30%" />
  <img src="screenshots/Screenshot_20260323_144916.png" alt="Экран тренировки" width="30%" />
  <img src="screenshots/Screenshot_20260323_144926.png" alt="AI тренировка" width="30%" />
</p>

<p align="center">
  <img src="screenshots/Screenshot_20260323_144938.png" alt="AI результат" width="30%" />
  <img src="screenshots/Screenshot_20260323_144953.png" alt="Дополнительный экран" width="30%" />
</p>

## Технологии

- Kotlin
- Jetpack Compose
- ViewModel
- Room
- MVI
- OpenRouter API

## OpenRouter

Для AI-вкладки проект использует OpenRouter через OpenAI-compatible API.

### Как подключить OpenRouter

Что нужно сделать:

1. Зарегистрироваться на `https://openrouter.ai/`
2. Открыть раздел `Keys`
3. Сгенерировать API key
4. Добавить ключ и модель в `local.properties` в корне проекта

Пример `local.properties`:

```properties
llmApiKey=sk-or-v1-...
llmModel=meta-llama/llama-3.1-8b-instruct
```

Где взять данные:

- `llmApiKey` - создается в личном кабинете OpenRouter, в разделе `Keys`
- `llmModel` - это идентификатор модели из OpenRouter, например `meta-llama/llama-3.1-8b-instruct`

Важно:

- не добавляй `local.properties` в git
- не публикуй API key в репозитории
- если ключ скомпрометирован, удали его в OpenRouter и создай новый

## RuStore API

Для публикации в RuStore добавлен конфиг `rustorePublishing` в `app/build.gradle.kts` и task `publishToRuStore`.

Пример конфигурации через Gradle properties:

```properties
rustoreKeyId=123456
rustorePrivateKey=BASE64_PRIVATE_KEY
rustoreArtifactType=AAB
rustoreAppType=MAIN
rustoreCategories=health,fitness
rustoreMinAndroidVersion=8
rustoreDeveloperEmail=Masha_9595@mail.ru
rustoreDeveloperWebsite=
rustoreDeveloperVkCommunity=
rustorePublishType=MANUAL
rustorePartialValue=100
rustoreReleaseNotes=Сборка из CI
rustorePriorityUpdate=0
```

Доступные параметры:

- `rustoreKeyId` - ID ключа из RuStore Console
- `rustorePrivateKey` - приватный ключ из RuStore Console
- `rustoreArtifactType` - `AAB` или `APK`
- `rustoreAppType` - `MAIN` или `GAMES`
- `rustoreCategories` - список категорий через запятую
- `rustoreMinAndroidVersion` - минимальная версия Android
- `rustoreDeveloperEmail` - email разработчика
- `rustoreDeveloperWebsite` - сайт разработчика, опционально
- `rustoreDeveloperVkCommunity` - ссылка на VK-сообщество, опционально
- `rustorePublishType` - `MANUAL`, `INSTANTLY` или `DELAYED`
- `rustorePartialValue` - процент публикации: `5`, `10`, `25`, `50`, `75`, `100`
- `rustoreReleaseNotes` - описание `Что нового`
- `rustorePriorityUpdate` - приоритет обновления от `0` до `5`

Gradle task для публикации:

```bash
./gradlew publishToRuStore
```

Что нужно настроить в RuStore:

1. Открыть `https://console.rustore.ru/`
2. Сгенерировать ключ в разделе `API RuStore`
3. Скопировать `keyId` и приватный ключ
4. Убедиться, что у ключа есть доступ к методам публикации приложений
5. Добавить значения в `local.properties` или передавать как Gradle properties в CI

Важно:

- для работы API должна существовать хотя бы одна активная версия приложения в RuStore
- релизная сборка должна быть подписана
- приватный ключ нельзя коммитить в репозиторий

## Подпись release

Для release-сборки проект читает данные подписи из `local.properties` или из Gradle properties/CI-переменных.

Добавь в `local.properties`:

```properties
releaseStoreFile=keystore/release.keystore
releaseStorePassword=YOUR_STORE_PASSWORD
releaseKeyAlias=YOUR_KEY_ALIAS
releaseKeyPassword=YOUR_KEY_PASSWORD
```

Пояснение:

- `releaseStoreFile` - путь до keystore относительно корня проекта
- `releaseStorePassword` - пароль от keystore
- `releaseKeyAlias` - alias ключа внутри keystore
- `releaseKeyPassword` - пароль от ключа

Если подпись не настроена, `publishToRuStore` завершится с понятной ошибкой до начала публикации.
