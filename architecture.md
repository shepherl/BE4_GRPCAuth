 ```mermaid
  flowchart TD
      %% Определяем стили узлов
      classDef client fill:#f9f9f9,stroke:#333,stroke-width:2px;
      classDef nginx fill:#d4edda,stroke:#28a745,stroke-width:2px;
      classDef java fill:#cce5ff,stroke:#007bff,stroke-width:2px;
      classDef go fill:#e2d9f3,stroke:#6f42c1,stroke-width:2px;
      classDef db fill:#fff3cd,stroke:#ffc107,stroke-width:2px;
      classDef admin fill:#e2e3e5,stroke:#6c757d,stroke-width:1px;

      Client([💻 Клиент: Браузер / Postman]):::client
      Nginx{🛡️ Nginx 1.24  Балансировщик  HTTPS :443}:::nginx

      subgraph "ShopAPI (Java Spring Boot)"
          Master[Master Instance  :8080]:::java
          Rep1[Replica 1  :8080]:::java
          Rep2[Replica 2  :8080]:::java
      end

      subgraph "AuthAPI (Go)"
          Auth[Auth Service  gRPC :50051]:::go
      end

      subgraph "Базы Данных (PostgreSQL)"
          ShopDB[(Shop DB  port: 5432)]:::db
          AuthDB[(Auth DB  port: 5433)]:::db
      end

      Admin([🔧 pgAdmin  :5050]):::admin

      Client -- HTTPS --> Nginx
      Nginx -- "POST / PUT / DELETE" --> Master
      Nginx -- "GET (Round Robin)" --> Master
      Nginx -- "GET (Round Robin)" --> Rep1
      Nginx -- "GET (Round Robin)" --> Rep2

      Master -- "Read / Write" --> ShopDB
      Rep1 -. "Read Only" .-> ShopDB
      Rep2 -. "Read Only" .-> ShopDB
      Auth -- "Read / Write" --> AuthDB

      Admin -. "Управление" .-> ShopDB
      Admin -. "Управление" .-> AuthDB

      Master <== "gRPC (Login, Validate)" ==> Auth
      Rep1 <== "gRPC (Validate)" ==> Auth
      Rep2 <== "gRPC (Validate)" ==> Auth
  ```