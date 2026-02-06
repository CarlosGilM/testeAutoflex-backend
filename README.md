# Inventory Management API (Autoflex)

API REST desenvolvida com **Quarkus** para o gerenciamento de inventário industrial. O sistema permite o controle completo de matérias-primas e a definição da composição técnica de produtos finais, com cálculos automatizados e validações de estoque.

---

## 🚀 Funcionalidades

* **Gerenciamento de Matérias-Primas**

  * CRUD completo
  * Controle de saldo de estoque
  * Validação de quantidades não negativas

* **Composição de Produtos**

  * Definição técnica dos insumos e quantidades que compõem cada produto final

* **Cálculo de Preços**

  * Estrutura preparada para precificação baseada em componentes

* **Automação de Banco de Dados**

  * Inicialização automática do schema
  * Carga de dados iniciais para facilitar desenvolvimento e testes

---

## 🛠 Tecnologias e Frameworks

* **Linguagem:** Java 21
* **Framework:** [Quarkus 3.31.2](https://quarkus.io/)
* **Persistência:** Hibernate ORM com Panache
* **Banco de Dados:**

  * PostgreSQL 15 (Produção/Desenvolvimento)
  * H2 (Testes)
* **Containerização:** Docker & Docker Compose
* **Testes e Qualidade:** JUnit 5, Mockito, RestAssured

---

## 📦 Como Rodar o Projeto

### 1. Utilizando Docker Compose (Recomendado)

Este método sobe toda a infraestrutura (API + Banco de Dados) de forma isolada e automática.

1. Certifique-se de que o Docker e o Docker Compose estão instalados e em execução.
2. Na raiz do projeto, gere o pacote da aplicação:

```bash
./mvnw package
```

3. Suba os containers e force o build da imagem da API:

```bash
docker-compose up --build
```

A aplicação estará disponível em:

```
http://localhost:8080
```

---

### 2. Execução Manual (Modo Dev)

Neste modo, a API é executada localmente com **Live Reload**, porém o banco de dados **PostgreSQL deve estar rodando separadamente** (via Docker ou instalação local).

#### 2.2 Configurações de Conexão

As credenciais padrão esperadas pela aplicação são:

* **Host:** localhost
* **Porta:** 5432
* **Database:** inventory_db
* **Usuário:** postgres
* **Senha:** autoflexTESTE

Essas configurações podem ser verificadas ou ajustadas no arquivo:

```
src/main/resources/application.properties
```

Exemplo:

```properties
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=postgres
quarkus.datasource.password=autoflexTESTE
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/inventory_db
```

#### 2.3 Executando a aplicação em modo desenvolvimento

Com o banco em execução, inicie a API com:

```bash
./mvnw quarkus:dev
```
---

## 🧪 Testes de Qualidade

O projeto possui uma suíte de testes que valida desde regras de negócio unitárias até o fluxo completo de integração.

Para rodar todos os testes:

```bash
./mvnw test
```

> **Nota**
>
> Os testes de integração utilizam o profile `%test` com banco H2 em memória, garantindo que os dados de teste não afetem os ambientes de desenvolvimento ou produção.

---

## 📑 Documentação da API (Swagger)

Com a aplicação em execução, a documentação interativa da API pode ser acessada em:

👉 `http://localhost:8080/q/swagger-ui`

### Principais Endpoints

| Método | Endpoint              | Descrição                                    |
| ------ | --------------------- | -------------------------------------------- |
| GET    | `/products`           | Lista todos os produtos e suas composições   |
| POST   | `/products`           | Cadastra um novo produto final e sua receita |
| GET    | `/raw-materials`      | Consulta o estoque de matérias-primas        |
| PUT    | `/raw-materials/{id}` | Atualiza dados ou saldo de um insumo         |
| DELETE | `/products/{id}`      | Remove um produto e suas associações         |

---

## 🧠 Observações Finais

* Projeto desenvolvido como **desafio técnico para a Autoflex**

