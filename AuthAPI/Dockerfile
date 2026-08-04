# /AuthAPI/Dockerfile
FROM python:3.12-slim

WORKDIR /app

# Устанавливаем системные зависимости для сборки (нужны для grpcio)
RUN apt-get update && apt-get install -y --no-install-recommends \
    gcc \
    python3-dev \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

EXPOSE 50051

CMD ["python", "main.py"]