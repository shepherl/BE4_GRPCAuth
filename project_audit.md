# 🔍 Аудит проекта BE4_GRPCAuth по заданию School 21

## Chapter III — Требования и статус выполнения

---

### 1. Dependency Injection через интерфейсы (IoC)
> *"Implement injection implementations using interfaces through the IOC container"*

| Сервис | Интерфейс | Реализация | Статус |
|---|---|---|---|
| ProductService | ✅ `ProductService.java` (interface) | ✅ `ProductServiceImpl.java` (@Service) | ✅ Сделано |
| ClientService | ✅ `ClientService.java` (interface) | ✅ `ClientServiceImpl.java` (@Service) | ✅ Сделано |
| ImageService | ✅ `ImageService.java` (interface) | ✅ `ImageServiceImpl.java` (@Service) | ✅ Сделано |
| SupplierService | ✅ `SupplierService.java` (interface) | ✅ `SupplierServiceImpl.java` (@Service) | ✅ Сделано |
| AuthClient | ✅ `AuthClient.java` (interface) | ✅ `AuthClientImpl.java` (@Service) | ✅ Сделано |

> [!TIP]
> Spring автоматически внедряет `*Impl` реализацию через конструктор контроллера. Контроллеры зависят только от интерфейсов.

---

### 2. Отдельный сервис авторизации
> *"Implement an authorization service (a separate application)"*

| Требование | Статус | Где реализовано |
|---|---|---|
| Отдельное приложение | ✅ | `AuthAPI/` — Python (grpcio) |
| Своя БД (новая база, НЕ таблица!) | ✅ | Контейнер `auth-db` с базой `authdb` (отдельный PostgreSQL) |
| Связь ТОЛЬКО через gRPC | ✅ | gRPC сервер на порту `50051` |
| `.proto` файл с описанием методов | ✅ | `AuthAPI/proto/auth.proto` |

---

### 3. Методы авторизационного сервиса

| Метод | Требование | Статус | Детали |
|---|---|---|---|
| **Register** | Создание пользователя (email, имя, фамилия, телефон, пароль) → токен | ✅ | `service.py` → `Register()` возвращает JWT |
| **Login** | Проверка email/пароль → токен | ✅ | `service.py` → `Login()` возвращает JWT |
| **ChangePassword** | Смена пароля | ✅ | `service.py` → `ChangePassword()` проверяет старый пароль |
| **ResetPassword** | Сброс пароля → вывод в консоль (заглушка email) | ✅ | `service.py` → `ResetPassword()` использует `print()` |
| **ValidateToken** | Проверка JWT токена (для middleware) | ✅ | `service.py` → `ValidateToken()` декодирует JWT, возвращает email и role |

---

### 4. Безопасность паролей
> *"Passwords should be stored in hashed form with salt. Cannot use md5 or sha1."*

| Требование | Статус | Детали |
|---|---|---|
| Хеширование с солью | ✅ | `bcrypt.gensalt()` + `bcrypt.hashpw()` |
| НЕ md5 / НЕ sha1 | ✅ | Используется **bcrypt** |
| В БД хранится хеш, не пароль | ✅ | Поле `password_hash` в модели `User` |

---

### 5. REST-эндпоинты авторизации в ShopAPI
> *"Add registration (/register), authorization (/auth), and password recovery (/reset)"*

| Эндпоинт | Требование | Статус | Путь в коде |
|---|---|---|---|
| Регистрация | `/register` | ✅ | `POST /api/v1/auth/register` |
| Авторизация | `/auth` | ⚠️ | `POST /api/v1/auth/login` (назван `/login` вместо `/auth`) |
| Сброс пароля | `/reset` | ✅ | `POST /api/v1/auth/reset` |
| Смена пароля | *(бонус)* | ✅ | `POST /api/v1/auth/change-password` |

> [!WARNING]
> В задании маршрут логина назван `/auth`, а у вас он `/login`. Формально это мелочь, но буквоед-проверяющий может придраться. Решение: переименовать `@PostMapping("/login")` в `@PostMapping("/auth")` в файле `AuthController.java`.

