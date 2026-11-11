#!/bin/bash
# =============================================================================
# Скрипт для диагностики Gateway и подключения микросервисов к сети
# =============================================================================

echo "🔍 Диагностика текущего состояния..."
echo ""

echo "1️⃣ Проверка портов Gateway..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
docker logs mc-gateway 2>&1 | grep -iE "netty|started|port|listening" | tail -10
echo ""

echo "2️⃣ Проверка портов Eureka..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
docker logs eureka-server 2>&1 | grep -iE "started|port|tomcat" | tail -10
echo ""

echo "3️⃣ Текущие сети контейнеров..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "mc-gateway:"
docker inspect mc-gateway | grep -A 10 '"Networks"'
echo ""
echo "eureka-server:"
docker inspect eureka-server | grep -A 10 '"Networks"'
echo ""
echo "mc-auth:"
docker inspect mc-auth | grep -A 10 '"Networks"'
echo ""

echo "4️⃣ Подключение микросервисов к social-network-net..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Функция для подключения контейнера к сети
connect_to_network() {
    local container=$1
    echo "Подключение $container..."

    # Проверяем, уже подключен ли
    if docker network inspect social-network-net | grep -q "\"Name\": \"$container\""; then
        echo "  ✅ $container уже в сети social-network-net"
    else
        docker network connect social-network-net $container && \
        echo "  ✅ $container успешно подключен" || \
        echo "  ❌ Ошибка при подключении $container"
    fi
}

# Подключаем все микросервисы
connect_to_network "mc-gateway"
connect_to_network "eureka-server"
connect_to_network "mc-auth"

echo ""
echo "5️⃣ Проверка связности между контейнерами..."
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

echo "Frontend → Gateway (проверка резолва):"
docker exec frontend-vue3 nslookup mc-gateway 2>/dev/null || \
docker exec frontend-vue3 getent hosts mc-gateway || \
echo "  ⚠️  DNS резолв не работает"

echo ""
echo "Frontend → Gateway (проверка подключения через возможные порты):"
for port in 8765 8080 8084 9090; do
    echo -n "  Порт $port: "
    docker exec frontend-vue3 timeout 2 nc -zv mc-gateway $port 2>&1 | grep -q succeeded && \
    echo "✅ ОТКРЫТ" || echo "❌ закрыт"
done

echo ""
echo "6️⃣ Список портов, которые слушает Gateway:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
docker exec mc-gateway netstat -tulpn 2>/dev/null | grep LISTEN || \
docker exec mc-gateway ss -tulpn 2>/dev/null | grep LISTEN || \
echo "  ⚠️  Не удалось получить список портов (netstat/ss не установлен)"

echo ""
echo "7️⃣ Актуальное состояние сети social-network-net:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
docker network inspect social-network-net | grep -A 3 '"Name"' | grep -E '"Name"|IPv4Address'

echo ""
echo "✅ Диагностика завершена!"
echo ""
echo "📋 Следующие шаги:"
echo "1. Если Gateway слушает порт, отличный от 8765 - обновите frontnginx/server.conf"
echo "2. Пересоберите и задеплойте фронтенд через GitLab CI/CD"
echo "3. Проверьте в браузере - ошибка должна измениться с 503 на 401/200"

