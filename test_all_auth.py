import sys
import os

# Добавляем папку AuthAPI в путь поиска модулей, так как файлы лежат там
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), 'AuthAPI')))

import grpc

try:
    import auth_pb2
    import auth_pb2_grpc
except ImportError:
    print("Ошибка: Не найдены сгенерированные файлы auth_pb2.py и auth_pb2_grpc.py в папке AuthAPI.")
    exit(1)

def run_tests():
    # Порт, на котором запущен ваш gRPC-сервис auth-api (обычно 50051)
    server_address = 'localhost:50051'
    print(f"Подключение к gRPC серверу по адресу {server_address}...\n")
    
    with grpc.insecure_channel(server_address) as channel:
        stub = auth_pb2_grpc.AuthServiceStub(channel)
        
        test_email = "student21@school21.ru"
        test_password = "InitialPassword123"
        new_password = "NewStrongPassword456"
        
        # 1. ТЕСТ: Регистрация (Register)
        print("-----------------------------------------")
        print("1. Тестирование метода: Register")
        print("-----------------------------------------")
        try:
            reg_request = auth_pb2.RegisterRequest(
                email=test_email,
                first_name="Питон",
                last_name="Разработчик",
                phone="+79990000000",
                password=test_password
            )
            reg_response = stub.Register(reg_request)
            print(f"✅ Успешно вызван Register")
            print(f"   Token: {reg_response.token}")
            print(f"   Error: {reg_response.error}")
        except grpc.RpcError as e:
            print(f"❌ Ошибка RPC [{e.code()}]: {e.details()}")

        # 2. ТЕСТ: Вход / Авторизация (Login)
        print("\n-----------------------------------------")
        print("2. Тестирование метода: Login")
        print("-----------------------------------------")
        jwt_token = ""
        try:
            login_request = auth_pb2.LoginRequest(
                email=test_email,
                password=test_password
            )
            login_response = stub.Login(login_request)
            print(f"✅ Успешно вызван Login")
            print(f"   Token: {login_response.token}")
            print(f"   Error: {login_response.error}")
            jwt_token = login_response.token
        except grpc.RpcError as e:
            print(f"❌ Ошибка RPC [{e.code()}]: {e.details()}")

        if not jwt_token:
            jwt_token = "placeholder_token_if_login_failed"

        # 3. ТЕСТ: Проверка токена (ValidateToken)
        print("\n-----------------------------------------")
        print("3. Тестирование метода: ValidateToken")
        print("-----------------------------------------")
        try:
            val_request = auth_pb2.ValidateTokenRequest(token=jwt_token)
            val_response = stub.ValidateToken(val_request)
            print(f"✅ Успешно вызван ValidateToken")
            print(f"   Is Valid: {val_response.is_valid}")
            print(f"   Email: {val_response.email}")
            print(f"   Role: {val_response.role}")
            print(f"   Error: {val_response.error}")
        except grpc.RpcError as e:
            print(f"❌ Ошибка RPC [{e.code()}]: {e.details()}")

        # 4. ТЕСТ: Смена пароля (ChangePassword)
        print("\n-----------------------------------------")
        print("4. Тестирование метода: ChangePassword")
        print("-----------------------------------------")
        try:
            ch_request = auth_pb2.ChangePasswordRequest(
                token=jwt_token,
                old_password=test_password,
                new_password=new_password
            )
            ch_response = stub.ChangePassword(ch_request)
            print(f"✅ Успешно вызван ChangePassword")
            print(f"   Success: {ch_response.success}")
            print(f"   Error: {ch_response.error}")
        except grpc.RpcError as e:
            print(f"❌ Ошибка RPC [{e.code()}]: {e.details()}")

        # 5. ТЕСТ: Сброс пароля (ResetPassword)
        print("\n-----------------------------------------")
        print("5. Тестирование метода: ResetPassword")
        print("-----------------------------------------")
        try:
            reset_request = auth_pb2.ResetPasswordRequest(email=test_email)
            reset_response = stub.ResetPassword(reset_request)
            print(f"✅ Успешно вызван ResetPassword")
            print(f"   Success: {reset_response.success}")
            print(f"   Message: {reset_response.message}")
        except grpc.RpcError as e:
            print(f"❌ Ошибка RPC [{e.code()}]: {e.details()}")

if __name__ == '__main__':
    run_tests()