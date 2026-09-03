Análisis de Patrones de Diseño GoF en el Código Fuente de Spring Framework

Nombre: Juan David Llanos Castañeda
Código: 1152462
Curso: Patrones de diseño-1156701-A
Unidad: PostContenido/Unidad 1
Fecha: 2 de septiembre de 2026

Introducción

Los patrones de diseño del Gang of Four (GoF) constituyen las soluciones fundacionales a los problemas recurrentes en el desarrollo orientado a objetos. A más de tres décadas de su publicación, estas plantillas arquitectónicas no han perdido vigencia; por el contrario, representan el núcleo estructural de los ecosistemas de software más robustos de la industria contemporánea. Spring Framework, al consolidarse como un proyecto de código abierto maduro, exhaustivamente documentado y con más de veinte años de evolución orgánica, se presenta como el caso de estudio idóneo para analizar la aplicación práctica de estos patrones a escala empresarial, trascendiendo el ámbito meramente académico.El propósito de este documento es identificar, desglosar y contrastar tres patrones GoF pertenecientes a categorías distintas (Creacional, Estructural y de Comportamiento) directamente en el código fuente del repositorio oficial spring-projects/spring-framework en GitHub. 

Para cada patrón seleccionado, se detallará la clase o interfaz concreta que lo implementa, el problema de ingeniería que resuelve en el contexto de una aplicación moderna y su correlación directa con los principios SOLID de diseño de software.A efectos de este análisis, se adopta Spring Boot como el prisma metodológico. Dado que representa la interfaz primaria a través de la cual los desarrolladores Java interactúan con el ecosistema de Spring, cada uno de sus componentes modulares (starters como spring-boot-starter-web o spring-boot-starter-data-jpa) no son más que abstracciones construidas sobre los engranajes internos del framework base. Por lo tanto, descifrar estos mecanismos subyacentes permite comprender con precisión científica el comportamiento "bajo el capó" de las aplicaciones empresariales actuales y los criterios de extensibilidad, desacoplamiento y gestión del ciclo de vida que las sostienen.




## Singleton — Patrón Creacional

El **Singleton** pertenece a la categoría de patrones **creacionales** del catálogo GoF. Su propósito es garantizar que una clase tenga una única instancia dentro de un ámbito determinado y proporcionar un mecanismo controlado para acceder a ella. En **Spring Framework**, este patrón se aplica mediante el alcance (`scope`) predeterminado de los beans: un bean definido como `singleton` tiene una única instancia administrada por el contenedor para cada nombre de bean y dentro del `ApplicationContext` correspondiente. A diferencia del Singleton clásico, Spring no obliga a que la clase implemente métodos estáticos ni tenga un constructor privado; la responsabilidad de controlar la instancia pertenece al contenedor IoC.

### ¿Dónde aparece en Spring Framework?

La implementación principal se encuentra en la clase **`org.springframework.beans.factory.support.DefaultSingletonBeanRegistry`**, perteneciente al módulo **`spring-beans`** de Spring Framework. Esta clase implementa la infraestructura necesaria para registrar, almacenar y recuperar las instancias singleton administradas por el contenedor. Está relacionada con la interfaz **`org.springframework.beans.factory.config.SingletonBeanRegistry`**, que define operaciones como registrar y obtener instancias singleton.

Una de las estructuras fundamentales de `DefaultSingletonBeanRegistry` es `singletonObjects`, utilizada como caché para almacenar las instancias creadas:

```java
private final Map<String, Object> singletonObjects =
        new ConcurrentHashMap<>(256);
```

De esta manera, Spring asocia el nombre de cada bean con la instancia que debe reutilizarse. La clase forma parte de la infraestructura utilizada por las implementaciones de `BeanFactory`, por lo que el comportamiento Singleton está integrado directamente en el mecanismo de creación y administración de beans del framework.

### ¿Qué problema resuelve en este contexto?

