 flowchart TD
        %% Определяем стили узлов
        classDef client fill:#f9f9f9,stroke:#333,stroke-width:2px;
        classDef nginx fill:#d4edda,stroke:#28a745,stroke-width:2px;
        classDef java fill:#cce5ff,stroke:#007bff,stroke-width:2px;
        classDef go fill:#e2d9f3,stroke:#6f42c1,stroke-width:2px;
        classDef db fill:#fff3cd,stroke:#ffc107,stroke-width:2px;
        classDef admin fill:#e2e3e5,stroke:#6c757d,stroke-width:1px;

        %% Клиент
        Client([💻 Клиент: Браузер / Postman]):::client

        %% Nginx
        Nginx{🛡️ Nginx 1.24 <br/> Балансировщик <br/> HTTPS :443}:::nginx

        %% Группа Java Сервисов
        subgraph "ShopAPI (Java Spring Boot)"
            Master[Master Instance <br/> :8080]:::java
            Rep1[Replica 1 <br/> :8080]:::java
            Rep2[Replica 2 <br/> :8080]:::java
        end

        %% Группа Go Сервисов
        subgraph "AuthAPI (Python)"
            Auth[Auth Service <br/> gRPC :50051]:::go
        end

        %% Базы данных
        subgraph "Базы Данных (PostgreSQL)"
            ShopDB[(Shop DB <br/> port: 5432)]:::db
            AuthDB[(Auth DB <br/> port: 5433)]:::db
        end

        %% Инструменты
        Admin([🔧 pgAdmin <br/> :5050]):::admin

        %% Связи и стрелки
        Client -- HTTPS --> Nginx

        %% Маршрутизация Nginx
        Nginx -- "POST / PUT / DELETE" --> Master
        Nginx -- "GET (Round Robin)" --> Master
        Nginx -- "GET (Round Robin)" --> Rep1
        Nginx -- "GET (Round Robin)" --> Rep2

        %% Связи баз данных
        Master -- "Read / Write" --> ShopDB
        Rep1 -. "Read Only" .-> ShopDB
        Rep2 -. "Read Only" .-> ShopDB
        Auth -- "Read / Write" --> AuthDB

        Admin -. "Управление" .-> ShopDB
        Admin -. "Управление" .-> AuthDB

        %% gRPC связи
        Master <== "gRPC (Login, Validate)" ==> Auth
        Rep1 <== "gRPC (Validate)" ==> Auth
        Rep2 <== "gRPC (Validate)" ==> Auth
