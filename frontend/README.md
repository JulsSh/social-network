# Social Network Frontend (Vue 3)

Фронтенд-приложение для социальной сети, построенное на Vue 3 с микросервисной архитектурой бэкенда.

## 📋 Содержание

- [Обзор проекта](#обзор-проекта)
- [Технологии](#технологии)
- [Архитектура проекта](#архитектура-проекта)
- [Структура файлов](#структура-файлов)
- [Установка и запуск](#установка-и-запуск)
- [Конфигурация](#конфигурация)
- [API и бэкенд](#api-и-бэкенд)
- [CI/CD](#cicd)

---

## Обзор проекта

Это SPA (Single Page Application) для социальной сети с функционалом:

- **Аутентификация** — регистрация, авторизация, восстановление пароля, JWT токены
- **Профили пользователей** — просмотр и редактирование профилей
- **Новостная лента** — посты, реакции, комментарии
- **Друзья** — добавление, поиск друзей
- **Сообщения** — личные диалоги через WebSocket
- **Уведомления** — push-уведомления в реальном времени
- **Настройки** — персонализация аккаунта
- **Поиск** — глобальный поиск по пользователям и контенту

---

## Технологии

### Основной стек


| Технология | Версия | Описание                                                       |
| -------------------- | ------------ | ---------------------------------------------------------------------- |
| **Vue.js**           | 3.3.4        | Основной фреймворк                                    |
| **Vue Router**       | 4.2.5        | Маршрутизация SPA                                         |
| **Vuex**             | 4.1.0        | Централизованное хранилище состояния |
| **Axios**            | 1.5.1        | HTTP-клиент для API запросов                          |
| **WebSocket**        | Native       | Реалтайм-коммуникации                              |

### UI и компоненты


| Библиотека       | Описание                              |
| -------------------------- | --------------------------------------------- |
| **vue3-toastify**          | Всплывающие уведомления |
| **@vuepic/vue-datepicker** | Выбор даты                           |
| **vue-select**             | Выпадающие списки             |
| **vue3-emoji-picker**      | Эмодзи                                  |
| **@tiptap**                | Rich-text редактор                    |
| **v-tooltip**              | Тултипы                                |

### Стили и оформление


| Инструмент | Описание                                            |
| -------------------- | ----------------------------------------------------------- |
| **Stylus**           | CSS-препроцессор                                |
| **Day.js**           | Работа с датами (локализация ru/en) |

### Валидация


| Библиотека | Описание            |
| -------------------- | --------------------------- |
| **@vuelidate**       | Валидация форм |

### Сборка


| Инструмент | Версия | Описание                 |
| -------------------- | ------------ | -------------------------------- |
| **Vue CLI**          | 5.0.8        | Сборка и dev-сервер |
| **Webpack**          | 5.88.2       | Бандлер                   |
| **Babel**            | 7.23.0       | Транспиляция JS      |
| **ESLint**           | 8.50.0       | Линтинг кода          |

---

## Архитектура проекта

### Общая схема

```
┌─────────────────────────────────────────────────────────────────┐
│                        Browser (SPA)                            │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    Vue 3 Application                     │   │
│  │  ┌────────────┐ ┌────────────┐ ┌────────────────────┐    │   │
│  │  │   Router   │ │   Vuex     │ │     Components     │    │   │
│  │  │  (pages)   │ │  (store)   │ │    (UI blocks)     │    │   │
│  │  └────────────┘ └────────────┘ └────────────────────┘    │   │
│  └──────────────────────────────────────────────────────────┘   │
│                              │                                  │
│                    ┌─────────┴─────────┐                        │
│                    ▼                   ▼                        │
│              HTTP (Axios)        WebSocket                      │
│                    │                   │                        │
└────────────────────┼───────────────────┼────────────────────────┘
                     │                   │
                     ▼                   ▼
        ┌────────────────────────────────────────┐
        │         API Gateway (:8765)             │
        │         /api/v1/*                       │
        └────────────────────────────────────────┘
                           │
          ┌────────────────┼────────────────┐
          ▼                ▼                ▼
    ┌──────────┐    ┌──────────┐    ┌──────────┐
    │   Auth   │    │   User   │    │   Chat   │
    │ Service  │    │ Service  │    │ Service  │
    └──────────┘    └──────────┘    └──────────┘
```

### Layouts (Лейауты)

Приложение использует 3 типа лейаутов:

1. **MainLayout** — основной лейаут для авторизованных пользователей

   - Header с навигацией и профилем
   - Sidebar с меню
   - Основной контент
   - Компонент реалтайм-обновлений
2. **FormLayout** — лейаут для форм авторизации

   - Информационный блок
   - Форма (логин/регистрация)
   - Анимированные волны
3. **EmptyLayout** — пустой лейаут для специальных страниц

### Маршрутизация

```
/login              — Авторизация
/registration       — Регистрация  
/forgot             — Восстановление пароля
/change-password    — Изменение пароля

/                   — Новостная лента (главная)
/profile            — Мой профиль
/profile/:id        — Профиль пользователя
/friends            — Список друзей
/friends/find       — Поиск друзей
/im                 — Сообщения
/im/:activeDialogId — Конкретный диалог
/push               — Уведомления
/settings           — Настройки
/search             — Глобальный поиск
```

### Vuex Store

Централизованное хранилище состояния:

```
store/
├── index.js          — Корневой store
├── auth/             — Модуль аутентификации
│   ├── api.js        — JWT токены, login/logout/refresh
│   ├── captcha.js    — Капча для форм
│   ├── info.js       — Информация о пользователе
│   └── languages.js  — Мультиязычность
├── profile/          — Модуль профиля
│   ├── info.js       — Данные профиля
│   ├── friends.js    — Друзья
│   ├── feeds.js      — Посты
│   ├── dialogs.js    — Диалоги
│   ├── comments.js   — Комментарии
│   └── notifications.js — Уведомления
├── global/           — Глобальные модули
│   ├── alert.js      — Уведомления пользователя
│   ├── search.js     — Поиск
│   ├── likes.js      — Лайки
│   └── storage.js    — Загрузка файлов
└── users/            — Другие пользователи
```

---

## Структура файлов

```
frontend-vue3/
├── public/                    # Статические файлы
│   ├── css/                   # Глобальные стили
│   ├── fonts/                 # Шрифты
│   ├── img/                   # Изображения
│   ├── static/                # Статичные ресурсы
│   └── index.html             # HTML-шаблон
│
├── src/                       # Исходный код
│   ├── assets/                # Ресурсы компонентов
│   │   ├── stylus/            # Stylus стили
│   │   │   ├── base/          # Базовые стили и переменные
│   │   │   └── main.styl      # Главный файл стилей
│   │   └── static/            # Статичные изображения
│   │
│   ├── components/            # Vue компоненты
│   │   ├── MainLayout/        # Header, Sidebar
│   │   ├── FormLayout/        # Компоненты форм
│   │   ├── Profile/           # Профиль пользователя
│   │   ├── News/              # Новостная лента
│   │   ├── Friends/           # Друзья
│   │   ├── Im/                # Сообщения (чат)
│   │   ├── Comments/          # Комментарии
│   │   ├── Search/            # Поиск
│   │   ├── Settings/          # Настройки
│   │   ├── Push/              # Уведомления
│   │   ├── Modal.vue          # Модальные окна
│   │   └── FormElements/      # Элементы форм
│   │
│   ├── composables/           # Vue 3 composables
│   │   ├── usePhoneFormatter.js
│   │   └── useTranslations.js
│   │
│   ├── directives/            # Пользовательские директивы
│   │   ├── scroll.js          # Директива скролла
│   │   └── pattern.js         # Директива паттерна ввода
│   │
│   ├── Icons/                 # SVG иконки как компоненты
│   │
│   ├── layouts/               # Лейауты страниц
│   │   ├── MainLayout.vue
│   │   ├── FormLayout.vue
│   │   └── EmptyLayout.vue
│   │
│   ├── pages/                 # Страницы (views)
│   │   ├── Auth/              # Страницы авторизации
│   │   │   ├── Login.vue
│   │   │   ├── Registration.vue
│   │   │   ├── Forgot.vue
│   │   │   └── ChangePassword.vue
│   │   └── User/              # Страницы пользователя
│   │       ├── News.vue
│   │       ├── Profile.vue
│   │       ├── Friends.vue
│   │       ├── Im.vue
│   │       └── Settings.vue
│   │
│   ├── plugins/               # Vue плагины
│   │   └── websocket.js       # WebSocket клиент
│   │
│   ├── requests/              # API запросы (Axios)
│   │   ├── auth.js            # Аутентификация
│   │   ├── account.js         # Аккаунт
│   │   ├── posts.js           # Посты
│   │   ├── friends.js         # Друзья
│   │   ├── dialogs.js         # Диалоги
│   │   └── settings.js        # Настройки
│   │
│   ├── router/                # Vue Router
│   │   ├── index.js           # Основная конфигурация
│   │   └── modules/           # Модули маршрутов
│   │       ├── auth.js
│   │       ├── user.js
│   │       └── admin.js
│   │
│   ├── settings/              # Настройки приложения
│   │   ├── axios.js           # Конфигурация Axios
│   │   └── router.js          # Настройки маршрутизатора
│   │
│   ├── store/                 # Vuex хранилище
│   │
│   ├── utils/                 # Утилиты
│   │   ├── createPagination.js
│   │   └── notifications.utils.js
│   │
│   ├── App.vue                # Корневой компонент
│   └── main.js                # Точка входа
│
├── frontnginx/                # Конфигурация Nginx для Docker
│   └── server.conf
│
├── Dockerfile                 # Docker образ
├── nginx.conf                 # Nginx конфигурация (production)
├── nginx-local.conf           # Nginx конфигурация (local)
├── .gitlab-ci.yml             # CI/CD для GitLab
├── package.json               # Зависимости проекта
├── vue.config.js              # Конфигурация Vue CLI
└── babel.config.js            # Конфигурация Babel
```

---

## Установка и запуск

### Требования

- **Node.js** >= 18.x
- **npm** >= 9.x или **yarn** >= 1.22

### Локальный запуск (Development)

```bash
# Клонировать репозиторий
git clone <repository-url>
cd frontend-vue3

# Установить зависимости
npm install
# или
yarn install

# Запустить dev-сервер
npm run serve
# или
yarn serve
```

Приложение будет доступно по адресу: **http://localhost:8088**

### Сборка для production

```bash
npm run build
# или
yarn build
```

Собранные файлы будут в папке `dist/`

### Линтинг кода

```bash
npm run lint
# или
yarn lint
```

### Запуск через Docker

#### Вариант 1: Сборка и запуск образа

```bash
# Сборка Docker образа
docker build -t social-network-frontend .

# Запуск контейнера
docker run -d -p 80:80 --name frontend social-network-frontend
```

Приложение будет доступно по адресу: **http://localhost**

#### Вариант 2: Docker Compose (рекомендуется)

Создайте файл `docker-compose.yml`:

```yaml
version: '3.8'

services:
  frontend:
    build: .
    ports:
      - "80:80"
    restart: unless-stopped
    environment:
      - NODE_ENV=production
```

Запуск:

```bash
docker-compose up -d
```

#### Вариант 3: Локальный запуск с Docker (для разработки)

```bash
# Сборка образа для локальной разработки
docker build -t frontend-local .

# Запуск с примонтированным volume для hot-reload
docker run -d \
  -p 88:80 \
  -v $(pwd)/dist:/usr/share/nginx/html \
  --name frontend-local \
  frontend-local
```

---

## Конфигурация

### API Endpoints

Основные настройки API находятся в:

**`src/settings/axios.js`**

```javascript
// Production (текущее значение)
axios.defaults.baseURL = "http://89.111.155.206:8765/api/v1/";

// Для локальной разработки раскомментируйте:
// axios.defaults.baseURL = "/api/v1/";
// axios.defaults.baseURL = "http://localhost:8765/api/v1/";
```

**`src/plugins/websocket.js`**

```javascript
// Production
const serverUrl = 'ws://89.111.155.206:8765/api/v1/streaming/ws';

// Для локальной разработки:
// const serverUrl = 'ws://localhost:8765/api/v1/streaming/ws';
```

### Proxy для разработки

**`vue.config.js`**

```javascript
devServer: {
  proxy: 'http://localhost:8088'
  // Или для подключения к реальному бэкенду:
  // proxy: 'http://89.111.155.206:8080'
}
```

### Переменные окружения

Рекомендуется создать файл `.env.local` для локальных настроек:

```env
VUE_APP_API_URL=http://localhost:8765/api/v1/
VUE_APP_WS_URL=ws://localhost:8765/api/v1/streaming/ws
```

---

## API и бэкенд

### Структура API запросов

Все API запросы находятся в папке `src/requests/`:


| Файл           | Описание                                                                   |
| ------------------ | ---------------------------------------------------------------------------------- |
| `auth.js`          | Регистрация, логин, logout, refresh token, смена пароля |
| `account.js`       | Управление аккаунтом                                            |
| `posts.js`         | Создание, редактирование, удаление постов      |
| `friends.js`       | Друзья, запросы в друзья                                       |
| `dialogs.js`       | Сообщения и диалоги                                               |
| `comments.js`      | Комментарии к постам                                             |
| `likes.js`         | Лайки и реакции                                                       |
| `notifications.js` | Уведомления                                                             |
| `search.js`        | Поиск                                                                         |
| `storage.js`       | Загрузка файлов                                                      |
| `geo.js`           | Геолокация                                                               |
| `languages.js`     | Локализация                                                             |

### Авторизация

Используется JWT авторизация:

1. При логине сервер возвращает `accessToken` и `refreshToken`
2. `accessToken` хранится в localStorage и добавляется в заголовок `Authorization`
3. При истечении токена автоматически происходит refresh через `refreshToken`
4. При ошибке 401 пользователь перенаправляется на страницу логина

### WebSocket

WebSocket используется для:

- Реалтайм-сообщений в чате
- Уведомлений о новых событиях
- Обновления статуса пользователей

Подключение: `ws://<host>:8765/api/v1/streaming/ws`

---

## CI/CD

### GitLab CI/CD

Файл `.gitlab-ci.yml` содержит 3 этапа:

1. **build** — сборка приложения

   - Установка зависимостей
   - Сборка production bundle
2. **deploy_docker** — публикация Docker образа

   - Сборка Docker образа
   - Push в Docker Hub
3. **deploy_prod** — деплой на production (закомментирован)

   - SSH деплой на Kubernetes

### Запуск CI/CD

Пайплайн запускается вручную (manual) для веток:

- `testFrontend`
- `master` (закомментирован)

### Docker образ

Образ публикуется в Docker Hub:

```
skillboxgroup40/frontend-vue3
```

---

## Разработка

### Структура компонента

Компоненты используют Composition API (Vue 3):

```vue
<template>
  <!-- HTML шаблон -->
</template>

<script>
import { ref, computed } from 'vue';
import { useStore } from 'vuex';

export default {
  name: 'ComponentName',
  setup() {
    const store = useStore();
    // Логика компонента
    return { /* reactive data */ };
  }
};
</script>

<style lang="stylus">
// Stylus стили
</style>
```

### Добавление новой страницы

1. Создайте компонент в `src/pages/`
2. Добавьте маршрут в `src/router/modules/`
3. При необходимости создайте модуль store в `src/store/`

### Стилизация

- Используйте Stylus для компонентов
- Переменные в `src/assets/stylus/base/vars.styl`
- Глобальные стили в `public/css/style.min.css`

---

## Полезные команды

```bash
# Установка зависимостей
npm install

# Запуск dev-сервера
npm run serve

# Сборка production
npm run build

# Линтинг
npm run lint

# Обновление browserslist
npx update-browserslist-db@latest

# Docker сборка
docker build -t frontend .

# Docker запуск
docker run -p 80:80 frontend
```

---
