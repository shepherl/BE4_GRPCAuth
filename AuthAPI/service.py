import datetime
import random
import string

import auth_pb2
import auth_pb2_grpc
import bcrypt
import jwt
from models import User

SECRET_KEY = "very_super_secret_jwt_key"

class AuthService(auth_pb2_grpc.AuthServiceServicer):
    def __init__(self, db_session_factory):
        self.db_session_factory = db_session_factory

    def _hash_password(self, password: str) -> str:
        salt = bcrypt.gensalt()
        return bcrypt.hashpw(password.encode('utf-8'), salt).decode('utf-8')

    def _check_password(self, password: str, hashed: str) -> bool:
        return bcrypt.checkpw(password.encode('utf-8'), hashed.encode('utf-8'))

    def _create_token(self, email: str, role: str = "USER") -> str:
        payload = {
            "email": email,
            "role": role,
            # Используем timezone-aware datetime для Python 3.12
            "exp": datetime.datetime.now(datetime.timezone.utc) + datetime.timedelta(hours=24)
        }
        return jwt.encode(payload, SECRET_KEY, algorithm="HS256")

    def Register(self, request, context):
        with self.db_session_factory() as db:
            if db.query(User).filter(User.email == request.email).first():
                return auth_pb2.AuthResponse(error="Пользователь с таким email уже существует")
            
            new_user = User(
                email=request.email,
                first_name=request.first_name,
                last_name=request.last_name,
                phone=request.phone,
                password_hash=self._hash_password(request.password)
            )
            db.add(new_user)
            db.commit()
            
            token = self._create_token(request.email)
            return auth_pb2.AuthResponse(token=token, error="")

    def Login(self, request, context):
        with self.db_session_factory() as db:
            user = db.query(User).filter(User.email == request.email).first()
            if not user or not self._check_password(request.password, user.password_hash):
                return auth_pb2.AuthResponse(error="Неверный email или пароль")
            
            token = self._create_token(user.email)
            return auth_pb2.AuthResponse(token=token, error="")

    def ValidateToken(self, request, context):
        try:
            payload = jwt.decode(request.token, SECRET_KEY, algorithms=["HS256"])
            return auth_pb2.ValidateTokenResponse(
                is_valid=True,
                email=payload.get("email", ""),
                role=payload.get("role", "USER"),
                error=""
            )
        except jwt.ExpiredSignatureError:
            return auth_pb2.ValidateTokenResponse(is_valid=False, error="Token expired")
        except jwt.InvalidTokenError:
            return auth_pb2.ValidateTokenResponse(is_valid=False, error="Invalid token")

    def ChangePassword(self, request, context):
        try:
            payload = jwt.decode(request.token, SECRET_KEY, algorithms=["HS256"])
            email = payload.get("email")
        except jwt.InvalidTokenError:
            return auth_pb2.ChangePasswordResponse(success=False, error="Недействительный токен")

        with self.db_session_factory() as db:
            user = db.query(User).filter(User.email == email).first()
            if not user or not self._check_password(request.old_password, user.password_hash):
                return auth_pb2.ChangePasswordResponse(success=False, error="Неверный старый пароль")
            
            user.password_hash = self._hash_password(request.new_password)
            db.commit()
            return auth_pb2.ChangePasswordResponse(success=True, error="")

    def ResetPassword(self, request, context):
        with self.db_session_factory() as db:
            user = db.query(User).filter(User.email == request.email).first()
            if not user:
                return auth_pb2.ResetPasswordResponse(success=True, message="Если email существует, пароль отправлен")

            new_password = ''.join(random.choices(string.ascii_letters + string.digits, k=10))
            # Заглушка: выводим пароль в консоль сервера
            print(f"\n[ЗАГЛУШКА ПОЧТЫ] Сброс пароля для {user.email}. Новый пароль: {new_password}\n")
            
            user.password_hash = self._hash_password(new_password)
            db.commit()
            
            return auth_pb2.ResetPasswordResponse(success=True, message="Новый пароль отправлен на почту")