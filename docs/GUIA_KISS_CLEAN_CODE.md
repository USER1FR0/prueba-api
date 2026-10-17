# Buenas Prácticas: KISS y Clean Code

Guía de referencia **genérica** (aplicable a cualquier proyecto/lenguaje). Reúne principios, reglas concretas y ejemplos breves para escribir código simple, legible y mantenible.

---

## 1. KISS (Keep It Simple, Stupid)

> La solución más simple que resuelve el problema es casi siempre la mejor.

- **Resuelve el problema que tienes, no el que imaginas.** Evita abstracciones y configuraciones "por si acaso" (principio **YAGNI** — *You Aren't Gonna Need It*).
- **Menos piezas = menos fallas.** Cada capa, patrón o dependencia extra es algo que mantener y que puede romperse.
- **Prefiere lo obvio sobre lo ingenioso.** El código se lee muchas más veces de las que se escribe.
- **Una responsabilidad a la vez.** Divide un problema grande en pasos pequeños y claros.

**Ejemplo**
```text
// Sobre-ingeniería: una fábrica abstracta para crear un objeto que nunca cambia
FactoryProvider.getFactory("user").create()

// KISS:
new User(...)
```

---

## 2. Nombres claros

- Nombres que **revelen la intención**: `diasDesdeUltimoPago` en vez de `d`.
- Evita abreviaturas crípticas y nombres genéricos (`data`, `info`, `temp`, `manager`).
- El nombre debe decir **qué es o qué hace**, no cómo está implementado.
- Consistencia: un mismo concepto, un mismo nombre en todo el proyecto.

```text
// Mal
List<X> l = get(2);

// Bien
List<Cliente> clientesActivos = buscarClientesPorEstatus(ACTIVO);
```

---

## 3. Funciones pequeñas y con una sola responsabilidad

- Una función hace **una cosa** y la hace bien.
- Si necesitas "y" para describirla (*valida y guarda y notifica*), probablemente son varias funciones.
- Pocos parámetros (idealmente ≤ 3). Si son muchos, agrúpalos en un objeto.
- Un solo **nivel de abstracción** por función: no mezcles lógica de alto nivel con detalles de bajo nivel.

```text
// En vez de una función de 80 líneas:
registrar(request) {
  validarUnicidad(request);
  var cliente = guardar(request);
  crearCuenta(cliente);
  notificar(cliente);
}
```

---

## 4. No te repitas (DRY)

- Si copias y pegas, extrae: un método, una constante, una clase.
- Un cambio de regla debe hacerse en **un solo lugar**.
- Cuidado con la duplicación "oculta": la misma validación o el mismo literal repetidos.

```text
// Mal: el mismo mensaje/regex en dos lugares → se desincronizan
// Bien: una constante compartida
public static final String MSG_PASSWORD = "Mínimo 8, mayúscula, minúscula, número y especial";
```

> Matiz: no fuerces DRY entre cosas que *parecen* iguales pero cambian por razones distintas. Primero claridad, luego DRY.

---

## 5. Manejo de errores limpio

- Usa **excepciones**, no códigos de retorno dispersos ni `null` como "error".
- **Centraliza** el manejo (un manejador global) en vez de `try/catch` repetidos por todos lados.
- Lanza excepciones **específicas y con mensaje útil**; agrúpalas por familia para no repetir manejadores.
- **Nunca** expongas trazas técnicas, SQL o detalles internos al cliente.
- Falla rápido: valida en los bordes y aborta temprano con un mensaje claro.

```text
// Mal
if (cliente == null) return "error";

// Bien
if (cliente == null) throw new RecursoNoEncontradoException("No existe el cliente " + id);
```

---

## 6. Constantes en vez de números/strings mágicos

- Un literal con significado va en una constante con nombre.
- Configuración (timeouts, tamaños, URLs) va en archivos de configuración, no incrustada.

```text
// Mal
if (intentos > 20) ...

// Bien
private static final int MAX_INTENTOS = 20;
if (intentos > MAX_INTENTOS) ...
```

---

## 7. Comentarios: el porqué, no el qué

- El buen código se explica solo; el comentario explica **decisiones** y **contexto**, no lo obvio.
- Un comentario que repite el código es ruido. Mejor renombra la variable/función.
- Elimina código comentado (para eso está el control de versiones).

```text
// Mal:  i++  // incrementa i
// Bien:  // Reintentamos porque la API externa a veces responde 503 el primer intento
```

---

## 8. Separación de responsabilidades (capas)

- Cada capa tiene un trabajo: **entrada/salida**, **lógica de negocio**, **acceso a datos**.
- La lógica de negocio no vive en el controlador ni en la base de datos.
- No mezcles decisiones de negocio con detalles de framework o de persistencia.

```text
Controller  -> recibe/valida/responde (sin lógica de negocio)
Service     -> reglas de negocio, transacciones
Repository  -> solo acceso a datos
```

---

## 9. Objetos de transferencia (DTO) ≠ entidades

- **No expongas ni recibas entidades de base de datos** directamente: usa DTOs de entrada y de salida.
- Así controlas qué campos entran (evitas que el cliente toque campos del servidor) y qué campos salen (no filtras datos sensibles).
- Valida en el DTO de entrada, en el borde del sistema.

---

## 10. Inmutabilidad e inyección de dependencias

- Prefiere datos **inmutables** (campos `final`, objetos que no cambian tras construirse): menos errores por estado compartido.
- **Inyección por constructor** en vez de estado mutable o globales: hace las dependencias explícitas y el código testeable.

```text
// Bien
private final ClienteRepository repo;
public ClienteService(ClienteRepository repo) { this.repo = repo; }
```

---

## 11. Validación uniforme y respuestas consistentes

- Valida en un solo mecanismo declarativo (anotaciones/esquemas), no con `if` repetidos.
- Mantén una **estructura uniforme** de respuesta para éxito y error (mismo envelope).

```text
{ "codigo": 0, "mensaje": "OK", "datos": { ... } }
```

---

## 12. Pruebas

- Escribe pruebas para la **lógica de negocio** y los casos límite.
- Una prueba debe ser legible: *dado / cuando / entonces*.
- Sin pruebas, cualquier refactor es a ciegas.

---

## 13. Logging con criterio

- Registra lo útil para diagnosticar (eventos, errores con contexto), no todo.
- Nunca loguees secretos, contraseñas ni datos sensibles.
- Usa niveles (`debug`, `info`, `warn`, `error`) con sentido.

---

## 14. Consistencia

- Un solo estilo de formato, nombres e idioma en todo el proyecto.
- Sigue las convenciones del lenguaje/framework.
- La consistencia reduce la carga mental de quien lee.

---

## Code smells a evitar

- Funciones largas o con muchos parámetros.
- Clases que "hacen de todo" (God object).
- Duplicación de lógica o literales.
- `if/else` o `switch` anidados y profundos.
- Números/strings mágicos.
- Comentarios que explican código confuso en vez de simplificarlo.
- `try/catch` vacíos o que se tragan el error.
- Devolver `null` en lugar de lanzar o de usar un tipo vacío/opcional.
- Exponer entidades o detalles internos al exterior.

---

## Checklist rápido antes de hacer commit

- [ ] ¿La solución es la más simple que funciona? (KISS/YAGNI)
- [ ] ¿Los nombres explican la intención?
- [ ] ¿Cada función hace una sola cosa?
- [ ] ¿Eliminé duplicación real?
- [ ] ¿Sin números/strings mágicos?
- [ ] ¿Errores con excepciones claras y manejo centralizado?
- [ ] ¿Capas separadas, sin lógica en el lugar equivocado?
- [ ] ¿DTOs en los bordes, sin exponer entidades?
- [ ] ¿Respuestas con formato uniforme?
- [ ] ¿Sin datos sensibles en logs ni en las respuestas?
- [ ] ¿Hay pruebas para lo importante?