Spring necesita evitar que el contenedor cree múltiples objetos cuando diferentes componentes solicitan el mismo bean. Crear una nueva instancia mediante `new` cada vez que se solicita un objeto provocaría un mayor consumo de recursos y dificultaría la administración de su ciclo de vida. Además, determinados componentes deben compartir el mismo estado o recurso durante la ejecución de una aplicación.

El Singleton permite resolver este problema manteniendo una instancia dentro del contenedor. Cuando un bean singleton ya ha sido creado, una nueva solicitud obtiene la instancia existente en lugar de crear otra. Esto es más flexible que implementar manualmente el Singleton GoF dentro de cada clase, porque la clase no necesita conocer cómo se controla su propia instancia. Spring puede administrar la creación, reutilización, dependencias y destrucción del objeto desde el contenedor.

Por tanto, en Spring el Singleton debe entenderse como un **alcance administrado por el contenedor**, no simplemente como una instancia global de una clase. La instancia pertenece al contexto del contenedor y puede ser obtenida mediante mecanismos como `ApplicationContext#getBean()`.

### ¿Qué evidencia de código lo confirma?

El siguiente fragmento de `DefaultSingletonBeanRegistry` muestra el registro de una instancia singleton:

```java
@Override
public void registerSingleton(String beanName, Object singletonObject) {
    Assert.notNull(beanName, "Bean name must not be null");
    Assert.notNull(singletonObject, "Singleton object must not be null");

    this.singletonLock.lock();
    try {
        addSingleton(beanName, singletonObject);
    }
    finally {
        this.singletonLock.unlock();
    }
}
```

El método recibe el nombre del bean y la instancia que debe registrarse. Posteriormente, `addSingleton` incorpora la instancia al registro utilizado por Spring. La recuperación se realiza mediante `getSingleton`, que consulta el caché:

```java
@Override
public @Nullable Object getSingleton(String beanName) {
    return getSingleton(beanName, true);
}

protected @Nullable Object getSingleton(
        String beanName, boolean allowEarlyReference) {

    Object singletonObject = this.singletonObjects.get(beanName);

    if (singletonObject != null) {
        return singletonObject;
    }
    // ...
}
```

La instrucción `this.singletonObjects.get(beanName)` constituye una evidencia directa del comportamiento Singleton: Spring busca la instancia previamente almacenada utilizando el nombre del bean. Si la instancia existe, puede reutilizarla en lugar de crear un nuevo objeto.

### ¿Qué principio SOLID refuerza?

La aplicación del Singleton en Spring se relaciona principalmente con el **Single Responsibility Principle (SRP)**. En una implementación tradicional del patrón, la propia clase debe encargarse de controlar su instancia única mediante un constructor privado, una variable estática y un método de acceso. Esto mezcla la responsabilidad funcional de la clase con la administración de su ciclo de vida.

Spring separa ambas responsabilidades. La clase se concentra en proporcionar su comportamiento, mientras que `DefaultSingletonBeanRegistry` y el resto del contenedor se encargan de controlar la creación, almacenamiento y reutilización de sus instancias. De esta manera, el objeto administrado no necesita implementar ninguna lógica específica para ser singleton.

También existe una relación con el **Dependency Inversion Principle (DIP)**, ya que los componentes pueden recibir sus dependencias desde el contenedor en lugar de obtenerlas mediante llamadas estáticas como `MiServicio.getInstance()`. Esto disminuye el acoplamiento entre las clases y el mecanismo utilizado para administrar sus instancias.

En conclusión, **Spring implementa el concepto de Singleton como parte de su contenedor IoC**, utilizando `DefaultSingletonBeanRegistry` para almacenar y recuperar las instancias compartidas. Esta estrategia conserva el objetivo principal del patrón GoF, pero evita acoplar la lógica Singleton a las clases de aplicación y permite que Spring controle de manera centralizada su ciclo de vida.


## Decorator — Patrón Estructural

El **Decorator** es un patrón de diseño estructural cuyo propósito es agregar responsabilidades o comportamiento a un objeto de forma dinámica mediante otro objeto que lo envuelve. El objeto decorador mantiene una interfaz compatible con el objeto original, permitiendo añadir funcionalidades sin modificar directamente su implementación. De esta manera, el comportamiento puede extenderse mediante composición en lugar de crear múltiples subclases.

