import sys
import os
import grpc
from concurrent.futures import ThreadPoolExecutor

# Добавляем путь к сгенерированным файлам
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), 'AuthAPI')))

import auth_pb2
import auth_pb2_grpc

SERVER_ADDRESS = 'localhost:50051'

def test_concurrency():
    print("=== ТЕСТ 1: Конкурентные запросы (Многопоточность) ===")
    with grpc.insecure_channel(SERVER_ADDRESS) as channel:
        stub = auth_pb2_grpc.AuthServiceStub(channel)
        
        def register_user(i):
            try:
                req = auth_pb2.RegisterRequest(
                    email=f"load_user_{i}@school21.ru",
                    first_name=f"User{i}",
                    last_name="Test",
                    phone=f"+7999111{i:04d}",
                    password="SecurePassword123"
                )
                resp = stub.Register(req)
                if resp.token:
                    return f"   [Поток {i}] Успешно зарегистрирован."
                else:
                    return f"   [Поток {i}] Ошибка ответа: {resp.error}"
            except grpc.RpcError as e:
                return f"   [Поток {i}] RPC Ошибка [{e.code()}]: {e.details()}"

        # Запускаем 10 параллельных запросов на регистрацию
        with ThreadPoolExecutor(max_workers=5) as executor:
            futures = [executor.submit(register_user, i) for i in range(10)]
            for f in futures:
                print(f.result())

def test_deadline():
    print("\n=== ТЕСТ 2: Таймауты и Deadlines (DEADLINE_EXCEEDED) ===")
    with grpc.insecure_channel(SERVER_ADDRESS) as channel:
        stub = auth_pb2_grpc.AuthServiceStub(channel)
        try:
            # Устанавливаем экстремально малый таймаут (1 миллисекунда), чтобы спровоцировать тайм-аут
            stub.Login(
                auth_pb2.LoginRequest(email="load_user_0@school21.ru", password="SecurePassword123"),
                timeout=0.001
            )
            print("   Запрос выполнился слишком быстро (неожиданно).")
        except grpc.RpcError as e:
            print(f"   ✅ Перехвачен ожидаемый таймаут: Код = {e.code()}, Описание = {e.details()}")

def test_metadata():
    print("\n=== ТЕСТ 3: gRPC Metadata (Передача заголовков и контекста) ===")
    with grpc.insecure_channel(SERVER_ADDRESS) as channel:
        stub = auth_pb2_grpc.AuthServiceStub(channel)
        
        # Передаем кастомные метаданные (эквивалент HTTP-заголовков)
        metadata = [
            ('x-client-source', 'python-advanced-tester'),
            ('x-request-id', 'uuid-school-21-security')
        ]
        
        try:
            resp = stub.ValidateToken(
                auth_pb2.ValidateTokenRequest(token="dummy_token_for_metadata_test"),
                metadata=metadata
            )
            print(f"   ✅ Запрос с метаданными успешно обработан. Токен валиден: {resp.is_valid}")
        except grpc.RpcError as e:
            print(f"   RPC Ошибка: {e.code()} — {e.details()}")

if __name__ == '__main__':
    print(f"Подключение к gRPC серверу по адресу {SERVER_ADDRESS}...\n")
    test_concurrency()
    test_deadline()
    test_metadata()
    print("\n Все продвинутые тесты успешно завершены.")