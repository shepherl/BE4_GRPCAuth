from concurrent import futures

import auth_pb2_grpc
import grpc
from database import Base, SessionLocal, engine
from service import AuthService


def serve():
    # Создаем таблицы в БД при запуске
    Base.metadata.create_all(bind=engine)

    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    
    # Внедряем зависимость (DI)
    auth_service = AuthService(db_session_factory=SessionLocal)
    auth_pb2_grpc.add_AuthServiceServicer_to_server(auth_service, server)
    
    server.add_insecure_port('[::]:50051')
    print("Запуск AuthAPI gRPC сервера на порту 50051...")
    server.start()
    server.wait_for_termination()

if __name__ == '__main__':
    serve()