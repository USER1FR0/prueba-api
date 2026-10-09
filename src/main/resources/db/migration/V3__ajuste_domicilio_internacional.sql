-- =====================================================================
-- V3: Soporte de domicilios internacionales
--  - pais pasa a codigo ISO 3166 de 2 letras
--  - codigo_postal admite formatos internacionales (hasta 10 caracteres)
--  - estado deja de ser obligatorio (solo aplica a Mexico)
--  - el CP de 5 digitos solo se exige cuando el pais es MX
-- =====================================================================

-- Normaliza datos previos (si existieran) antes de reducir el tamano
UPDATE domicilios
   SET pais = 'MX'
 WHERE UPPER(TRIM(pais)) IN ('MEXICO', 'MÉXICO', 'MX', 'MEX');

-- codigo_postal: CHAR(5) -> VARCHAR(10) (recorta el padding del CHAR)
ALTER TABLE domicilios
    ALTER COLUMN codigo_postal TYPE VARCHAR(10) USING TRIM(codigo_postal);

-- pais: VARCHAR(100) -> VARCHAR(2) en mayusculas
ALTER TABLE domicilios
    ALTER COLUMN pais TYPE VARCHAR(2) USING UPPER(LEFT(TRIM(pais), 2));

-- estado opcional (paises sin division estatal en catalogo)
ALTER TABLE domicilios
    ALTER COLUMN estado DROP NOT NULL;

-- Reemplaza el CHECK de CP: 5 digitos solo si el pais es MX
ALTER TABLE domicilios DROP CONSTRAINT IF EXISTS ck_domicilios_cp;
ALTER TABLE domicilios
    ADD CONSTRAINT ck_domicilios_cp
    CHECK (UPPER(pais) <> 'MX' OR codigo_postal ~ '^[0-9]{5}$');

-- pais debe ser 2 letras mayusculas (el catalogo ISO real se valida en la app)
ALTER TABLE domicilios
    ADD CONSTRAINT ck_domicilios_pais
    CHECK (pais ~ '^[A-Z]{2}$');