En **Spring Framework**, una implementación relacionada directamente con este patrón es la interfaz **`org.springframework.core.task.TaskDecorator`**, perteneciente al módulo **`spring-core`**. Esta interfaz define un mecanismo para decorar objetos `Runnable`, permitiendo que una tarea sea envuelta antes de ser ejecutada. Spring utiliza este concepto en sus componentes relacionados con la ejecución de tareas, como `ThreadPoolTaskExecutor`, para permitir la incorporación de comportamiento adicional a las tareas ejecutadas.

El problema que resuelve en Spring es la necesidad de agregar funcionalidades alrededor de la ejecución de una tarea sin modificar cada implementación de `Runnable`. Por ejemplo, una aplicación puede necesitar conservar información de contexto, realizar acciones de seguimiento o ejecutar lógica adicional antes y después de una tarea. Una alternativa directa sería modificar cada tarea individualmente o crear diferentes clases heredadas para cada comportamiento, lo que aumentaría el acoplamiento y la cantidad de código. `TaskDecorator` permite separar esta funcionalidad de la tarea original y aplicarla mediante un objeto que la envuelve.

Como evidencia del código fuente de Spring Framework se encuentra la interfaz:

```java id="d4x7kp"
@FunctionalInterface
public interface TaskDecorator {

    Runnable decorate(Runnable runnable);

}
```

El método `decorate` recibe un objeto `Runnable` y devuelve otro `Runnable`. Esto permite que la implementación cree un objeto que envuelva la tarea original y agregue comportamiento antes o después de su ejecución. Conceptualmente, una implementación podría realizar lo siguiente:

```java id="p8w3nm"
public Runnable decorate(Runnable runnable) {
    return () -> {
        // Comportamiento adicional antes de ejecutar.
        runnable.run();
        // Comportamiento adicional después de ejecutar.
    };
}
```

En este caso, el `Runnable` original permanece sin modificaciones. El decorador incorpora responsabilidades adicionales alrededor de su ejecución, que es precisamente la característica principal del patrón Decorator. Además, diferentes implementaciones de `TaskDecorator` pueden proporcionar comportamientos diferentes y ser utilizadas sin cambiar el código de la tarea que será ejecutada.

El uso de `TaskDecorator` refuerza principalmente el principio **Open/Closed Principle (OCP)** de SOLID, debido a que permite extender el comportamiento de una tarea sin modificar su código original. También se relaciona con el **Single Responsibility Principle (SRP)**, porque la responsabilidad de ejecutar la tarea permanece separada de las responsabilidades adicionales que se quieran incorporar. De esta forma, Spring favorece una estructura flexible en la que las funcionalidades pueden agregarse mediante composición.

Por lo tanto, `TaskDecorator` representa una aplicación del concepto **Decorator** dentro de Spring Framework, utilizando la composición para extender el comportamiento de objetos `Runnable`. Su diseño permite agregar responsabilidades de manera flexible y mantener separadas las tareas originales de las funcionalidades adicionales que puedan requerirse durante su ejecución.



## Observer — Patrón de Comportamiento

El **Observer** pertenece a la categoría de patrones **de comportamiento** del catálogo GoF. Su propósito es establecer una relación de dependencia entre un objeto que produce cambios de estado y otros objetos interesados en recibir notificaciones cuando dichos cambios ocurren. El objeto observado no necesita conocer los detalles de cómo reaccionan los observadores; simplemente comunica que ocurrió un evento y cada observador decide cómo procesarlo.

En **Spring Framework**, este patrón se encuentra implementado mediante el sistema de eventos de la aplicación, basado principalmente en las abstracciones **`org.springframework.context.ApplicationEvent`** y **`org.springframework.context.ApplicationListener`**, pertenecientes al módulo **`spring-context`**. A través de estas abstracciones, un componente puede publicar un evento y múltiples listeners pueden reaccionar ante él sin que el componente que genera el evento tenga que invocarlos directamente.

