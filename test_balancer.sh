#!/bin/bash

# Количество запросов для теста
REQUESTS=20
URL="https://s21shop.duckdns.org/api/v1/products/available"

echo "==============================================="
echo "Начинаем тест балансировщика Nginx (GET запросы)"
echo "Отправляем $REQUESTS запросов на $URL"
echo "==============================================="

# Массив для подсчета ответов от каждого IP
declare -A server_counts

for i in $(seq 1 $REQUESTS); do
    # Отправляем настоящий GET-запрос (чтобы сработал балансировщик для GET), 
    # сохраняем только заголовки и вытаскиваем нужный
    UPSTREAM=$(curl -s -D - -o /dev/null -X GET $URL | grep -i "x-upstream-server" | awk '{print $2}' | tr -d '\r')
    
    if [ -z "$UPSTREAM" ]; then
        UPSTREAM="Неизвестно (Возможно, кэш или ошибка)"
    fi

    echo "Запрос $i обработал сервер: $UPSTREAM"
    
    # Увеличиваем счетчик для этого сервера
    ((server_counts["$UPSTREAM"]++))
done

echo ""
echo "==============================================="
echo "ИТОГОВОЕ РАСПРЕДЕЛЕНИЕ НАГРУЗКИ:"
echo "==============================================="
for server in "${!server_counts[@]}"; do
    echo "Сервер $server ответил раз: ${server_counts[$server]}"
done

echo ""
echo "Подсказка: По умолчанию в Nginx у нас настроено вес 2:1:1"
echo "(master=2, replica1=1, replica2=1)."
