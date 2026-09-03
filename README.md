# llanos-post1-u1

## Análisis de Violaciones SOLID

| Principio | Método/Sección afectada | Descripción de la violación |
|-----------|-------------------------|-----------------------------|
| **SRP** (Single Responsibility Principle) | `calculateTotal` + `applyDiscount` + `saveOrder` + `sendEmail` + `printReport` | La clase actúa como un *God Object*. Acumula cinco responsabilidades distintas que deberían cambiar por razones independientes: cálculo de impuestos, lógica de promociones, persistencia en memoria, infraestructura de mensajería y formateo de reportes visuales en consola. |
| **OCP** (Open/Closed Principle) | `applyDiscount` (if/else sobre customerType) | El método está cerrado a la extensión y abierto a la modificación. Si en el futuro se añade un nuevo tipo de cliente (por ejemplo, "PREMIUM" o "CORPORATE"), es obligatorio modificar directamente el código fuente agregando más bloques condicionales `if`, incrementando la fragilidad del sistema. |
| **DIP** (Dependency Inversion Principle) | Toda la clase (dependencias internas sin abstracciones) | La clase maneja todos sus procesos de forma acoplada y concreta. No expone interfaces ni abstracciones para desacoplar el almacenamiento, el envío de correos o las estrategias de descuento, lo que impide sustituir estas implementaciones o realizar pruebas unitarias aisladas (mocking). |


# Post-contenido — Unidad 1: Fundamentos de Patrones de Diseño y Buenas Prácticas

## Descripción
Repositorio del post-contenido de la Unidad 1 de Patrones de Diseño
de Software — Sexto Semestre. Contiene dos partes: refactorización
SOLID de un God Object (parte-1-refactorizacion-solid/) y análisis
de patrones GoF en Spring Framework (parte-2-analisis-gof-spring/).

## Parte 1 — Refactorización SOLID
Proyecto Maven que refactoriza OrderProcessor aplicando SRP, OCP y
DIP. Ver parte-1-refactorizacion-solid/.

## Parte 2 — Análisis de Patrones GoF en Spring

| # | Patrón | Categoría | Clase en Spring |
|---|--------|-----------|-----------------|
| 1 | Singleton | Creacional | DefaultSingletonBeanRegistry.java |
| 2 | Decorator | Estructural | TaskDecorator.java |
| 3 | Observer | Comportamiento | ApplicationListener.java |


Ver parte-2-analisis-gof-spring/documento-analisis.md.

## Herramientas utilizadas
- Java 21, Apache Maven, VS Code, Git, GitHub
- Código fuente de Spring Framework (investigación)

## Conclusiones

El análisis de los patrones GoF permitió comprender cómo soluciones de diseño reconocidas pueden integrarse de manera sistemática en un framework como Spring. Se identificó que Singleton, Decorator y Observer permiten resolver problemas relacionados con la creación de objetos, la extensión de comportamiento y la comunicación entre componentes, respectivamente. El estudio del código fuente permitió relacionar los conceptos teóricos de los patrones con implementaciones concretas y comprender su relación con principios SOLID. Como aprendizaje principal, los patrones deben utilizarse para resolver problemas reales de diseño y no simplemente como estructuras que se aplican de forma automática.