### ¿Dónde aparece en Spring Framework?

La implementación se encuentra principalmente en el módulo **`spring-context`**. La clase abstracta **`org.springframework.context.ApplicationEvent`** representa un evento producido dentro del contexto de una aplicación, mientras que la interfaz **`org.springframework.context.ApplicationListener<E extends ApplicationEvent>`** representa a los objetos interesados en recibir determinados eventos.

El funcionamiento se completa mediante el contexto de Spring y su infraestructura de publicación de eventos. Un componente puede publicar un evento utilizando `ApplicationEventPublisher`, mientras que Spring se encarga de localizar los listeners correspondientes y notificarles.

La estructura puede resumirse conceptualmente de la siguiente manera: un componente actúa como **subject o publisher**, el evento representa el cambio o suceso que debe comunicarse y los objetos que implementan `ApplicationListener` actúan como **observers**. De esta forma, varios componentes pueden reaccionar ante el mismo evento sin establecer dependencias directas entre ellos.

### ¿Qué problema resuelve en este contexto?

En una aplicación desarrollada con Spring pueden existir operaciones que necesiten desencadenar diferentes acciones posteriores. Por ejemplo, después de registrar un usuario, pueden ser necesarias acciones como enviar una notificación, registrar información adicional o actualizar otro componente.

Una implementación directa consistiría en que el servicio encargado de registrar al usuario llamara explícitamente a cada uno de esos componentes. Aunque esta solución funciona, genera un fuerte acoplamiento: el servicio necesita conocer qué componentes deben ejecutarse y en qué orden. Además, cada nueva reacción requeriría modificar el código del servicio que genera el evento.

El patrón Observer resuelve este problema separando la generación del acontecimiento de las acciones que reaccionan ante él. El componente productor publica un `ApplicationEvent` y no necesita conocer quién está interesado en recibirlo. Los diferentes listeners se registran en el contexto de Spring y reaccionan cuando reciben el evento correspondiente.

Esto permite agregar nuevos comportamientos sin modificar el componente que produce el evento. Por ejemplo, podría añadirse un nuevo `ApplicationListener` para generar una auditoría sin modificar el servicio que originalmente publica el evento. Esta característica resulta especialmente útil en arquitecturas donde diferentes componentes necesitan reaccionar ante una misma operación.

### ¿Qué evidencia de código lo confirma?

La interfaz `ApplicationListener` constituye una de las evidencias más claras del patrón Observer en Spring:

```java id="a5r7k2"
@FunctionalInterface
public interface ApplicationListener<E extends ApplicationEvent>
        extends EventListener {

    void onApplicationEvent(E event);

}
```

El método `onApplicationEvent` representa la operación mediante la cual el observador recibe la notificación. Cada implementación de `ApplicationListener` puede definir qué debe hacer cuando Spring publica un determinado tipo de evento.

La clase `ApplicationEvent` representa el acontecimiento que será comunicado:

```java id="x2m9qd"
public abstract class ApplicationEvent extends EventObject {

    private final long timestamp;

    public ApplicationEvent(Object source) {
        super(source);
        this.timestamp = System.currentTimeMillis();
    }

    public final long getTimestamp() {
        return this.timestamp;
    }
}
```

El evento contiene información sobre el acontecimiento y su origen, mientras que los listeners determinan qué comportamiento ejecutar cuando reciben esa notificación.

El mecanismo de publicación se encuentra abstraído mediante `ApplicationEventPublisher`, cuyo método principal es:

```java id="k7v3pn"
@FunctionalInterface
public interface ApplicationEventPublisher {

    default void publishEvent(ApplicationEvent event) {
        publishEvent((Object) event);
    }

    void publishEvent(Object event);
}
```

El componente que genera el evento solamente necesita utilizar el publicador. La resolución y notificación de los listeners queda bajo responsabilidad de la infraestructura de Spring.

Por ejemplo, conceptualmente un componente puede publicar:

