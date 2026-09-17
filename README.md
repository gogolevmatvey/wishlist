# Wishlist Organizer

Android-приложение для ведения списка желаний. Помогает сохранять товары и идеи, распределять их по категориям и отслеживать путь от желания до покупки.

## Возможности

- Просмотр списка желаний и обновление жестом pull-to-refresh.
- Создание, редактирование и удаление карточек.
- Категории, описание, ориентировочная цена и ссылка на магазин.
- Приоритеты: низкий, средний и высокий.
- Статусы: «Желаю», «Куплено» и «Отменено», а также фильтрация по статусу.
- Просмотр деталей и отправка карточки через системное меню «Поделиться».
- Загрузка изображения из галереи; в интерфейсе также доступен вызов камеры.
- Сводная статистика по загруженному списку.

## Технологии

- Java 11 и Android SDK (minSdk 31, target/compileSdk 36)
- Gradle 8.13 и Android Gradle Plugin 8.3.0
- AndroidX, Material Components, RecyclerView
- Retrofit 2, OkHttp и Gson для REST API
- Glide для отображения изображений

## Требования

- Android Studio с JDK 17 (встроенный JDK Android Studio подойдёт).
- Android SDK Platform 36.
- Запущенный совместимый REST API, доступный с устройства или эмулятора.

## Настройка API

Приложение не хранит данные локально: оно обращается к серверному API. Базовый URL задаётся в [AppConfig.java](app/src/main/java/com/example/wishlist/config/AppConfig.java):

```java
public static final String API_BASE_URL = "http://192.168.56.1/";
```

Перед запуском замените его на адрес своего сервера, обязательно оставив завершающий `/`. Для эмулятора Android адрес хоста зависит от используемой виртуальной сети; для физического устройства сервер должен быть доступен из его сети.

Клиент ожидает следующие пути относительно базового URL:

| Метод | Путь | Назначение |
| --- | --- | --- |
| `GET` | `api/api_categories.php` | Получить категории |
| `GET` | `api/api_wishes.php` | Получить список желаний; доступны `category_id` и `status` |
| `GET` | `api/api_wishes.php?id={id}` | Получить одно желание |
| `POST` | `api/api_wishes.php` | Создать желание (JSON) |
| `PUT` | `api/api_wishes.php` | Обновить желание (JSON) |
| `DELETE` | `api/api_wishes.php?id={id}` | Удалить желание |
| `POST` | `api/upload_image.php` | Загрузить изображение (`multipart/form-data`, поле `image`) |

Карточка желания передаёт поля `title`, `description`, `category_id`, `estimated_price`, `store_url`, `priority`, `status` и при наличии `image_path`. API должен возвращать данные в формате, соответствующем модели [`Wish`](app/src/main/java/com/example/wishlist/models/Wish.java), а операции сохранения — объект с полем `success`.

> В манифесте включён незашифрованный HTTP-трафик (`usesCleartextTraffic="true"`), поскольку адрес API по умолчанию использует `http`. Для публичного сервера рекомендуется использовать HTTPS и отключить эту настройку.

## Запуск

1. Клонируйте репозиторий и откройте его в Android Studio.
2. Установите адрес API в `AppConfig.API_BASE_URL`.
3. Дождитесь синхронизации Gradle и выберите устройство с Android 12 (API 31) или новее.
4. Нажмите **Run**.

Сборку из командной строки можно выполнить так:

```bash
./gradlew assembleDebug
```

APK появится по пути `app/build/outputs/apk/debug/app-debug.apk`.

## Разрешения

Приложение запрашивает доступ к интернету и состоянию сети. Камера и доступ к изображениям используются только при добавлении фотографии к желанию. Набор разрешений зависит от версии Android: для Android 13+ заявлен `READ_MEDIA_IMAGES`, для более ранних версий — `READ_EXTERNAL_STORAGE`.

## Структура проекта

```text
app/src/main/java/com/example/wishlist/
├── activities/  # экраны приложения
├── adapters/    # адаптер списка желаний
├── api/         # Retrofit-клиент и описание API
├── config/      # параметры приложения и API
├── models/      # модели Wish, Category и ApiResponse
└── utils/       # работа с датами, сетью и изображениями
```

## Проверка

```bash
./gradlew test
```

Инструментальные тесты требуют подключённого устройства или эмулятора:

```bash
./gradlew connectedAndroidTest
```
