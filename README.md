# Schedule_application

Полноценная система расписания для студентов:
- `android-app/` - Android-приложение на Kotlin (Compose)
- `server/` - backend на FastAPI для хранения и модерации расписания

## Реализованный функционал

1. Подключение к серверу и получение расписания
- Android-приложение подключается к настраиваемому URL сервера.
- Backend хранит несколько расписаний по студенческим группам.
- Приложение получает расписание на день/неделю по ключу чтения.

2. Суперпользователь + защищённое добавление/редактирование
- Авторизация суперпользователя по логину и паролю.
- Суперпользователь может создавать, обновлять (upsert), редактировать и удалять пары.
- Обычные пользователи могут отправлять предложения расписания по ключу отправки.
- Суперпользователь может принимать/отклонять предложения.

3. Настройки
- URL сервера
- Ключ чтения (`read key`)
- Ключ отправки (`submit key`)
- Студенческая группа
- Тема: светлая / тёмная / системная
- Настройка уведомлений по приоритетам

4. UX расписания и приоритеты
- При запуске автоматически выбирается текущий день недели.
- Пользователь может вручную переключать день.
- Для каждой пары есть 5 цветовых приоритетов:
  - Красный (максимальный)
  - Оранжевый
  - Жёлтый
  - Тёмно-зелёный
  - Светло-зелёный
- Уведомления настраиваются отдельно для каждого приоритета.

---

## Запуск сервера (`server/`)

### 1) Установка зависимостей
```bash
cd server
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
```

### 2) Настройка окружения
```bash
copy .env.example .env
```
Заполните `.env` продакшн-значениями:
- `READ_KEY`
- `SUBMIT_KEY`
- `JWT_SECRET`
- `SUPERUSER_USERNAME`
- `SUPERUSER_PASSWORD`
- `DATABASE_URL` (по умолчанию SQLite, для продакшна лучше PostgreSQL)

### 3) Локальный запуск
```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

Проверка доступности:
- `GET /health`

### 4) Деплой на Render
- Используйте стартовую команду из `server/render.yaml`:
  - `uvicorn app.main:app --host 0.0.0.0 --port $PORT`
- Добавьте переменные окружения из `.env.example` в панели Render.

---

## Обзор API

Публичные эндпоинты:
- `GET /api/v1/groups` (`X-Read-Key`)
- `GET /api/v1/schedule/day?group_name=...&day_of_week=...` (`X-Read-Key`)
- `GET /api/v1/schedule/week?group_name=...` (`X-Read-Key`)
- `POST /api/v1/proposals` (`X-Submit-Key`)

Админские эндпоинты:
- `POST /api/v1/admin/login` -> Bearer-токен
- `GET /api/v1/admin/schedules`
- `POST /api/v1/admin/schedules/upsert`
- `PUT /api/v1/admin/schedules/{entry_id}`
- `DELETE /api/v1/admin/schedules/{entry_id}`
- `GET /api/v1/admin/proposals`
- `POST /api/v1/admin/proposals/{proposal_id}/accept`
- `POST /api/v1/admin/proposals/{proposal_id}/reject`

---

## Запуск Android-приложения (`android-app/`)

1. Откройте `android-app/` в Android Studio.
2. Выполните `Gradle Sync`.
3. Запустите приложение на эмуляторе или устройстве.
4. В настройках приложения заполните:
   - URL сервера
   - Ключ чтения
   - Ключ отправки
   - Студенческую группу
   - Тему и правила уведомлений

### Сборка APK
- В Android Studio: `Build -> Build Bundle(s) / APK(s) -> Build APK(s)`
- Либо через командную строку (если есть wrapper):
```bash
./gradlew assembleDebug
```

---

## Примечания
- Учётная запись суперпользователя создаётся автоматически при запуске backend (из переменных окружения).
- Приоритеты пар сохраняются локально на устройстве (Room DB).
- Периодический worker проверяет ближайшие пары и отправляет уведомления по правилам приоритетов.
- Для устранения `java.lang.OutOfMemoryError: Java heap space` при сборке APK увеличены лимиты памяти Gradle в `android-app/gradle.properties`.
