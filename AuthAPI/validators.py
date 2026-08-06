import re


def validate_and_normalize_email(email: str) -> str:
    """
    Очищает пробелы, переводит в нижний регистр и проверяет на латиницу.
    Если валидация не пройдена, выбрасывает ValueError.
    """
    normalized_email = email.strip().lower()

    # Регулярное выражение для проверки почты (только латиница и базовые символы)
    pattern = re.compile(r"^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\.[a-zA-Z0-9-.]+$")

    if not pattern.match(normalized_email):
        raise ValueError(
            "Некорректный формат почты (разрешена только латиница и стандартные символы)"
        )

    return normalized_email


def validate_phone(phone: str) -> str:
    """
    Проверяет, что номер телефона состоит только из цифр (допускается '+' в начале).
    """
    clean_phone = phone.strip()

    pattern = re.compile(r"^\+?\d{10,15}$")

    if not pattern.match(clean_phone):
        raise ValueError(
            "Некорректный номер телефона (должен содержать только цифры, допускается '+' в начале)"
        )

    return clean_phone


def check_password_change(old_password: str, new_password: str):
    """
    Проверяет, что новый пароль не совпадает со старым.
    """
    if old_password == new_password:
        raise ValueError("Новый пароль не должен совпадать с текущим")
