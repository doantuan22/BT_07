
IF DB_ID(N'bt_07') IS NULL
    CREATE DATABASE bt_07;
GO

USE bt_07;
GO


IF OBJECT_ID(N'dbo.categories', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.categories (
        category_id   BIGINT IDENTITY(1,1) NOT NULL,
        category_name NVARCHAR(255) NULL,
        icon          NVARCHAR(255) NULL,
        CONSTRAINT PK_categories PRIMARY KEY (category_id)
    );
END
GO


IF OBJECT_ID(N'dbo.products', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.products (
        product_id   BIGINT IDENTITY(1,1) NOT NULL,
        product_name NVARCHAR(500) NOT NULL,
        quantity     INT           NOT NULL,
        unit_price   FLOAT         NOT NULL,
        images       VARCHAR(200)  NULL,
        description  NVARCHAR(500) NOT NULL,
        discount     FLOAT         NOT NULL,
        create_date  DATETIME2(6)  NULL,
        status       SMALLINT      NOT NULL,
        category_id  BIGINT        NULL,
        CONSTRAINT PK_products PRIMARY KEY (product_id),
        CONSTRAINT FK_products_categories FOREIGN KEY (category_id)
            REFERENCES dbo.categories (category_id)
    );
END
GO


IF OBJECT_ID(N'dbo.SPRING_SESSION', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.SPRING_SESSION (
        PRIMARY_ID            CHAR(36)     NOT NULL,
        SESSION_ID            CHAR(36)     NOT NULL,
        CREATION_TIME         BIGINT       NOT NULL,
        LAST_ACCESS_TIME      BIGINT       NOT NULL,
        MAX_INACTIVE_INTERVAL INT          NOT NULL,
        EXPIRY_TIME           BIGINT       NOT NULL,
        PRINCIPAL_NAME        VARCHAR(100) NULL,
        CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
    );
    CREATE UNIQUE INDEX SPRING_SESSION_IX1 ON dbo.SPRING_SESSION (SESSION_ID);
    CREATE INDEX SPRING_SESSION_IX2 ON dbo.SPRING_SESSION (EXPIRY_TIME);
    CREATE INDEX SPRING_SESSION_IX3 ON dbo.SPRING_SESSION (PRINCIPAL_NAME);
END
GO

IF OBJECT_ID(N'dbo.SPRING_SESSION_ATTRIBUTES', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.SPRING_SESSION_ATTRIBUTES (
        SESSION_PRIMARY_ID CHAR(36)      NOT NULL,
        ATTRIBUTE_NAME     VARCHAR(200)  NOT NULL,
        ATTRIBUTE_BYTES    VARBINARY(MAX) NOT NULL,
        CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
        CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID)
            REFERENCES dbo.SPRING_SESSION (PRIMARY_ID) ON DELETE CASCADE
    );
END
GO


IF NOT EXISTS (SELECT 1 FROM dbo.categories)
BEGIN
    INSERT INTO dbo.categories (category_name, icon) VALUES
        (N'Điện thoại', NULL),
        (N'Laptop',     NULL),
        (N'Phụ kiện',   NULL),
        (N'Thời trang', NULL);
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.products)
BEGIN
    INSERT INTO dbo.products
        (product_name, quantity, unit_price, images, description, discount, create_date, status, category_id)
    SELECT N'iPhone 15',            50, 22990000, NULL, N'Điện thoại Apple iPhone 15 128GB', 5,  SYSDATETIME(), 1, category_id FROM dbo.categories WHERE category_name = N'Điện thoại'
    UNION ALL
    SELECT N'Samsung Galaxy S24',   40, 19990000, NULL, N'Điện thoại Samsung Galaxy S24 256GB', 8, SYSDATETIME(), 1, category_id FROM dbo.categories WHERE category_name = N'Điện thoại'
    UNION ALL
    SELECT N'MacBook Air M2',       25, 27990000, NULL, N'Laptop Apple MacBook Air M2 13 inch', 3, SYSDATETIME(), 1, category_id FROM dbo.categories WHERE category_name = N'Laptop'
    UNION ALL
    SELECT N'Dell XPS 13',          15, 31990000, NULL, N'Laptop Dell XPS 13 Core i7',           0, SYSDATETIME(), 1, category_id FROM dbo.categories WHERE category_name = N'Laptop'
    UNION ALL
    SELECT N'Tai nghe Bluetooth',  120,   890000, NULL, N'Tai nghe Bluetooth chống ồn',         10, SYSDATETIME(), 1, category_id FROM dbo.categories WHERE category_name = N'Phụ kiện'
    UNION ALL
    SELECT N'Áo thun nam',         200,   250000, NULL, N'Áo thun cotton thoáng mát',            0, SYSDATETIME(), 1, category_id FROM dbo.categories WHERE category_name = N'Thời trang';
END
GO