> *"These methods are not authenticated"* — ✅ На `AuthController` аннотации `@RequiresAuth` нет, эндпоинты публичные.

---

### 6. Авторизация всех API эндпоинтов
> *"Implement endpoint authorization of all APIs based on verification in the authentication service"*

| Контроллер | `@RequiresAuth` на классе | Удаление — только ADMIN | Статус |
|---|---|---|---|
| ProductController | ✅ | ✅ `@RequiresAuth(roles={"ADMIN"})` | ✅ |
| ClientController | ✅ | ✅ `@RequiresAuth(roles={"ADMIN"})` | ✅ |
| SupplierController | ✅ | ✅ `@RequiresAuth(roles={"ADMIN"})` | ✅ |
| ImageController | ✅ | ✅ `@RequiresAuth(roles={"ADMIN"})` | ✅ |
| AuthController | ❌ (публичный) | — | ✅ Так и должно быть |

---

### 7. Middleware (Interceptor)
> *"Authorization in controller methods should be based on a self-written authorization attribute whose logic resides in the application's middleware"*

| Требование | Статус | Где реализовано |
|---|---|---|
| Самописный атрибут (аннотация) | ✅ | `@RequiresAuth` в `security/RequiresAuth.java` |
| Middleware (интерцептор) | ✅ | `security/AuthInterceptor.java` implements `HandlerInterceptor` |
| Регистрация в конфигурации | ✅ | `config/WebMvcConfig.java` → `addPathPatterns("/api/**")` |
| Проверка ДО контроллера (preHandle) | ✅ | `preHandle()` метод |
| Ходит в gRPC для валидации | ✅ | `authClient.validateToken(token)` |
| Невалидный токен → 401 | ✅ | `SC_UNAUTHORIZED` |
| JWT в заголовке Authorization | ✅ | `request.getHeader("Authorization")` → `Bearer ...` |

---

### 8. gRPC поверх HTTP/2
> *"Don't forget that gRPC runs on top of HTTP/2"*

| Требование | Статус | Детали |
|---|---|---|
| gRPC использует HTTP/2 | ✅ | grpcio по умолчанию работает на HTTP/2 |
| `.proto` файл описан | ✅ | `auth.proto` — 5 RPC методов, 10 message types |
| Java-клиент генерируется из proto | ✅ | Плагин protobuf в `build.gradle` |

---

## 📊 Итоговая таблица

| Блок | Статус |
|---|---|
| DI через интерфейсы (IoC) | ✅ Выполнено |
| Отдельный сервис авторизации | ✅ Выполнено |
| Своя БД для авторизации | ✅ Выполнено |
| gRPC протокол | ✅ Выполнено |
| `.proto` файл | ✅ Выполнено |
| Register → токен | ✅ Выполнено |
| Login → токен | ✅ Выполнено |
| Смена пароля | ✅ Выполнено |
| Сброс пароля (заглушка в консоль) | ✅ Выполнено |
| Пароли хешируются bcrypt + salt | ✅ Выполнено |
| REST: /register, /auth, /reset | ⚠️ `/auth` назван `/login` |
| Авторизация всех API | ✅ Выполнено |
| Самописный атрибут (middleware) | ✅ Выполнено |
| JWT в Authorization header | ✅ Выполнено |
| Невалидный токен → 401 | ✅ Выполнено |

---

## ⚠️ Единственная рекомендация

В файле [`AuthController.java`](file:///mnt/c/Users/geekf/Desktop/Java/BE4_GRPCAuth/BE4_GRPCAuth/ShopAPI/src/main/java/com/school21/shopapi/controller/AuthController.java) переименовать маршрут логина:

```diff
-    @PostMapping("/login")
+    @PostMapping("/auth")
```

Это займет 2 секунды и полностью закроет формальное требование задания: *"Add ... authorization (/auth) ... method"*.
