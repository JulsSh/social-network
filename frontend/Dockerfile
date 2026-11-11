# =============================================================================
# Multi-stage Dockerfile for Vue 3 Frontend Application
# =============================================================================
# 
# Stage 1: Build - Сборка production bundle
# Stage 2: Serve - Nginx сервер для раздачи статики
#
# Использование:
#   docker build -t frontend-vue3 .
#   docker run -p 80:80 frontend-vue3
# =============================================================================

# -----------------------------------------------------------------------------
# Stage 1: Build
# -----------------------------------------------------------------------------
FROM node:18-alpine AS build

WORKDIR /app

# Копируем файлы зависимостей для лучшего кэширования
COPY package*.json ./

# Устанавливаем зависимости
RUN npm ci --prefer-offline

# Копируем исходный код
COPY . .

# Собираем production bundle
RUN npm run build

# -----------------------------------------------------------------------------
# Stage 2: Serve
# -----------------------------------------------------------------------------
FROM nginx:alpine AS serve

# Копируем собранное приложение
COPY --from=build /app/dist /usr/share/nginx/html

# Копируем конфигурацию Nginx
COPY ./frontnginx/server.conf /etc/nginx/conf.d/default.conf

# Порт приложения
EXPOSE 80

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -fsS http://127.0.0.1/ || exit 1

# Запуск Nginx
CMD ["nginx", "-g", "daemon off;"]
