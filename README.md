# llanos-post1-u1
"Post-contenido — Refactorización SOLID y análisis de patrones GoF en Spring
## Análisis de Violaciones SOLID

| Principio | Método/Sección afectada | Descripción de la violación |
|-----------|-------------------------|-----------------------------|
| **SRP** (Single Responsibility Principle) | `calculateTotal` + `applyDiscount` + `saveOrder` + `sendEmail` + `printReport` | La clase actúa como un *God Object*. Acumula cinco responsabilidades distintas que deberían cambiar por razones independientes: cálculo de impuestos, lógica de promociones, persistencia en memoria, infraestructura de mensajería y formateo de reportes visuales en consola. |
| **OCP** (Open/Closed Principle) | `applyDiscount` (if/else sobre customerType) | El método está cerrado a la extensión y abierto a la modificación. Si en el futuro se añade un nuevo tipo de cliente (por ejemplo, "PREMIUM" o "CORPORATE"), es obligatorio modificar directamente el código fuente agregando más bloques condicionales `if`, incrementando la fragilidad del sistema. |
| **DIP** (Dependency Inversion Principle) | Toda la clase (dependencias internas sin abstracciones) | La clase maneja todos sus procesos de forma acoplada y concreta. No expone interfaces ni abstracciones para desacoplar el almacenamiento, el envío de correos o las estrategias de descuento, lo que impide sustituir estas implementaciones o realizar pruebas unitarias aisladas (mocking). |
