import os
import sys
import uuid

import grpc

sys.path.append(os.path.abspath(os.path.dirname(__file__)))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

try:
    import auth_pb2
    import auth_pb2_grpc
except ImportError:
    from AuthAPI import auth_pb2, auth_pb2_grpc


def run():
    server_address = "localhost:50051"
    print(f"Подключение к gRPC серверу по адресу {server_address}...")

    channel = grpc.insecure_channel(server_address)
    stub = auth_pb2_grpc.AuthServiceStub(channel)

    # === БЛОК 1: Проверка ВАЛИДНОГО токена ===
    try:
        test_email = f"client_test_{uuid.uuid4().hex[:6]}@school21.ru"
        print(f"\n1️⃣ Регистрируем тестового пользователя: {test_email}")

        reg_response = stub.Register(
            auth_pb2.RegisterRequest(
                email=test_email,
                first_name="Иван",
                last_name="Иванов",
                phone="+79991234567",
                password="SecurePassword123",
            )
        )

        real_token = reg_response.token
        print(f"   ✅ Пользователь создан. Получен токен: {real_token[:15]}...")

        print("\n2️⃣ Проверяем ВАЛИДНЫЙ токен...")
        val_response = stub.ValidateToken(
            auth_pb2.ValidateTokenRequest(token=real_token)
        )
        print(
            f"   ✅ Запрос успешен! Результат проверки (is_valid): {val_response.is_valid}"
        )

    except grpc.RpcError as e:
        print(f"\n❌ gRPC ошибка в блоке валидного токена [{e.code()}]: {e.details()}")

    # === БЛОК 2: Проверка НЕВАЛИДНОГО токена ===
    print("\n3️⃣ Проверяем НЕВАЛИДНЫЙ (фейковый) токен...")
    try:
        # Имитируем структуру JWT (header.payload.signature), но с неверными данными
        fake_token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.fake_payload.fake_signature"

        stub.ValidateToken(auth_pb2.ValidateTokenRequest(token=fake_token))

        # Если код дошел сюда, значит сервер пропустил фейк (ЭТО ПЛОХО!)
        print("   ❌ КРИТИЧЕСКАЯ ОШИБКА: Сервер принял фейковый токен!")

    except grpc.RpcError as e:
        # Если мы поймали ошибку UNAUTHENTICATED, значит защита сработала штатно
        if e.code() == grpc.StatusCode.UNAUTHENTICATED:
            print(
                f"   ✅ Успех! Сервер отклонил подделку. Статус: {e.code()} — '{e.details()}'"
            )
        else:
            print(
                f"   ⚠️ Сервер вернул неожиданную ошибку: {e.code()} — '{e.details()}'"
            )


if __name__ == "__main__":
    run()
