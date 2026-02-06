-- Tabela de Matérias-primas
CREATE TABLE IF NOT EXISTS raw_materials (
    code BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    stock_quantity DOUBLE PRECISION NOT NULL CHECK (stock_quantity >= 0)
);

-- Tabela de Produtos
CREATE TABLE IF NOT EXISTS products (
    code BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(19, 2) NOT NULL CHECK (price > 0)
);

-- Tabela de Composição (Relacionamento)
CREATE TABLE IF NOT EXISTS product_compositions (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    raw_material_id BIGINT NOT NULL,
    quantity_needed DOUBLE PRECISION NOT NULL CHECK (quantity_needed > 0),
    CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES products (code),
    CONSTRAINT fk_raw_material FOREIGN KEY (raw_material_id) REFERENCES raw_materials (code)
);

-- ==================================================================================
-- INSERÇÃO DE DADOS (CENÁRIO DE TESTE)
-- ==================================================================================

INSERT INTO
    raw_materials (name, stock_quantity)
VALUES ('Aço Tubular (kg)', 100.0);

INSERT INTO
    raw_materials (name, stock_quantity)
VALUES ('Borracha Pneu (un)', 40.0);

INSERT INTO
    raw_materials (name, stock_quantity)
VALUES ('Plástico ABS (kg)', 50.0);

INSERT INTO
    raw_materials (name, stock_quantity)
VALUES ('Parafuso (un)', 500.0);

INSERT INTO
    products (name, price)
VALUES ('Bicicleta Premium', 1500.00);

INSERT INTO
    products (name, price)
VALUES ('Patinete Iniciante', 200.00);

INSERT INTO
    product_compositions (
        product_id,
        raw_material_id,
        quantity_needed
    )
VALUES (1, 1, 8.0);

INSERT INTO
    product_compositions (
        product_id,
        raw_material_id,
        quantity_needed
    )
VALUES (1, 2, 2.0);

INSERT INTO
    product_compositions (
        product_id,
        raw_material_id,
        quantity_needed
    )
VALUES (1, 4, 10.0);

INSERT INTO
    product_compositions (
        product_id,
        raw_material_id,
        quantity_needed
    )
VALUES (2, 1, 2.0);

INSERT INTO
    product_compositions (
        product_id,
        raw_material_id,
        quantity_needed
    )
VALUES (2, 3, 4.0);

INSERT INTO
    product_compositions (
        product_id,
        raw_material_id,
        quantity_needed
    )
VALUES (2, 4, 4.0);