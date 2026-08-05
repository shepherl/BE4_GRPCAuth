import os
import sys
import uuid

import grpc

sys.path.append(os.path.abspath(os.path.dirname(__file__)))
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '..')))

try:
    import auth_pb2
    import auth_pb2_grpc
except ImportError:
    from AuthAPI import auth_pb2, auth_pb2_grpc

SERVER_ADDRESS = 'localhost:50051'

def run_tests():
    print(f"Подключение к gRPC серверу {SERVER_ADDRESS}...\n")
    
    with grpc.insecure_channel(SERVER_ADDRESS) as channel:
        stub = auth_pb2_grpc.AuthServiceStub(channel)
        
        # Генерация случайного email для каждого прогона
        test_email = f"student21_{uuid.uuid4().hex[:8]}@school21.ru"
        test_password = "Password123"
        
        # 1. Регистрация нового пользователя
        print("1. Тест метода Register...")
        try:
            resp = stub.Register(auth_pb2.RegisterRequest(
                email=test_email, first_name="Тест", last_name="Статусов", 
                phone="+79991112233", password=test_password
            ))
            print(f"   ✅ Зарегистрирован ({test_email}). Токен получен: {bool(resp.token)}")
        except grpc.RpcError as e:
            print(f"   ❌ gRPC Ошибка [{e.code()}]: {e.details()}")

        # 2. Попытка повторной регистрации ТЕГО ЖЕ email (Проверка ALREADY_EXISTS)
        print("\n2. Тест ошибки дубликата (Register -> ALREADY_EXISTS)...")
        try:
            stub.Register(auth_pb2.RegisterRequest(
                email=test_email, first_name="Дубль", last_name="Дубль", 
                phone="+79991112233", password=test_password
            ))
            print("   ❌ Ошибка: Сервер не заблокировал дубликат.")
        except grpc.RpcError as e:
            print(f"   ✅ Успешно перехвачен статус {e.code()}: '{e.details()}'")

        # 3. Попытка входа с неверным паролем (Проверка UNAUTHENTICATED)
        print("\n3. Тест неверного пароля (Login -> UNAUTHENTICATED)...")
        try:
            stub.Login(auth_pb2.LoginRequest(email=test_email, password="WrongPassword"))
            print("   ❌ Ошибка: Сервер пропустил неверный пароль.")
        except grpc.RpcError as e:
            print(f"   ✅ Успешно перехвачен статус {e.code()}: '{e.details()}'")

        # 4. Проверка невалидного токена (ValidateToken -> UNAUTHENTICATED)
        print("\n4. Тест невалидного токена (ValidateToken -> UNAUTHENTICATED)...")
        try:
            stub.ValidateToken(auth_pb2.ValidateTokenRequest(token="invalid_jwt_token"))
            print("   ❌ Ошибка: Сервер принял невалидный токен.")
        except grpc.RpcError as e:
            print(f"   ✅ Успешно перехвачен статус {e.code()}: '{e.details()}'")

if __name__ == '__main__':
    run_tests()