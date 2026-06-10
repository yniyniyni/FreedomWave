# FreedomWave

[English](README.md) | **Русский**

Нативный Android-клиент для администрирования [Remnawave](https://remna.st) — self-hosted панели управления VPN/proxy. Написан на Kotlin Multiplatform и Compose Multiplatform: весь UI и бизнес-логика — в общем коде (iOS-таргет собирается как framework-заглушка).

## Возможности

- **Дашборд** — живая статистика системы, трафик, пользователи онлайн, счётчики по нодам
- **Пользователи** — полный CRUD, поиск, фильтры и сортировка, лимиты трафика и срок действия, членство в сквадах, ссылка на подписку с QR-кодом, HWID-устройства
- **Ноды** — мониторинг статуса и аптайма, включение/отключение/перезапуск, статистика трафика
- **Хосты** — управление хостами с настройкой VLESS и security layer
- **Сквады** — управление внутренними сквадами, массовое добавление/удаление пользователей
- **Трафик** — графики трафика по нодам
- **Безопасность** — биометрическая блокировка приложения
- **Темы** — Material 3, динамические цвета на Android

## Начало работы

1. В панели Remnawave создайте API-токен (раздел **API Tokens**).
2. Установите приложение и введите:
   - URL вашей панели (например, `https://panel.example.com`),
   - API-токен.
3. Приложение проверит токен и подключится. Токен хранится локально и фактически бессрочен; при ответе 401 приложение возвращается на экран входа.

## Сборка

```bash
# Debug APK
./gradlew :app:assembleDebug

# Release APK
./gradlew :app:assembleRelease

# Юнит-тесты
./gradlew :composeApp:testDebugUnitTest

# iOS framework (требуется полный Xcode)
./gradlew :composeApp:linkDebugFrameworkIosArm64
```

Требования: JDK 17+, Android SDK 36. Минимальная версия Android: 12 (API 31).

## Архитектура

Двухмодульная структура (AGP 9+ запрещает совмещать `com.android.application` и `kotlin.multiplatform` в одном модуле):

| Модуль | Назначение |
|---|---|
| `:app` | Тонкий Android-лаунчер — `MainActivity`, `Application`, манифест, ресурсы |
| `:composeApp` | Весь общий код: Compose UI, домен, данные, DI. Собирается в Android AAR и iOS framework |

Внутри `:composeApp/commonMain`: MVVM на `StateFlow`, Ktor-клиент с Bearer-авторизацией для REST API Remnawave, репозитории с маппингом DTO в доменные модели, Koin для DI, DataStore для настроек.

### Стек

| Роль | Библиотека |
|---|---|
| Язык / UI | Kotlin Multiplatform, Compose Multiplatform (Material 3) |
| Сеть | Ktor Client + kotlinx.serialization |
| DI | Koin |
| Хранилище | AndroidX DataStore (multiplatform) |
| ViewModel | JetBrains lifecycle-viewmodel-compose |
| Изображения / QR | Coil 3, qrose |
| Логирование | Kermit |

## Лицензия

Распространяется под лицензией [European Union Public Licence v. 1.2](LICENSE) (EUPL-1.2).
