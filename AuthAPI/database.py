import os

from sqlalchemy import create_engine
from sqlalchemy.orm import declarative_base, sessionmaker

# Адрес базы данных по умолчанию (для локального запуска) !!!!!!!!!
DB_URL = os.getenv(
    "DATABASE_URL", "postgresql://postgres:postgres@localhost:5433/authdb"
)

engine = create_engine(DB_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()
