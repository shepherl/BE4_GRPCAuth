import sys
import os
import grpc

# Добавляем папку AuthAPI в путь поиска сгенерированных protobuf-файлов
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), 'AuthAPI')))

import auth_pb2
import auth_pb2_grpc

def run():
    server_address = 'localhost:50051'
    print(f"Подключение к gRPC серверу по адресу {server_address}...")
    
    channel = grpc.insecure_channel(server_address)
    stub = auth_pb2_grpc.AuthServiceStub(channel)
    
    try:
        # Пример вызова метода ValidateToken с тестовым токеном
        request = auth_pb2.ValidateTokenRequest(token="sample_test_token")
        response = stub.ValidateToken(request)
        
        print("✅ Канал gRPC успешно открыт, соединение с auth-api установлено.")
        print(f"   Результат проверки токена (is_valid): {response.is_valid}")
        print(f"   Сообщение об ошибке (если есть): {response.error}")
        
    except grpc.RpcError as e:
        print(f"❌ gRPC ошибка [{e.code()}]: {e.details()}")

if __name__ == '__main__':
    run()