```java id="r4n8wc"
publisher.publishEvent(new UsuarioRegistradoEvent(this, usuario));
```

Mientras que otro componente puede reaccionar implementando:

```java id="m6q1zs"
@Component
public class AuditoriaListener
        implements ApplicationListener<UsuarioRegistradoEvent> {

    @Override
    public void onApplicationEvent(
            UsuarioRegistradoEvent event) {
        // Procesar la notificación del evento.
    }
}
```

En este caso, `UsuarioRegistradoEvent` representa la información que se comunica, mientras que `AuditoriaListener` funciona como observador. El componente que registra al usuario no necesita conocer la existencia de `AuditoriaListener`.

### ¿Qué principio SOLID refuerza?

El patrón Observer aplicado mediante Spring Events refuerza principalmente el **Open/Closed Principle (OCP)** y el **Dependency Inversion Principle (DIP)**.

La relación con **OCP** se produce porque es posible agregar nuevos comportamientos creando nuevos listeners sin modificar el componente que publica el evento. El sistema queda abierto a la extensión mediante nuevos observadores, pero el código existente responsable de generar el evento permanece sin cambios.

También se relaciona con **DIP**, porque el productor no depende directamente de las implementaciones concretas que reaccionarán ante el evento. En lugar de llamar a clases específicas, utiliza una abstracción como `ApplicationEventPublisher`. Spring se encarga posteriormente de conectar el evento con los listeners registrados en el contexto.

Además, Observer contribuye a una separación clara de responsabilidades. El productor tiene la responsabilidad de generar y publicar el acontecimiento, mientras que cada listener se concentra en responder a ese acontecimiento. Esto favorece también el **Single Responsibility Principle (SRP)**, ya que las diferentes reacciones pueden mantenerse separadas en componentes independientes.

En conclusión, **Spring Events constituye una aplicación del patrón Observer**, donde `ApplicationEvent` representa el acontecimiento, `ApplicationEventPublisher` permite publicarlo y `ApplicationListener` representa a los observadores interesados. Esta arquitectura reduce el acoplamiento entre componentes y permite incorporar nuevas reacciones sin modificar el código que genera los eventos, reforzando principalmente los principios **Open/Closed** y **Dependency Inversion** de SOLID.

## Conclusiones

El análisis de Singleton, Decorator y Observer permite observar que los patrones de diseño no son únicamente soluciones aisladas para problemas de programación, sino mecanismos que los frameworks como Spring aplican de manera sistemática para organizar responsabilidades, reducir el acoplamiento y facilitar la extensibilidad del software. En Spring, el patrón Singleton se integra con la administración del ciclo de vida de los beans y el contenedor IoC; Observer permite desacoplar la generación de eventos de las acciones que deben ejecutarse como respuesta; y Decorator demuestra cómo el comportamiento puede ampliarse mediante objetos envolventes sin modificar directamente la implementación original. El estudio de estos patrones evidencia que su principal valor dentro de un framework está en resolver problemas recurrentes a una escala mayor y de manera consistente. Como lección para el diseño propio, resulta importante identificar primero las responsabilidades y relaciones entre componentes antes de elegir un patrón, evitando aplicarlos únicamente por seguir una estructura conocida; cuando se utilizan correctamente, los patrones contribuyen a diseños más mantenibles, extensibles y alineados con principios SOLID.

## Referencias

Freeman, E., Robson, E., Bates, B., & Sierra, K. (2004). *Head first design patterns*. O'Reilly Media.

Oracle. (2025). *Java Platform, Standard Edition documentation*. Oracle. https://docs.oracle.com/en/java/javase/

Spring. (2026). *Spring Framework reference documentation*. VMware, Inc. https://docs.spring.io/spring-framework/reference/

Spring. (2026). *Spring Framework source code*. GitHub. https://github.com/spring-projects/spring-framework

Gamma, E., Helm, R., Johnson, R., & Vlissides, J. (1994). *Design patterns: Elements of reusable object-oriented software*. Addison-Wesley.


