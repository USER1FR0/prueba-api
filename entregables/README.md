# Entregables - Onboarding de Clientes

| Archivo | Contenido |
|---------|-----------|
| 01-diagrama-ER.md        | Diagrama entidad-relacion (Mermaid) |
| 02-script-bd.sql         | Script de creacion de tablas (= migracion Flyway V2) |
| 03 payloads/             | JSON de ejemplo validos |
| 03 payloads/invalidos/   | JSON para pruebas de validacion (deben dar 400) |
| 04-pruebas.http          | Coleccion de requests (VS Code / IntelliJ HTTP Client) |
| 05-pruebas-estres-k6.js  | Script de carga/estres con k6 |
| 06-documento-tecnico.md  | Documento tecnico de la solucion |

Evidencias: ejecutar la coleccion `04-pruebas.http` y/o k6, y guardar las
respuestas/capturas en una carpeta `evidencias/`.
