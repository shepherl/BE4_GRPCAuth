import datetime
import random
import string

import auth_pb2
import auth_pb2_grpc
import bcrypt
import grpc
import jwt
from models import User
from validators import (
    check_password_change,
    validate_and_normalize_email,
    validate_phone,
)

SECRET_KEY = "very_super_secret_jwt_key"


class AuthService(auth_pb2_grpc.AuthServiceServicer):
    def __init__(self, db_session_factory):
        self.db_session_factory = db_session_factory

    def _hash_password(self, password: str) -> str:
        salt = bcrypt.gensalt()
        return bcrypt.hashpw(password.encode("utf-8"), salt).decode("utf-8")

    def _check_password(self, password: str, hashed: str) -> bool:
        return bcrypt.checkpw(password.encode("utf-8"), hashed.encode("utf-8"))

    def _create_token(self, email: str, role: str = "USER") -> str:
        payload = {
            "email": email,
            "role": role,
            # Используем timezone-aware datetime для Python 3.12
            "exp": datetime.datetime.now(datetime.timezone.utc)
            + datetime.timedelta(hours=24),
        }
        return jwt.encode(payload, SECRET_KEY, algorithm="HS256")

    def Register(self, request, context):
        try:
            # Валидация и нормализация данных
            if not request.password or len(request.password) < 8:
                raise ValueError("Пароль должен содержать минимум 8 символов")
            
            clean_email = validate_and_normalize_email(request.email)
            clean_phone = validate_phone(request.phone)
        except ValueError as e:
            # Прерываем запрос со статусом INVALID_ARGUMENT (ошибка валидации)
            context.abort(grpc.StatusCode.INVALID_ARGUMENT, str(e))

        with self.db_session_factory() as db:
            if db.query(User).filter(User.email == clean_email).first():
                # Прерываем выполнение и отправляем gRPC-статус
                context.abort(
                    grpc.StatusCode.ALREADY_EXISTS,
                    "Пользователь с таким email уже существует",
                )

            new_user = User(
                email=clean_email,
                first_name=request.first_name,
                last_name=request.last_name,
                phone=clean_phone,
                password_hash=self._hash_password(request.password),
            )
            db.add(new_user)
            db.commit()

            # Если почта начинается на admin@, даем права администратора
            role = "ADMIN" if clean_email.startswith("admin@") else "USER"
            token = self._create_token(clean_email, role=role)
            return auth_pb2.AuthResponse(token=token, error="")

    def Login(self, request, context):
        try:
            # При логине тоже нормализуем почту (иначе человек не войдет, если введет почту с заглавной буквы)
            clean_email = validate_and_normalize_email(request.email)
        except ValueError as e:
            context.abort(grpc.StatusCode.INVALID_ARGUMENT, str(e))

        with self.db_session_factory() as db:
            user = db.query(User).filter(User.email == clean_email).first()
            if not user:
                context.abort(
                    grpc.StatusCode.NOT_FOUND, "Пользователь с таким email не найден"
                )

            if not self._check_password(request.password, user.password_hash):
                context.abort(grpc.StatusCode.UNAUTHENTICATED, "Неверный пароль")

            # Выдаем правильную роль при логине
            role = "ADMIN" if user.email.startswith("admin@") else "USER"
            token = self._create_token(user.email, role=role)
            return auth_pb2.AuthResponse(token=token, error="")

    def ValidateToken(self, request, context):
        try:
            payload = jwt.decode(request.token, SECRET_KEY, algorithms=["HS256"])
            return auth_pb2.ValidateTokenResponse(
                is_valid=True,
                email=payload.get("email", ""),
                role=payload.get("role", "USER"),
                error="",
            )
        except jwt.ExpiredSignatureError:
            context.abort(grpc.StatusCode.UNAUTHENTICATED, "Токен просрочен")
        except jwt.InvalidTokenError:
            context.abort(grpc.StatusCode.UNAUTHENTICATED, "Недействительный токен")

    def ChangePassword(self, request, context):
        # 1. Сначала проверяем, не совпадают ли пароли (до запроса в БД)
        try:
            check_password_change(request.old_password, request.new_password)
        except ValueError as e:
            context.abort(grpc.StatusCode.INVALID_ARGUMENT, str(e))

        # 2. Проверяем валидность токена
        try:
            payload = jwt.decode(request.token, SECRET_KEY, algorithms=["HS256"])
            email = payload.get("email")
        except jwt.ExpiredSignatureError:
            context.abort(grpc.StatusCode.UNAUTHENTICATED, "Токен просрочен")
        except jwt.InvalidTokenError:
            context.abort(grpc.StatusCode.UNAUTHENTICATED, "Недействительный токен")

        # 3. Меняем пароль в БД
        with self.db_session_factory() as db:
            user = db.query(User).filter(User.email == email).first()
            if not user:
                context.abort(grpc.StatusCode.NOT_FOUND, "Пользователь не найден")

            if not self._check_password(request.old_password, user.password_hash):
                context.abort(
                    grpc.StatusCode.INVALID_ARGUMENT, "Неверный старый пароль"
                )

            user.password_hash = self._hash_password(request.new_password)
            db.commit()
            return auth_pb2.ChangePasswordResponse(success=True, error="")

    def ResetPassword(self, request, context):
        try:
            clean_email = validate_and_normalize_email(request.email)
        except ValueError as e:
            context.abort(grpc.StatusCode.INVALID_ARGUMENT, str(e))

        with self.db_session_factory() as db:
            user = db.query(User).filter(User.email == clean_email).first()
            if not user:
                context.abort(
                    grpc.StatusCode.NOT_FOUND, "Пользователь с таким email не найден"
                )

            new_password = "".join(
                random.choices(string.ascii_letters + string.digits, k=10)
            )
            # Заглушка: выводим пароль в консоль сервера
            print(
                f"\n[ЗАГЛУШКА ПОЧТЫ] Сброс пароля для {user.email}. Новый пароль: {new_password}\n"
            )

            user.password_hash = self._hash_password(new_password)
            db.commit()

            return auth_pb2.ResetPasswordResponse(
                success=True, message="Новый пароль отправлен на почту"
            )
