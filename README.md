# Proyecto Funkos Server Client- Java con H2
***
Integrantes: Jorge Alonso Cruz Vera, Joselyn Carolina Obando Fernandez.
***
Este proyecto es una aplicación simple en Java que utiliza H2 como base de datos. A continuación, se describen los pasos para configurar y ejecutar el proyecto.
## Requisitos
***
* Java 8 o superior
* Gradle
## Configuración
***
### Paso 1: Dependencias de Gradle
Agrega las siguientes dependencias a tu archivo `build.gradle`:

```kotlin
plugins {
    id("java")
    jacoco
    //shadowjar
    id("com.github.johnrengelman.shadow") version "7.1.2"

}

group = "org.develop"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    // version para compilar y ejecutar en Java 11, subir a 17
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

dependencies {
    implementation("io.projectreactor:reactor-core:3.5.10")
    implementation("io.r2dbc:r2dbc-h2:1.0.0.RELEASE")
    implementation("io.r2dbc:r2dbc-pool:1.0.0.RELEASE")
    implementation("com.google.code.gson:gson:2.10.1")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-junit-jupiter:5.5.0")
    testImplementation("org.mockito:mockito-core:5.5.0")
    implementation("org.projectlombok:lombok:1.18.28")
    testImplementation("org.projectlombok:lombok:1.18.28")
    annotationProcessor("org.projectlombok:lombok:1.18.28")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("com.auth0:java-jwt:4.2.1")
    implementation("org.mindrot:jbcrypt:0.4")
}

tasks.test {
    useJUnitPlatform()
}


tasks.shadowJar{
    manifest{
        attributes["Main-Class"] = "org.develop.main.Server"
    }

    dependsOn(tasks.test)
}

//tasks.jar {
//    manifest {
//        attributes["Main-Class"] = "org.develop.main.Server"
//    }
//    configurations["compileClasspath"].forEach { file: File ->
//        from(zipTree(file.absoluteFile))
//    }
//    duplicatesStrategy = DuplicatesStrategy.INCLUDE
//}
```
## Commons - Model - MainUse
***
### Paso 2: Crea la clase Funko.
La clase `Funko` representa un objeto coleccionable Funko. Esta clase incluye varios atributos y métodos para trabajar con coleccionables Funko.

```java
import lombok.Builder;
import lombok.Data;
import org.develop.commons.model.mainUse.Modelo;
import org.develop.commons.utils.locale.MyLocale;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Data
@Builder
public class Funko {
    private long myId;
    private int id;
    private UUID uuid;
    private String name;
    private Modelo modelo;
    private double precio;
    private LocalDate fecha_lanzamiento;
    @Builder.Default
    private LocalDateTime created_at = LocalDateTime.now();
    @Builder.Default
    private LocalDateTime updated_at = LocalDateTime.now();
    
    @Override
    public String toString() {
        return "Funko{" +
                "id=" + id +
                ", myid=" + myId +
                ", uuid=" + uuid +
                ", name='" + name + '\'' +
                ", modelo=" + modelo +
                ", precio=" + MyLocale.toLocalMoney(precio) +
                ", fecha_lanzamiento=" + MyLocale.toLocalDate(fecha_lanzamiento) +
                '}';
    }
    
    public Funko setFunko(String line){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String[] lineas = line.split(",");
        setUuid(UUID.fromString(lineas[0].length()>36?lineas[0].substring(0,35):lineas[0]));
        setName(lineas[1]);
        setModelo(Modelo.valueOf(lineas[2]));
        setPrecio(Double.parseDouble(lineas[3]));
        setFecha_lanzamiento(LocalDate.parse(lineas[4],formatter));

        return this;
    }
}
```

### Paso 3: Crear la clase Modelo
El enumerado `Modelo` representa diferentes categorías o modelos de productos, y se utiliza para clasificar elementos de una colección. En este caso, los modelos posibles son: "MARVEL", "DISNEY", "ANIME" y "OTROS".
El enumerado `Modelo` se utiliza en aplicaciones Java para categorizar o etiquetar elementos de una colección. Cada valor del enumerado representa un modelo específico.

```java
public enum Modelo {
    MARVEL,DISNEY,ANIME,OTROS;
}
```
### Paso 4: Crear la clase MyIDGenerator
La clase `MyIDGenerator` se utiliza para generar identificadores únicos en aplicaciones Java. Al llamar al método `getIDandIncrement()`, se obtiene un identificador único y se incrementa automáticamente para el siguiente uso.
```java
package org.develop.commons.model.mainUse;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyIDGenerator {

    private static MyIDGenerator instance;
    private static long id = 0;

    private static final Lock locker = new ReentrantLock(true);

    private MyIDGenerator(){}
    
    public static MyIDGenerator getInstance(){
        if (instance == null){
            instance = new MyIDGenerator();
        }
        return instance;
    }
    
    public Long getIDandIncrement(){
        locker.lock();
        id++;
        locker.unlock();
        return id;
    }
}
```
### Paso 5: Crear la clase Notificacion
La clase `Notificacion <T>` representa una notificación genérica que puede contener cualquier tipo de contenido.

```java
package org.develop.commons.model.mainUse;

public class Notificacion <T>{
    private Tipo tipo;
    private T contenido;
    
    public Notificacion(Tipo tipo, T contenido) {
        this.tipo = tipo;
        this.contenido = contenido;
    }
    
    public Tipo getTipo() {
        return tipo;
    }
    
    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }
    
    public T getContenido() {
        return contenido;
    }
    
    public void setContenido(T contenido) {
        this.contenido = contenido;
    }
    
    @Override
    public String toString() {
        return "Notificacion{" +
                "tipo=" + tipo +
                ", contenido=" + contenido +
                '}';
    }
    
    public enum Tipo {
        NEW, UPDATED, DELETED
    }
}
```
##  Commons - Model - ServerUse
***
### Paso 6: Crear el record Login.
La clase `Login` representa un registro que contiene la información de inicio de sesión de un usuario. Esta clase se utiliza para almacenar el nombre de usuario (username) y la contraseña (password) asociados con el inicio de sesión.
```java
package org.develop.commons.model.serverUse;

public record Login(String username, String password) {
}
```
***
### Paso 7: Crear el record User.
La clase `User` representa un registro que contiene la información de un usuario en la aplicación. Incluye atributos como el ID de usuario, el nombre de usuario, la contraseña y el rol del usuario. Además, esta clase define una enumeración `Role` que especifica los roles de usuario admitidos.
```java
package org.develop.commons.model.serverUse;

public record User(long id, String username, String password, Role role) {
   
    public enum Role{
        ADMIN, USER
    }
}
```
***
### Paso 8: Crear el record Request.
La clase `Request` representa un registro que contiene información sobre una solicitud enviada al servidor. Incluye atributos como el tipo de solicitud, su contenido, un token de autenticación y la marca de tiempo de creación. Además, esta clase define una enumeración `Type` que especifica los tipos de solicitud compatibles.

```java
package org.develop.commons.model.serverUse;

public record Request(Type type, String content, String token, String createdAt) {

        public enum Type {
        LOGIN, SALIR, OTRO, GETALL, GETBYID, GETBYMODEL, GETBYLAUNCHDATE,POST, UPDATE, DELETE
    }
}
```
***
### Paso 9: Crear el record Response.
La clase `Response` es un registro que representa una respuesta del servidor en la aplicación. Contiene información relevante sobre el estado de la respuesta, su contenido y la marca de tiempo de creación. Además, define una enumeración `Status` que especifica los estados de respuesta admitidos en la aplicación.

```java
package org.develop.commons.model.serverUse;

public record Response(Status status, String content, String createdAt) {
    
    public enum Status {
        OK, ERROR, BYE, TOKEN
    }
}
```
##  Commons - Utils - Adapters.
***
### Paso 10: Creamos las clases LocalDateAdapter, LocalDateTimeAdapter, UUUIDAdapter.
Creamos 3 adapters 2 para las fechas (LocalDateAdapter, LocalDateTimeAdapter) y uno para el UUIDAdapter. Locale para la salida de los datos segun el pais actual para el properties.
```java
package org.develop.commons.utils.adapters;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.develop.commons.model.serverUse.Request;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateAdapter implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String dateString = json.getAsString();
        return LocalDate.parse(dateString, formatter);
    }

    @Override
    public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
        String dateString = src.format(formatter);
        return new JsonPrimitive(dateString);
    }
}
```
```java
package org.develop.commons.utils.adapters;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String dateTimeString = json.getAsString();
        return LocalDateTime.parse(dateTimeString, formatter);
    }

    @Override
    public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
        String dateTimeString = src.format(formatter);
        return new JsonPrimitive(dateTimeString);
    }

}
```

```java
package org.develop.commons.utils.adapters;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.develop.commons.model.serverUse.Request;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.UUID;

public class UUIDAdapter implements JsonSerializer<UUID>, JsonDeserializer<UUID> {

    @Override
    public UUID deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String uuidString = json.getAsString();
        return UUID.fromString(uuidString);
    }
    
    @Override
    public JsonElement serialize(UUID src, Type typeOfSrc, JsonSerializationContext context) {
        String uuidString = src.toString();
        return new JsonPrimitive(uuidString);
    }
}
```
```java
package org.develop.commons.utils.locale;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class MyLocale {
    private static final Locale locale = new Locale("es","ES");
    
    //Estoy utilizando el objeto Locale creado para definir el formato de fecha y dinero
    //el problema es que no reconoce algunos simbolos.
    public static String toLocalDate(LocalDate date) {
        return date.format(
                DateTimeFormatter
                        .ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault())
        );
    }
    
    public static String toLocalMoney(double money) {
        return NumberFormat.getCurrencyInstance(Locale.getDefault()).format(money);
    }

}
```
```java
package org.develop.commons.utils.properties;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesReader {
    private final String fileName;
    private final Properties properties;
    
    public PropertiesReader(String fileName) throws IOException {
        this.fileName = fileName;
        properties = new Properties();

        InputStream file = getClass().getClassLoader().getResourceAsStream(fileName);
        if (file != null) {
            properties.load(file);
        } else {
            throw new FileNotFoundException("No se encuentra el fichero " + fileName);
        }
    }

    public String getProperty(String key) throws FileNotFoundException {
        String value = properties.getProperty(key);
        if (value != null) {
            return value;
        } else {
            throw new FileNotFoundException("No se encuentra la propiedad " + key + " en el fichero " + fileName);
        }
    }
}

```
## Repositories - CRUD.
***
### Paso 11: Creamos la inteface CRUDRepository.
La interfaz `CRUDRepository` es una interfaz genérica que define operaciones CRUD (Crear, Leer, Actualizar, Eliminar) básicas para trabajar con entidades en un repositorio. Esta interfaz proporciona métodos comunes para interactuar con los datos.
```java
package org.develop.repositories.crud;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CRUDRepository <T,ID>{
     // Métodos que vamos a usar
    // Buscar todos
    Flux<T> findAll();

    // Buscar por ID
    Mono<T> findById(ID id);

    // Guardar
    Mono<T> save(T t);

    // Actualizar
    Mono<T> update(T t);

    // Borrar por ID
    Mono<Boolean> deleteById(ID id);

    // Borrar todos
    Mono<Void> deleteAll();
}
```
## Repositories - FUNKOS.
***
### Paso 11: Creamos la inteface FunkoRepository.
La interfaz `FunkoRepository` extiende la interfaz `CRUDRepository` y proporciona operaciones específicas para trabajar con entidades Funko. Esta interfaz define métodos adicionales para buscar Funkos por nombre y UUID.
```java
package org.develop.repositories.funkos;

import org.develop.commons.model.mainUse.Funko;
import org.develop.repositories.crud.CRUDRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FunkoRepository extends CRUDRepository<Funko, Integer> {
    
    Flux<Funko> findByName(String name);
    
    Mono<Funko> findByUuid(UUID uuid);
}
```
***
### Paso 12: Creamos la clase FunkoRepositoryImpl.
La clase `FunkoRepositoryImpl` es una implementación de la interfaz `FunkoRepository` que proporciona métodos para interactuar con la base de datos y realizar operaciones CRUD en objetos Funko.
La clase `FunkoRepositoryImpl` se utiliza para realizar operaciones CRUD en objetos Funko y interactuar con la base de datos. A continuación, se describen los métodos implementados en esta clase:

### `findAll()`

Este método busca y devuelve todos los objetos Funko almacenados en la base de datos. Utiliza SQL para realizar la consulta y mapea los resultados a objetos Funko.

### `findById(Integer id)`

Este método busca un Funko en la base de datos por su ID y devuelve el Funko encontrado, si existe. Utiliza SQL con un parámetro de ID y mapea el resultado a un objeto Funko.

### `save(Funko funko)`

Este método guarda un Funko en la base de datos y devuelve el Funko guardado. Realiza una inserción en la base de datos utilizando SQL con los datos del Funko.

### `update(Funko funko)`

Este método actualiza un Funko en la base de datos con los nuevos valores proporcionados y devuelve el Funko actualizado. Utiliza SQL para actualizar los datos del Funko en la base de datos.

### `deleteById(Integer id)`

Este método elimina un Funko de la base de datos por su ID y devuelve un valor booleano que indica si la operación fue exitosa. Utiliza SQL para realizar la eliminación.

### `deleteAll()`

Este método elimina todos los Funkos de la base de datos y no devuelve ningún valor. Utiliza SQL para eliminar todos los registros de Funko en la base de datos.

### `findByName(String name)`

Este método busca y devuelve Funkos de la base de datos cuyos nombres contienen la cadena especificada. Utiliza SQL con un parámetro de nombre y mapea los resultados a objetos Funko.

### `findByUuid(UUID uuid)`

Este método busca un Funko en la base de datos por su UUID (Identificador Único Universal) y devuelve el Funko encontrado, si existe. Utiliza SQL con un parámetro de UUID y mapea el resultado a un objeto Funko.

```java
package org.develop.repositories.funkos;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.Result;
import org.develop.commons.model.mainUse.Funko;
import org.develop.commons.model.mainUse.Modelo;
import org.develop.commons.model.mainUse.MyIDGenerator;
import org.develop.services.database.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class FunkoRepositoryImpl implements FunkoRepository{

    private static FunkoRepositoryImpl instance;
    private final Logger logger = LoggerFactory.getLogger(FunkoRepositoryImpl.class);

    private final ConnectionPool connectionFactory;
    private final MyIDGenerator idGenerator;

    private FunkoRepositoryImpl(DatabaseManager databaseManager,MyIDGenerator idGenerator){
        this.connectionFactory = databaseManager.getConnectionPool();
        this.idGenerator = idGenerator;
    }
    
    public static FunkoRepositoryImpl getInstance(DatabaseManager db, MyIDGenerator idGenerator){
        if (instance == null){
            instance= new FunkoRepositoryImpl(db,idGenerator);
        }
        return instance;
    }
    
    @Override
    public Flux<Funko> findAll() {
        logger.debug("Buscando todos los alumnos");
        String sql = "SELECT * FROM FUNKO";
        return Flux.usingWhen(
                connectionFactory.create(),
                connection -> Flux.from(connection.createStatement(sql).execute())
                        .flatMap(result -> result.map((row, rowMetadata) ->
                                Funko.builder()
                                        .id(row.get("id",Integer.class))
                                        .myId(row.get("myid",Long.class))
                                        .name(row.get("name",String.class))
                                        .uuid(row.get("uuid", UUID.class))
                                        .modelo(Modelo.valueOf(row.get("modelo", Object.class).toString()))
                                        .precio(row.get("precio", Double.class))
                                        .fecha_lanzamiento(row.get("fecha_lanzamiento", LocalDate.class))
                                        .created_at(row.get("created_at", LocalDateTime.class))
                                        .updated_at(row.get("updated_at", LocalDateTime.class))
                                        .build()
                        )),
                Connection::close
        );
    }
    
    @Override
    public Mono<Funko> findById(Integer id) {
        logger.debug("Buscando Funko por ID");
        String sql = "SELECT * FROM FUNKO WHERE id = ?";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                        .bind(0, id)
                        .execute()
                ).flatMap(result -> Mono.from(result.map((row, rowMetadata) ->
                        Funko.builder()
                                        .id(row.get("id",Integer.class))
                                        .myId(row.get("myid",Long.class))
                                        .name(row.get("name",String.class))
                                        .uuid(row.get("uuid", UUID.class))
                                        .modelo(Modelo.valueOf(row.get("modelo", Object.class).toString()))
                                        .precio(row.get("precio",Double.class))
                                        .fecha_lanzamiento(row.get("fecha_lanzamiento", LocalDate.class))
                                        .created_at(row.get("created_at", LocalDateTime.class))
                                        .updated_at(row.get("updated_at", LocalDateTime.class))
                                        .build()
                ))),
                Connection::close
        );
    }
    
    @Override
    public Mono<Funko> save(Funko funko) {
        logger.debug("Saving Funko on DB");
        String sql = "INSERT INTO FUNKO (myid,uuid,name,modelo,precio,fecha_lanzamiento) VALUES (?,?,?,?,?,?)";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                        .bind(0, idGenerator.getIDandIncrement())
                        .bind(1, funko.getUuid())
                        .bind(2, funko.getName())
                        .bind(3,funko.getModelo().toString())
                        .bind(4,funko.getPrecio())
                        .bind(5,funko.getFecha_lanzamiento())
                        .execute()
        ).then(Mono.just(funko)),
        Connection::close
        );
    }
    
    @Override
    public Mono<Funko> update(Funko funko) {
        logger.debug("Updating Funko on DB");
        String sql = "UPDATE FUNKO SET name = ? , modelo = ?, precio = ?, updated_at = ? WHERE id = ?";
        funko.setUpdated_at(LocalDateTime.now());
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                        .bind(0,funko.getName())
                        .bind(1,funko.getModelo().toString())
                        .bind(2,funko.getPrecio())
                        .bind(3,funko.getUpdated_at())
                        .bind(4,funko.getId())
                        .execute()

                ).then(Mono.just(funko)),
                Connection::close
        );
    }
    
    @Override
    public Mono<Boolean> deleteById(Integer id) {
        logger.debug("Deleting Funko On DB");
        String sql = "DELETE FROM FUNKO WHERE id = ?";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                        .bind(0,id)
                        .execute()
                ).flatMapMany(Result::getRowsUpdated)
                 .hasElements(),
                Connection::close
        );
    }
    
    @Override
    public Mono<Void> deleteAll() {
        logger.debug("Deleting All Funkos On DB");
        String sql = "DELETE FROM FUNKO";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                           .execute()
                        ).then(),
                Connection::close
        );
    }
    
    @Override
    public Flux<Funko> findByName(String name) {
        logger.debug("Finding Funko From DB with Name: " + name);
        String sql = "SELECT * FROM FUNKO WHERE name like ?";
        return Flux.usingWhen(
                connectionFactory.create(),
                connection -> Flux.from(connection.createStatement(sql)
                        .bind(0, "%"+name+"%")
                        .execute()
                ).flatMap(result -> result.map((row,rowMetaData)->
                        Funko.builder()
                                .id(row.get("id",Integer.class))
                                .myId(row.get("myid",Long.class))
                                .uuid(row.get("uuid", UUID.class))
                                .name(row.get("name",String.class))
                                .modelo(Modelo.valueOf(row.get("modelo", Object.class).toString()))
                                .precio(row.get("precio", Double.class))
                                .fecha_lanzamiento(row.get("fecha_lanzamiento",LocalDate.class))
                                .created_at(row.get("created_at",LocalDateTime.class))
                                .updated_at(row.get("updated_at", LocalDateTime.class))
                                .build()
                )),Connection::close
        );
    }

    /**
     * Busca un Funko en la base de datos por su UUID y devuelve el Funko encontrado, si existe.
     *
     * @param uuid El UUID del Funko que se va a buscar en la base de datos.
     * @return Un mono (Mono) que representa el Funko encontrado por su UUID, o un mono vacio si no se encuentra.
     */
    @Override
    public Mono<Funko> findByUuid(UUID uuid) {
        logger.debug("Buscando funko por uuid: " + uuid);
        String sql = "SELECT * FROM FUNKO WHERE uuid = ?";
        return Mono.usingWhen(
                connectionFactory.create(),
                connection -> Mono.from(connection.createStatement(sql)
                        .bind(0, uuid)
                        .execute()
                ).flatMap(result -> Mono.from(result.map((row, rowMetadata) ->
                        Funko.builder()
                                        .id(row.get("id",Integer.class))
                                        .myId(row.get("myid",Long.class))
                                        .name(row.get("name",String.class))
                                        .uuid(row.get("uuid", UUID.class))
                                        .modelo(Modelo.valueOf(row.get("modelo", Object.class).toString()))
                                        .precio(row.get("precio",Double.class))
                                        .fecha_lanzamiento(row.get("fecha_lanzamiento", LocalDate.class))
                                        .created_at(row.get("created_at", LocalDateTime.class))
                                        .updated_at(row.get("updated_at", LocalDateTime.class))
                                        .build()
                ))),
                Connection::close
        );
    }
}
```
## Repositories - USERS.
***
### Paso 13: Creamos la inteface UserRepository.
La clase `UserRepository` representa un repositorio de usuarios con métodos para buscar usuarios por nombre de usuario o por ID. Es importante destacar que esta implementación es un repositorio ficticio que utiliza datos estáticos y en memoria en lugar de interactuar con una base de datos real.
```java
package org.develop.repositories.users;

import org.develop.commons.model.serverUse.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Optional;

public class UserRepository {
    private static UserRepository INSTANCE = null;
    private final List<User> users = List.of(
            new User(
                    1,
                    "pepe",
                    BCrypt.hashpw("pepe1234", BCrypt.gensalt(12)),
                    User.Role.ADMIN
            ),
            new User(
                    2,
                    "ana",
                    BCrypt.hashpw("ana1234", BCrypt.gensalt(12)),
                    User.Role.USER
            )
    );
    
    private UserRepository() {
    }
    
    public synchronized static UserRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new UserRepository();
        }
        return INSTANCE;
    }
    
    public Optional<User> findByUsername(String username) {
        return users.stream()
                .filter(user -> user.username().equals(username))
                .findFirst();
    }

    
    public Optional<User> findById(int id) {
        return users.stream()
                .filter(user -> user.id() == id)
                .findFirst();
    }
}
```
## SERVICES - CACHE.
***
### Paso 14: Creamos la inteface Cache.
La interfaz `Cache` define un conjunto de métodos para interactuar con una caché que almacena valores asociados a claves. Esta interfaz proporciona una abstracción genérica para operaciones de almacenamiento en caché y recuperación de datos de la caché.
```java
package org.develop.services.cache;

import reactor.core.publisher.Mono;

public interface Cache<K,V> {
    
    Mono<Void> put(K key, V value);
    
    Mono<V> get(K key);
    
    Mono<Void> remove(K key);
    
    void clear();

    void shutdown();
}
```
## SERVICES - FUNKOS.
***
### Paso 15: Creamos la interface FunkoCache.
La interfaz `FunkoCache` representa una caché específica para objetos Funko, donde los objetos Funko se almacenan y recuperan utilizando identificadores enteros (ID). Esta interfaz extiende la interfaz genérica `Cache`, lo que significa que hereda los métodos para interactuar con la caché.

```java
package org.develop.services.funkos;

import org.develop.commons.model.mainUse.Funko;
import org.develop.services.cache.Cache;

public interface FunkoCache extends Cache<Integer, Funko> {
}

```
***
### Paso 16: Creamos la clase FunkoCacheImpl.
La clase `FunkoCacheImpl` representa una implementación de una caché para objetos Funko que almacena y recupera objetos Funko utilizando identificadores enteros (ID). Esta caché tiene un tamaño máximo y utiliza un algoritmo de eliminación de objetos más antiguos cuando se alcanza el tamaño máximo. Además, se encarga de eliminar automáticamente los objetos caducados de la caché.

```java
package org.develop.services.funkos;

import lombok.Getter;
import org.develop.commons.model.mainUse.Funko;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FunkoCacheImpl implements FunkoCache{
    private final Logger logger = LoggerFactory.getLogger(FunkoCacheImpl.class);
  
    @Getter
    private final int maxSize;
    @Getter
    private final Map<Integer, Funko> cache;
    @Getter
    private final ScheduledExecutorService cleaner;

    public FunkoCacheImpl(int maxSize){
        this.maxSize = maxSize;
        this.cache = new LinkedHashMap<>(maxSize,0.75f,true){
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, Funko> eldest) {
                return size() > maxSize;
            }
        };

        this.cleaner = Executors.newSingleThreadScheduledExecutor();
        this.cleaner.scheduleAtFixedRate(this::clear,2,2, TimeUnit.MINUTES);
    }
    
    @Override
    public Mono<Void> put(Integer key, Funko value) {
        logger.debug("Añadinedo Funko en la Cache id: " + key);
        return Mono.fromRunnable(()->cache.put(key,value));
    }
    
    @Override
    public Mono<Funko> get(Integer key) {
        logger.debug("Obteniendo Funko de la Cache con id: " + key);
        return Mono.justOrEmpty(cache.get(key));
    }
    
    @Override
    public Mono<Void> remove(Integer key) {
        logger.debug("Eliminando Funko de la Cache con id: " + key);
        return Mono.fromRunnable(()-> cache.remove(key));
    }

    @Override
    public void clear() {
        cache.entrySet().removeIf(entry -> {
            boolean shouldRemove = entry.getValue().getUpdated_at().plusMinutes(1).isBefore(LocalDateTime.now());
            if (shouldRemove) {
                logger.debug("Autoeliminando por caducidad funko de cache con id: " + entry.getKey());
            }
            return shouldRemove;
        });
    }

 
    @Override
    public void shutdown() {
        cleaner.shutdown();
    }
}
```
***
### Paso 17: Creamos la interface FunkoNotification.
La interfaz `FunkoNotification` define un conjunto de métodos para notificar y obtener notificaciones relacionadas con objetos Funko. Esta interfaz proporciona una abstracción genérica para la comunicación y el flujo de notificaciones.
```java
package org.develop.services.funkos;

import org.develop.commons.model.mainUse.Funko;
import org.develop.commons.model.mainUse.Notificacion;
import reactor.core.publisher.Flux;

public interface FunkoNotification {

    Flux<Notificacion<Funko>> getNotificationAsFlux();
    
    void notify(Notificacion<Funko> notificacion);
}
```
***
### Paso 18: Creamos la clase FunkoNotificationImpl.
La clase `FunkoNotificationImpl` implementa la interfaz `FunkoNotification` y proporciona métodos para notificar y obtener notificaciones relacionadas con objetos Funko. Esta clase utiliza Reactor, una biblioteca reactiva de Java, para gestionar un flujo (Flux) de notificaciones.
```java
package org.develop.services.funkos;

import org.develop.commons.model.mainUse.Funko;
import org.develop.commons.model.mainUse.Notificacion;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

public class FunkoNotificationImpl implements FunkoNotification{
    private static FunkoNotificationImpl instance;
    private final Flux<Notificacion<Funko>> funkosNotificationFlux;
    private FluxSink<Notificacion<Funko>> funkosNotification;
    
    private FunkoNotificationImpl(){
        this.funkosNotificationFlux = Flux.<Notificacion<Funko>> create(emitter -> this.funkosNotification = emitter).share();
    }
    
    public static FunkoNotificationImpl getInstance(){
        if (instance == null) instance= new FunkoNotificationImpl();
        return instance;
    }

    
    @Override
    public Flux<Notificacion<Funko>> getNotificationAsFlux() {
        return funkosNotificationFlux;
    }
    
    @Override
    public void notify(Notificacion<Funko> notificacion) {
        funkosNotification.next(notificacion);
    }
}
```
***
### Paso 19: Creamos la interface FunkoService.
La interfaz `FunkoService` define un conjunto de operaciones y métodos para el servicio relacionado con objetos Funko. Esta interfaz proporciona una abstracción genérica para realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar) en objetos Funko, así como operaciones de respaldo e importación.
```java
package org.develop.services.funkos;

import org.develop.commons.model.mainUse.Funko;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FunkoService {
        // Buscar todos
    Flux<Funko> findAll();

    // Buscar por ID
    Mono<Funko> findById(Integer id);
    //Buscar por nombre
    Flux<Funko> findByName(String name);

    // Guardar
    Mono<Funko> save(Funko funko);

    // Actualizar
    Mono<Funko> update(Funko funko);

    // Borrar por ID
    Mono<Funko> deleteById(Integer id);

    // Borrar todos
    Mono<Void> deleteAll();

    Mono<Boolean> backup(String file);

    Flux<Funko> imported(String file);
    Mono<Funko> findByUuid(UUID uuid);


}
```
***
### Paso 20: Creamos la clase FunkoServiceImpl.
La clase `FunkoServiceImpl` es una implementación de la interfaz `FunkoService`, que proporciona una serie de operaciones para gestionar objetos Funko en una aplicación. La implementación incluye el uso de una caché para optimizar el rendimiento y notificaciones relacionadas con los objetos Funko.

```java
package org.develop.services.funkos;

import org.develop.exceptions.funkos.FunkoNotFoundException;
import org.develop.commons.model.mainUse.Funko;
import org.develop.commons.model.mainUse.Notificacion;
import org.develop.repositories.funkos.FunkoRepository;
import org.develop.services.files.BackupManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class FunkoServiceImpl implements FunkoService{
    private static final int CACHE_SIZE = 10;

    private static FunkoServiceImpl instance;
    private final FunkoCache cache;
    private final FunkoNotification notification;
    private final Logger logger = LoggerFactory.getLogger(FunkoServiceImpl.class);
    private final FunkoRepository funkoRepository;
    private final BackupManagerImpl backupManager;
    
    private FunkoServiceImpl(FunkoRepository funkoRepository, FunkoNotification notification,BackupManagerImpl backupManager){
        this.funkoRepository=funkoRepository;
        this.cache = new FunkoCacheImpl(CACHE_SIZE);
        this.notification = notification;
        this.backupManager = backupManager;
    }

   
    public static FunkoServiceImpl getInstance(FunkoRepository funkoRepository, FunkoNotification notification,BackupManagerImpl backupManager){
        if (instance==null) instance=new FunkoServiceImpl(funkoRepository,notification,backupManager);

        return instance;
    }
    
    @Override
    public Flux<Funko> findAll() {
        logger.debug("Buscando todos los Funkos");
        return funkoRepository.findAll();
    }

    
    @Override
    public Mono<Funko> findById(Integer id) {
        logger.debug("Buscando Funko por ID: " + id);
        return cache.get(id)
                .switchIfEmpty(funkoRepository.findById(id)
                        .flatMap(funko -> cache.put(funko.getId(),funko)
                                .then(Mono.just(funko)))
                        .switchIfEmpty(Mono.error(new FunkoNotFoundException("Funko with id " + id + " not found"))));
    }
    
    @Override
    public Flux<Funko> findByName(String name) {
        logger.debug("Buscando todos los funkos por nombre: " + name);
        return funkoRepository.findByName(name);
    }
    
    public Mono<Funko> saveWithOutNotification(Funko funko){
         return funkoRepository.save(funko)
                .flatMap(saved -> funkoRepository.findByUuid(saved.getUuid()));
    }

    @Override
    public Mono<Funko> save(Funko funko) {
        logger.debug("Guardando Funko " + funko.getName());
        return saveWithOutNotification(funko)
                .doOnSuccess(fkSaved -> notification.notify(new Notificacion<>(Notificacion.Tipo.NEW,fkSaved)));
    }

   
    public Mono<Funko> updateWithOutNotification(Funko funko){
        return funkoRepository.update(funko)
                .flatMap(updated->findById(updated.getId()));
    }

    @Override
    public Mono<Funko> update(Funko funko) {
        logger.debug("Actualizando Funko " + funko.getName());
        return updateWithOutNotification(funko)
                .doOnSuccess(fkUpdated->notification.notify(new Notificacion<>(Notificacion.Tipo.UPDATED,fkUpdated)));
    }
    
    public Mono<Funko> deleteByIdWithOutNotification(Integer id){
        return funkoRepository.findById(id)
                .switchIfEmpty(Mono.error(new FunkoNotFoundException("Funko with id " + id + " not found")))
                .flatMap(funko -> cache.remove(funko.getId())
                        .then(funkoRepository.deleteById(funko.getId()))
                        .thenReturn(funko));
    }
    
    @Override
    public Mono<Funko> deleteById(Integer id) {
        logger.debug("Eliminando Funko con ID " + id);
        return deleteByIdWithOutNotification(id)
                .doOnSuccess(deleted -> notification.notify(new Notificacion<>(Notificacion.Tipo.DELETED,deleted)));
    }
    
    @Override
    public Mono<Void> deleteAll() {
        logger.debug("Eliminando todos los Funkos");
        cache.clear();
        return funkoRepository.deleteAll()
                .then(Mono.empty());
    }

   
    @Override
    public Mono<Boolean> backup(String file) {
        logger.debug("Realizando Backup de Funkos");
        return findAll()
                .collectList()
                .flatMap(funkos -> backupManager.writeFile(file,funkos));
    }
    
    @Override
    public Flux<Funko> imported(String file) {
        return backupManager.readFile(file);
    }

    @Override
    public Mono<Funko> findByUuid(UUID uuid) {
        logger.debug("Buscando Funko por UUID: " + uuid);
        return funkoRepository.findByUuid(uuid);
    }

    public Flux<Notificacion<Funko>> getNotifications(){
        return notification.getNotificationAsFlux();
    }

}
```
## SERVICES - TOKEN
***
### Paso 21: Creamos la clase TokenService.
La clase TokenService proporciona métodos para la creación y verificación de tokens JWT (JSON Web Tokens) en aplicaciones Java. Los tokens JWT son ampliamente utilizados para gestionar la autenticación y autorización de usuarios en aplicaciones web y servicios. Esta clase permite generar tokens JWT para usuarios, verificar la validez de los tokens y obtener las reclamaciones (claims) de un token verificado.
```java
package org.develop.services.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.develop.commons.model.serverUse.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class TokenService {
    private static TokenService INSTANCE = null;
    private final Logger logger = LoggerFactory.getLogger(TokenService.class);
    
    private TokenService() {
    }
    
    public synchronized static TokenService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TokenService();
        }
        return INSTANCE;
    }
    
    public String createToken(User user, String tokenSecret, long tokenExpiration) {
        logger.debug("Creando token");
        Algorithm algorithm = Algorithm.HMAC256(tokenSecret);
        return JWT.create()
                //.withIssuer("2DAW") // Quien lo emite *
                //.withSubject("Desarrollo Web Entornos Servidor") // Para que lo emite *
                // Solo guardar el id del usuario, lo demas siempre lo podemos recuperar
                // y no damos pistas al atacante
                .withClaim("userid", user.id()) // Datos que queremos guardar * (al menos algunos)
                .withClaim("username", user.username()) // Datos que queremos guardar
                .withClaim("rol", user.role().toString()) // Datos que queremos guardar
                .withIssuedAt(new Date()) // Fecha de emision *
                .withExpiresAt(new Date(System.currentTimeMillis() + tokenExpiration)) // Fecha de expiracion *
                //.withJWTId(UUID.randomUUID().toString()) // Identificador unico del token
                //.withNotBefore(new Date(System.currentTimeMillis() + 1000L)) // Fecha de cuando se puede usar
                .sign(algorithm); // Firmamos el token
    }
    
    public boolean verifyToken(String token, String tokenSecret, User user) {
        logger.debug("Verificando token");
        Algorithm algorithm = Algorithm.HMAC256(tokenSecret);
        try {
            JWTVerifier verifier = JWT.require(algorithm)
                    .build(); // Creamos el verificador
            DecodedJWT decodedJWT = verifier.verify(token);
            logger.debug("Token verificado");
            // Comprobamos que el token es del usuario
            // Solo compruebas los datos obligatorios, en este caso el id
            // meter mas información es dar pistas al atacante
            return decodedJWT.getClaim("userid").asLong() == user.id() &&
                    decodedJWT.getClaim("username").asString().equals(user.username()) &&
                    decodedJWT.getClaim("rol").asString().equals(user.role().toString());
        } catch (Exception e) {
            logger.error("Error al verificar el token: " + e.getMessage());
            return false;
        }
    }
    
    public boolean verifyToken(String token, String tokenSecret) {
        logger.debug("Verificando token");
        Algorithm algorithm = Algorithm.HMAC256(tokenSecret);
        try {
            JWTVerifier verifier = JWT.require(algorithm)
                    .build(); // Creamos el verificador
            DecodedJWT decodedJWT = verifier.verify(token);
            logger.debug("Token verificado");
            return true;
        } catch (Exception e) {
            logger.error("Error al verificar el token: " + e.getMessage());
            return false;
        }
    }

    public java.util.Map<String, com.auth0.jwt.interfaces.Claim> getClaims(String token, String tokenSecret) {
        logger.debug("Verificando token");
        Algorithm algorithm = Algorithm.HMAC256(tokenSecret);
        try {
            JWTVerifier verifier = JWT.require(algorithm)
                    .build(); // Creamos el verificador
            DecodedJWT decodedJWT = verifier.verify(token);
            logger.debug("Token verificado");
            return decodedJWT.getClaims();
        } catch (Exception e) {
            logger.error("Error al verificar el token: " + e.getMessage());
            return null;
        }
    }
}
```
## SERVICES - DATABASE
***
### Paso 22: Creamos la clase DatabaseManager.
La clase DatabaseManager es responsable de gestionar la base de datos y la conexión a la misma en una aplicación Java. Proporciona métodos para inicializar tablas en la base de datos, ejecutar scripts SQL y obtener un pool de conexiones para interactuar con la base de datos.
```java
package org.develop.services.database;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.Properties;
import java.util.stream.Collectors;

public class DatabaseManager {

    private static DatabaseManager instance;
    private final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private final ConnectionFactory connectionFactory;
    private final ConnectionPool pool;
    private String serverUrl;
    private String dataBaseName;
    private boolean chargeInit;
    private String conURL;
    private String initScript;

    private DatabaseManager(){
        configFromProperties();

        connectionFactory = ConnectionFactories.get(conURL);

        ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration
                .builder(connectionFactory)
                .maxIdleTime(Duration.ofMillis(1000))
                .maxSize(20)
                .build();

        pool = new ConnectionPool(configuration);

        if (chargeInit){
            initTables();
        }
    }
    
    public static DatabaseManager getInstance(){
        if (instance == null){
            instance=new DatabaseManager();
        }
        return instance;
    }

    private synchronized void configFromProperties() {
        try {
            Properties properties = new Properties();
            properties.load(DatabaseManager.class.getClassLoader().getResourceAsStream("config.properties"));

            serverUrl = properties.getProperty("database.url", "jdbc:h2");
            dataBaseName = properties.getProperty("database.name", "Funkos");
            chargeInit = Boolean.parseBoolean(properties.getProperty("database.initDatabase", "false"));
            conURL = properties.getProperty("database.connectionUrl", serverUrl + ":" + dataBaseName + ".db");
            System.out.println(conURL);
            initScript = properties.getProperty("database.initScript", "init.sql");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public synchronized void initTables(){
        logger.debug("Borrando tablas de la Base de Datos");
        executeScript("delete.sql").block();
        logger.debug("Inicializando tablas de la BD");
        executeScript("init.sql").block();
        logger.debug("Tabla inicializada correctamente");
    }
    
        public synchronized Mono<Void> executeScript(String script){
            logger.debug("Executing of Init Script: " + script);
                    return Mono.usingWhen(
                connectionFactory.create(),
                connection -> {
                    logger.debug("Creando conexión con la base de datos");
                    String scriptContent = null;
                    try {
                        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(script)) {
                            if (inputStream == null) {
                                return Mono.error(new IOException("No se ha encontrado el fichero de script de inicialización de la base de datos"));
                            } else {
                                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                                    scriptContent = reader.lines().collect(Collectors.joining("\n"));
                                }
                            }
                        }
                        // logger.debug(scriptContent);
                        Statement statement = connection.createStatement(scriptContent);
                        return Mono.from(statement.execute());
                    } catch (IOException e) {
                        return Mono.error(e);
                    }
                },
                Connection::close
        ).then();
    }
    
    public ConnectionPool getConnectionPool(){
        return this.pool;
    }
}

```

## SERVICES - FILES
***
### Paso 23: Creamos la interface BackupManager.
La interfaz BackupManager define operaciones para gestionar copias de seguridad de datos. Permite leer datos desde un archivo y escribir datos en un archivo para realizar tareas de respaldo e importación.
```java
package org.develop.services.files;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


public interface BackupManager <T>{

    Flux<T> readFile(String path);

  
    Mono<Boolean> writeFile(String path, List<T> list);
}

```
***
### Paso 24: Creamos la clase BackupManagerImpl.
La clase BackupManagerImpl es una implementación de la interfaz BackupManager diseñada para gestionar operaciones de lectura y escritura de copias de seguridad de objetos Funko. Esta implementación específica permite leer datos desde un archivo CSV y escribir datos en un archivo JSON para realizar copias de seguridad.
```java
package org.develop.services.files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.develop.commons.utils.adapters.LocalDateAdapter;
import org.develop.commons.utils.adapters.LocalDateTimeAdapter;
import org.develop.commons.model.mainUse.Funko;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class BackupManagerImpl implements BackupManager<Funko> {
    private static BackupManagerImpl instance;
    private Logger logger = LoggerFactory.getLogger(BackupManagerImpl.class);

    private BackupManagerImpl(){

    }
    
    public static BackupManagerImpl getInstance(){
        if (instance == null) instance=new BackupManagerImpl();

        return instance;
    }
    
    @Override
    public Flux<Funko> readFile(String nomFile) {
        logger.debug("Leyendo fichero CSV");
        String path = Paths.get("").toAbsolutePath().toString() + File.separator + "data" + File.separator + nomFile;
        return Flux.create(sink->{
        try(BufferedReader reader =new BufferedReader(new FileReader(path))){
                String line;
                reader.readLine();
                while ((line = reader.readLine()) != null){
                        Funko fk = Funko.builder().build();
                        fk.setFunko(line);
                    sink.next(fk);
                }
                sink.complete();
        }catch (Exception e){
                logger.error("Error: " + e.getMessage(), e);
        }
        });
    }
    
    @Override
    public Mono<Boolean> writeFile(String nomFile, List<Funko> list) {
        logger.debug("Escribiendo fichero JSON");
        String path = Paths.get("").toAbsolutePath().toString() + File.separator + "data" + File.separator + nomFile;
        return Mono.create(sink ->{
                   Gson gs = new GsonBuilder()
                            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                            .setPrettyPrinting()
                            .create();

                    boolean success = false;
                    try (FileWriter writer = new FileWriter(path)) {
                        gs.toJson(list, writer);
                        success = true;
                    } catch (Exception e) {
                        logger.error("Error: "+e.getMessage(), e);
                }
                    sink.success(success);
                }
                );
    }
}

```
## EXCEPTIONS - CLIENT - FUNKOS - SERVER
***
### Paso 25: Creamos la clase ClientException.
La clase ClientException es una excepción personalizada que extiende la clase base Exception. Esta excepción se utiliza para representar errores o excepciones relacionados con el cliente en una aplicación.
```java
package org.develop.exceptions.client;

public class ClientException extends Exception {
   
    public ClientException(String message) {
        super(message);
    }
}

```
### Paso 26: Creamos la clase FunkoException.
La clase FunkoException es una excepción personalizada que extiende la clase base RuntimeException. Esta excepción se utiliza para representar errores o excepciones relacionados con los objetos Funko en una aplicación.
```java
package org.develop.exceptions.funkos;

public class FunkoException extends RuntimeException{
    
    public FunkoException(String message){
        super(message);
    }
}

```
### Paso 27: Creamos la clase FunkoNotFoundException.
La clase FunkoNotFoundException es una excepción personalizada que extiende la clase FunkoException. Esta excepción se utiliza para representar errores o excepciones específicos relacionados con la no existencia de un objeto Funko 
```java
package org.develop.exceptions.funkos;

public class FunkoNotFoundException extends FunkoException{

    
    public FunkoNotFoundException(String message) {
        super(message);
    }
}
```

### Paso 28: Creamos la clase FunkoNotSaveException.

FunkoNotSaveException
La clase FunkoNotSaveException es una excepción personalizada que extiende la clase FunkoException. Esta excepción se utiliza para representar errores o excepciones específicos relacionados con la no capacidad de guardar un objeto Funko
```java
package org.develop.exceptions.funkos;

public class FunkoNotSaveException extends FunkoException{
    
    public FunkoNotSaveException(String message) {
        super(message);
    }
}

```
### Paso 29: Creamos la clase ServerException.
La clase ServerException es una excepción personalizada que extiende la clase Exception. Esta excepción se utiliza para representar errores o excepciones específicos relacionados con problemas en el servidor
```java
package org.develop.exceptions.server;

public class ServerException extends Exception {
  
    public ServerException(String message) {
        super(message);
    }
}

```

## CLIENT
***
### Paso 30: Creamos la clase Client.
El cliente se comunica con el servidor a través de sockets seguros (SSL/TLS) y envía solicitudes en formato JSON utilizando la biblioteca Gson. El servidor debe proporcionar los puntos finales de API correspondientes para manejar las operaciones solicitadas por el cliente.
```java
package org.develop.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.develop.commons.model.mainUse.Funko;
import org.develop.commons.model.mainUse.Modelo;
import org.develop.commons.model.serverUse.Login;
import org.develop.commons.model.serverUse.Request;
import org.develop.commons.model.serverUse.Response;
import org.develop.commons.utils.adapters.LocalDateAdapter;
import org.develop.commons.utils.adapters.LocalDateTimeAdapter;
import org.develop.commons.utils.adapters.UUIDAdapter;
import org.develop.commons.utils.properties.PropertiesReader;
import org.develop.exceptions.client.ClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Client {
 private static final String HOST = "localhost";
    private static final int PORT = 3000;
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    private String username;
    private String password;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(UUID.class, new UUIDAdapter()).create();
    private SSLSocket socket;
    private PrintWriter out;
    private BufferedReader in;


   
    public static void main(String[] args) {
        Client client = new Client();
        try {
            client.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

   
    public void start() throws IOException {
        try {

            setUser("pepe");
            setPassword("pepe1234");


            openConnection();

            String token = sendRequestLogin();

            sendRequestGetAllFunkos(token);

            sendRequestGetFunkoById(token,"10");

            sendRequestGetFunkosByModel(token,"Marvel");

            sendRequestGetFunkosByDate(token,"2023");

            Funko fkn = Funko.builder()
                    .myId(100L)
                            .id(100)
                            .uuid(UUID.randomUUID())
                            .name("Funko Update Num 10 NUEVO")
                                    .precio(1000.1)
                                            .modelo(Modelo.OTROS)
                                        .fecha_lanzamiento(LocalDate.now())
                                                    .build();

            sendRequestPostFunko(token,fkn);

            fkn.setName("Funko Changes");
            fkn.setId(91);

            sendRequestUpdateFunko(token,fkn);

            sendRequestGetFunkoById(token,"91");

            sendRequestDeleteFunko(token,Funko.builder().id(19).build());

            sendRequestGetFunkoById(token,"19");

            sendRequestSalir();

        } catch (ClientException ex) {
            logger.error("Error: " + ex.getMessage());
            closeConnection();
            System.exit(1);
        }
    }
  
    private void closeConnection() throws IOException {
        logger.debug("Cerrando la conexión con el servidor: " + HOST + ":" + PORT);
        logger.info("🔵 Cerrando Cliente");
        if (in != null)
            in.close();
        if (out != null)
            out.close();
        if (socket != null)
            socket.close();
    }

   
    private void openConnection() throws IOException {
        logger.info("🔵 Iniciando Cliente");
        Map<String,String> myConfig = readConfigFile();

        logger.debug("Cargando Fichero de Propiedades");
        System.setProperty("javax.net.ssl.trustStore", myConfig.get("keyFile"));
        System.setProperty("javax.net.ssl.trustStorePassword", myConfig.get("keyPassword"));

        SSLSocketFactory clientFactory =(SSLSocketFactory) SSLSocketFactory.getDefault();
        socket = (SSLSocket) clientFactory.createSocket(HOST, PORT);

        logger.debug("Conectando al server: " + HOST + ":" + PORT);

        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        logger.info("✅ Cliente conectado a " + HOST + ":" + PORT);

    }
    
    public static Map<String,String> readConfigFile(){
        try{
            logger.debug("Reading config file");
            PropertiesReader propertiesReader = new PropertiesReader("client.properties");

            String keyFile= propertiesReader.getProperty("keyFile");
            String keyPassword = propertiesReader.getProperty("keyPassword");

            if (keyFile.isEmpty() || keyPassword.isEmpty()){
                throw new IllegalStateException("Missing keyFile or keyPassword");
            }

            if (!Files.exists(Path.of(keyFile))){
                throw new FileNotFoundException("No se encuentra el fichero " + keyFile);
            }

            Map<String,String> configMap = new HashMap<>();
            configMap.put("keyFile", keyFile);
            configMap.put("keyPassword", keyPassword);

            return configMap;
        }catch (FileNotFoundException e){
            logger.error("Error en clave: " + e.getLocalizedMessage());
            System.exit(1);
            return null;
        }
        catch (IOException e) {
          logger.error("Error al leer el fichero de propiedades: " + e.getLocalizedMessage());
            return null;
        }
    }

    private String sendRequestLogin() throws ClientException{
        String myToken = null;
        var loginGson = gson.toJson(new Login(username,password));

        Request request = new Request(Request.Type.LOGIN, loginGson, null, LocalDateTime.now().toString());
        logger.debug("Request Send: " + request);

        out.println(gson.toJson(request));

        try {
            Response response = gson.fromJson(in.readLine(),new TypeToken<Response>(){}.getType());

            logger.debug("Response Received Type: " + response.status());

            switch (response.status()){
                case TOKEN ->{
                    logger.info("🟢 Mi token es: " + response.content());
                    myToken = response.content();
                }
                default -> throw new ClientException("Unexpected response status: " + response.status());
            }
        } catch (IOException e) {
            logger.error("Error: " + e.getMessage());
        }

        return myToken;
    }

   
    private void sendRequestSalir() throws IOException, ClientException {
        Request request = new Request(Request.Type.SALIR, null, null, LocalDateTime.now().toString());
        logger.debug("Request Sent: " + request);

        out.println(gson.toJson(request));

        Response response = gson.fromJson(in.readLine(),new TypeToken<Response>(){}.getType());
        logger.debug("Response Received Type: " + response.status());

        switch (response.status()){
            case BYE -> {
                logger.info("🟢 Adios");
                closeConnection();
            }
            case ERROR -> logger.error("🔴 Error: " + response.content());
            default -> throw new ClientException("Unexpected response status: " + response.status());
        }
    }

    private void sendRequestGetAllFunkos(String token) throws ClientException, IOException {
        Request request = new Request(Request.Type.GETALL, null, token, LocalDateTime.now().toString());
        logger.debug("Request Send: " + request);

        out.println(gson.toJson(request));

        Response response = gson.fromJson(in.readLine(),new TypeToken<Response>(){}.getType());
        logger.debug("Response Received Type: " + response.status());

        switch (response.status()){
            case OK -> {
                List<Funko> responseFunkos = gson.fromJson(response.content(),new TypeToken<List<Funko>>(){}.getType());
                logger.info("🟢 Los funkos son: " + responseFunkos);
            }
            case ERROR -> logger.error("🔴 Error: " + response.content());
            default -> throw new ClientException("Unexpected response status: " + response.status());
        }
    }
   
    private void sendRequestGetFunkoById(String token, String id) throws ClientException, IOException {
        Request request = new Request(Request.Type.GETBYID, id, token, LocalDateTime.now().toString());
        logger.debug("Request Send: " + request);

        out.println(gson.toJson(request));

        Response response = gson.fromJson(in.readLine(),new TypeToken<Response>(){}.getType());
        logger.debug("Response Received Type: " + response.status());

        switch (response.status()){
            case OK -> {
                Funko responseFunko = gson.fromJson(response.content(),new TypeToken<Funko>(){}.getType());
                logger.info("🟢 El Funko es: " + responseFunko);
            }
            case ERROR -> logger.error("🔴 Error: " + response.content());
            default -> throw new ClientException("Unexpected response status: " + response.status());
        }
    }
    
    private void sendRequestGetFunkosByModel(String token, String model) throws ClientException, IOException {
        Request request = new Request(Request.Type.GETBYMODEL, model.toUpperCase(), token, LocalDateTime.now().toString());
        logger.debug("Request Send: " + request);

        out.println(gson.toJson(request));

        Response response = gson.fromJson(in.readLine(),new TypeToken<Response>(){}.getType());
        logger.debug("Response Received Type: " + response.status());

        switch (response.status()) {
            case OK -> {
                List<Funko> responseFunkos = gson.fromJson(response.content(),new TypeToken<List<Funko>>(){}.getType());
                logger.info("🟢 Los funkos con Modelo " + model +" son: " + responseFunkos);
            }
            case ERROR -> logger.error("🔴 Error: " + response.content());
            default -> throw new ClientException("Unexpected response status: " + response.status());
        }
    }
    
    private void sendRequestGetFunkosByDate(String token, String date) throws ClientException, IOException {
        Request request = new Request(Request.Type.GETBYLAUNCHDATE, date, token, LocalDateTime.now().toString());
        logger.debug("Request Send: " + request);

        out.println(gson.toJson(request));

        Response response = gson.fromJson(in.readLine(),new TypeToken<Response>(){}.getType());
        logger.debug("Response Received Type: " + response.status());

        switch (response.status()) {
            case OK -> {
                List<Funko> responseFunkos = gson.fromJson(response.content(),new TypeToken<List<Funko>>(){}.getType());
                logger.info("🟢 Los funkos con fecha de lanzamiento " + date +" son: " + responseFunkos);
            }
            case ERROR -> logger.error("🔴 Error: " + response.content());
            default -> throw new ClientException("Unexpected response status: " + response.status());
        }
    }

   
    private void sendRequestPostFunko(String token, Funko funko) throws ClientException, IOException {
        Request request = new Request(Request.Type.POST, gson.toJson(funko), token, LocalDateTime.now().toString());
        logger.debug("Request Send: " + request);

        out.println(gson.toJson(request));

        Response response = gson.fromJson(in.readLine(),new TypeToken<Response>(){}.getType());
        logger.debug("Response Received Type: " + response.status());

        switch (response.status()) {
            case OK -> {
                Funko responseFunko = gson.fromJson(response.content(),new TypeToken<Funko>(){}.getType());
                logger.info("🟢 El Funko fue creado con exito");
                logger.info(responseFunko.toString());
            }
            case ERROR -> logger.error("🔴 Error: " + response.content());
            default -> throw new ClientException("Unexpected response status: " + response.status());
        }
    }
    
    private void sendRequestUpdateFunko(String token, Funko funko) throws ClientException, IOException {
        Request request = new Request(Request.Type.UPDATE, gson.toJson(funko),token,LocalDateTime.now().toString());
        logger.debug("Request Send: " + request);

        out.println(gson.toJson(request));

        Response response = gson.fromJson(in.readLine(),new TypeToken<Response>(){}.getType());
        logger.debug("Response Received Type: " + response.status());

        switch (response.status()) {
            case OK -> {
                Funko responseFunko = gson.fromJson(response.content(),new TypeToken<Funko>(){}.getType());
                logger.info("🟢 El Funko fue actualizado con exito");
                logger.info(responseFunko.toString());
            }
            case ERROR -> logger.error("🔴 Error: " + response.content());
            default -> throw new ClientException("Unexpected response status: " + response.status());
        }
    }
    
    private void sendRequestDeleteFunko(String token, Funko funko) throws ClientException, IOException {
        Request request = new Request(Request.Type.DELETE,String.valueOf(funko.getId()),token,LocalDateTime.now().toString());
        logger.debug("Request Send: " + request);

        out.println(gson.toJson(request));

        Response response = gson.fromJson(in.readLine(),new TypeToken<Response>(){}.getType());
        logger.debug("Response Received Type: " + response.status());

        switch (response.status()) {
            case OK -> {
                Funko deleted = gson.fromJson(response.content(),new TypeToken<Funko>(){}.getType());
                  logger.info("🟢 El Funko fue eliminado con exito: " + deleted);
            }
            case ERROR -> logger.error("🔴 Error: " + response.content());
            default -> throw new ClientException("Unexpected response status: " + response.status());
        }
    }
    
    public void setUser(String username){
        this.username = username;
    }
    
    public void setPassword(String password){
        this.password = password;
    }
}

```
## MAIN
***
### Paso 31: Creamos la clase ClientHandler.
Este es un manejador de cliente (Client Handler) en Java para la aplicación de servidor de Funkos. Este manejador atiende a las solicitudes de los clientes que se conectan al servidor y procesa estas solicitudes de acuerdo con las operaciones permitidas.
```java
package org.develop.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.develop.commons.model.mainUse.Funko;
import org.develop.commons.model.mainUse.Modelo;
import org.develop.commons.model.serverUse.Login;
import org.develop.commons.model.serverUse.Request;
import org.develop.commons.model.serverUse.Response;
import org.develop.commons.model.serverUse.User;
import org.develop.commons.utils.adapters.LocalDateAdapter;
import org.develop.commons.utils.adapters.LocalDateTimeAdapter;
import org.develop.exceptions.server.ServerException;
import org.develop.repositories.users.UserRepository;
import org.develop.services.funkos.FunkoService;
import org.develop.services.token.TokenService;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ClientHandler extends Thread{
    private final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private final Socket clientSocket;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
    private final long clientNumber;
    private final FunkoService funkoService;

    BufferedReader in;
    PrintWriter out;

    
    public ClientHandler(Socket socket, long clientNumber, FunkoService funkoService) {
        this.clientSocket = socket;
        this.clientNumber = clientNumber;
        this.funkoService = funkoService;
    }

    @Override
    public void run(){
        try {
            openConnection();

            String clientInput;
            Request request;

            while (true){
                clientInput = in.readLine();
                logger.debug("Request Received: " + clientInput);
                request = gson.fromJson(clientInput,Request.class);
                handleRequest(request);
            }

        } catch (IOException e) {
            logger.error("Error: " + e.getMessage(), e);
        }catch (ServerException ex){
            out.println(gson.toJson(new Response(Response.Status.ERROR,ex.getMessage(),LocalDateTime.now().toString())));
        }
    }

   
    public void openConnection() throws IOException {
        logger.debug("Connectando con el cliente numero: " + clientNumber);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(),true);
    }
    
    public void closeConnection() throws IOException {
        logger.debug("Cerrando la conexion con el cliente numero: " + clientNumber);
        in.close();
        out.close();
        clientSocket.close();
    }
    
    private void handleRequest(Request request) throws ServerException {
        logger.debug("Request Handler: " + request);

        switch (request.type()){
            case LOGIN -> processLogin(request);
            case SALIR -> processSalir();
            case GETALL -> processGetAll(request);
            case GETBYID -> processGetById(request);
            case GETBYMODEL -> processGetByModel(request);
            case GETBYLAUNCHDATE -> processGetByLaunchYear(request);
            case POST -> processPost(request);
            case UPDATE -> processUpdate(request);
            case DELETE -> processDelete(request);
            default -> new Response(Response.Status.ERROR, "Not implemented Request", LocalDateTime.now().toString());
        }
    }
   
    private void processSalir(){
        out.println(gson.toJson(new Response(Response.Status.BYE,"Adios",LocalDateTime.now().toString())));
    }
   
    private void processLogin(Request request) throws ServerException {
        logger.debug("Requested login Recieved: " + request);

        Login login = gson.fromJson(String.valueOf(request.content()),new TypeToken<Login>(){}.getType());

        var user = UserRepository.getInstance().findByUsername(login.username());
        if (user.isEmpty() || !BCrypt.checkpw(login.password(),user.get().password())){
            logger.warn("User not found or wrong password");
            throw  new ServerException("User not found or wrong password");
        }

        var token = TokenService.getInstance().createToken(user.get(),Server.TOKEN_SECRET,Server.TOKEN_EXPIRATION);

        logger.debug("Sending Response: " + token);
        out.println(gson.toJson(new Response(Response.Status.TOKEN,token,LocalDateTime.now().toString())));
    }
  
    private Optional<User> processToken(String token) throws ServerException {
        if (TokenService.getInstance().verifyToken(token,Server.TOKEN_SECRET)){
            logger.debug("Token verified");
            var claims = TokenService.getInstance().getClaims(token,Server.TOKEN_SECRET);
            var id = claims.get("userid").asInt();
            var user = UserRepository.getInstance().findById(id);
            if (user.isEmpty()){
                logger.error("User Wrong Authentication");
                throw new ServerException("User Wrong Authentication");
            }
            return user;
        }else {
            logger.error("Token not verified");
            throw new ServerException("Token not verified");
        }

    }

    private void processGetAll(Request request) throws ServerException {
        processToken(request.token());

        funkoService.findAll()
                .collectList()
                .subscribe(funkos -> {
                    logger.debug("Sending Response: " + funkos);
                    var resJson = gson.toJson(funkos);
                    out.println(gson.toJson(new Response(Response.Status.OK,resJson,LocalDateTime.now().toString())));
                });
    }
    
    private void processGetById(Request request) throws ServerException {
        processToken(request.token());

        var id = Integer.parseInt(request.content());
        funkoService.findById(id)
                .subscribe(
                        funko -> {
                            logger.debug("Sending Response: " + funko);
                            var resJson = gson.toJson(funko);
                            out.println(gson.toJson(new Response(Response.Status.OK,resJson,LocalDateTime.now().toString())));
                        },
                        error -> {
                            logger.error("Error: " + error.getMessage());
                            out.println(gson.toJson(new Response(Response.Status.ERROR,error.getMessage(),LocalDateTime.now().toString())));
                        }
                );
    }
    
    private void processGetByModel(Request request) throws ServerException {
        processToken(request.token());
        Modelo model = Modelo.valueOf(request.content());

        funkoService.findAll()
                .filter(funko -> funko.getModelo().equals(model))
                .collectList()
                .subscribe(
                        funkos -> {
                            logger.debug("Sending Response: " + funkos);
                            var resJson = gson.toJson(funkos);
                            out.println(gson.toJson(new Response(Response.Status.OK,resJson,LocalDateTime.now().toString())));
                        },
                        error ->{
                            logger.error("Error: " + error.getMessage());
                            out.println(gson.toJson(new Response(Response.Status.ERROR,error.getMessage(),LocalDateTime.now().toString())));
                        }
                );
    }
    
    private void processGetByLaunchYear(Request request) throws ServerException {
        processToken(request.token());
        int launchDate = Integer.parseInt(request.content());

        funkoService.findAll()
                .filter(funko -> funko.getFecha_lanzamiento().getYear() == launchDate)
                .collectList()
                .subscribe(
                        funkos -> {
                            logger.debug("Sending Response: " + funkos);
                            var resJson = gson.toJson(funkos);
                            out.println(gson.toJson(new Response(Response.Status.OK,resJson,LocalDateTime.now().toString())));
                        },
                        error ->{
                            logger.error("Error: " + error.getMessage());
                            out.println(gson.toJson(new Response(Response.Status.ERROR,error.getMessage(),LocalDateTime.now().toString())));
                        }
                );
    }
    
    private void processPost(Request request) throws ServerException {
        var user = processToken(request.token());
        if (user.isPresent() && user.get().role().equals(User.Role.ADMIN)){
            Funko funko = gson.fromJson(String.valueOf(request.content()), new TypeToken<Funko>() {
            }.getType());
            funkoService.save(funko)
                    .subscribe(
                            funkoSave -> {
                                logger.debug("Sending Response: " + funkoSave);
                                var resJson = gson.toJson(funkoSave);
                                out.println(gson.toJson(new Response(Response.Status.OK,resJson,LocalDateTime.now().toString())));
                            },
                            error -> {
                                logger.error("Error: " + error.getMessage());
                                out.println(gson.toJson(new Response(Response.Status.ERROR,error.getMessage(),LocalDateTime.now().toString())));
                            }
                    );
        }else {
            logger.error("User doesn't have the necessary permissions");
            out.println(gson.toJson(new Response(Response.Status.ERROR,"User doesn't have the necessary permissions",LocalDateTime.now().toString())));
        }
    }
    
    private void processUpdate(Request request) throws ServerException {
        var user = processToken(request.token());

        if (user.isPresent() && user.get().role().equals(User.Role.ADMIN)){
            Funko funko = gson.fromJson(String.valueOf(request.content()), new TypeToken<Funko>() {
            }.getType());
            funkoService.update(funko)
                    .subscribe(
                            funkoUpt -> {
                                logger.debug("Sending Response: " + funkoUpt);
                                var resJson = gson.toJson(funkoUpt);
                                out.println(gson.toJson(new Response(Response.Status.OK,resJson,LocalDateTime.now().toString())));
                            },
                            error -> {
                                logger.error("Error: " + error.getMessage());
                                out.println(gson.toJson(new Response(Response.Status.ERROR,error.getMessage(),LocalDateTime.now().toString())));
                            }
                    );
        }else {
            logger.error("User doesn't have the necessary permissions");
            out.println(gson.toJson(new Response(Response.Status.ERROR,"User doesn't have the necessary permissions",LocalDateTime.now().toString())));
        }
    }

    private void processDelete(Request request) throws ServerException {
        var user = processToken(request.token());
        logger.debug("Borrando");
        if (user.isPresent() && user.get().role().equals(User.Role.ADMIN)){
            var id = Integer.parseInt(request.content());

            funkoService.deleteById(id)
                    .subscribe(
                            deleted -> {
                                logger.debug("Sending Response: " + deleted);
                                var resJson = gson.toJson(deleted);
                                out.println(gson.toJson(new Response(Response.Status.OK,resJson,LocalDateTime.now().toString())));
                            },
                            error ->{
                                logger.error("Error: " + error.getMessage());
                                out.println(gson.toJson(new Response(Response.Status.ERROR,error.getMessage(),LocalDateTime.now().toString())));
                            }
                    );
        }else {
            logger.error("User doesn't have the necessary permissions");
            out.println(gson.toJson(new Response(Response.Status.ERROR,"User doesn't have the necessary permissions",LocalDateTime.now().toString())));
        }
    }

}

```
***
### Paso 32: Creamos la clase Server.
El servidor de la aplicación Funko es una parte esencial de la infraestructura de la aplicación. Actúa como el intermediario entre los clientes y la base de datos de Funkos, gestionando las solicitudes de los clientes y proporcionando una capa de seguridad mediante SSL. Además, notifica a los clientes sobre cambios en la base de datos de Funkos.
```java
package org.develop.main;

import org.develop.commons.model.mainUse.MyIDGenerator;
import org.develop.commons.utils.properties.PropertiesReader;
import org.develop.repositories.funkos.FunkoRepositoryImpl;
import org.develop.services.database.DatabaseManager;
import org.develop.services.files.BackupManagerImpl;
import org.develop.services.funkos.FunkoNotificationImpl;
import org.develop.services.funkos.FunkoService;
import org.develop.services.funkos.FunkoServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * El punto de entrada principal del servidor Funko. Inicia el servidor, configura la seguridad SSL, y escucha conexiones entrantes de clientes.
 *
 */
public class Server {
    public static final String TOKEN_SECRET = "TokenSuperUltraSecretoNoLoCuentes";
    public static final long TOKEN_EXPIRATION = 10000;
    private static final AtomicLong clientNumber = new AtomicLong(0);
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final int PUERTO = 3000;
    private static final FunkoServiceImpl funkoService = FunkoServiceImpl.getInstance(FunkoRepositoryImpl.getInstance(DatabaseManager.getInstance(), MyIDGenerator.getInstance()), FunkoNotificationImpl.getInstance(), BackupManagerImpl.getInstance());

    public static void main(String[] args) {
        try {
            funkoService.getNotifications().subscribe(
                notificacion -> {
                    switch (notificacion.getTipo()) {
                        case NEW:
                            logger.info("🟢 Funko insertado: " + notificacion.getContenido());
                            break;
                        case UPDATED:
                            logger.info("🟠 Funko actualizado: " + notificacion.getContenido());
                            break;
                        case DELETED:
                            logger.info("🔴 Funko eliminado: " + notificacion.getContenido());
                            break;
                    }
                },
                error -> logger.error("Se ha producido un error: " + error),
                () -> logger.info("Completado")
        );
            var myConfig = readConfigFile();

            logger.debug("Configurando TSL");

            System.setProperty("javax.net.ssl.keyStore", myConfig.get("keyFile"));
            System.setProperty("javax.net.ssl.keyStorePassword", myConfig.get("keyPassword"));

            SSLServerSocketFactory serverFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket serverSocket = (SSLServerSocket) serverFactory.createServerSocket(PUERTO);


            funkoService.imported("funkos.csv").subscribe(fk -> {
                funkoService.save(fk).subscribe();
            });

            logger.debug("🚀 Servidor escuchando en el puerto 3000");

            while (true){
                new ClientHandler(serverSocket.accept(),clientNumber.incrementAndGet(),funkoService).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Lee la configuracion del servidor desde un archivo de propiedades y la devuelve en forma de mapa de cadenas.
     *
     * @return Un mapa que contiene la configuracion leida con las siguientes claves:
     *   - "keyFile": Ruta al archivo de clave utilizado para SSL.
     *   - "keyPassword": Contrasena asociada al archivo de clave.
     *   - "tokenSecret": Clave secreta para firmar y verificar tokens de autenticacion.
     *   - "tokenExpiration": Duracion de validez de los tokens de autenticacion en milisegundos.
     *
     * @throws IllegalStateException Si falta la ruta del archivo de clave o la contrasena en la configuracion.
     * @throws FileNotFoundException Si el archivo de clave no se encuentra en la ruta especificada.
     */
    public static Map<String,String> readConfigFile(){
        try{
            logger.debug("Reading config file");
            PropertiesReader propertiesReader = new PropertiesReader("server.properties");

            String keyFile= propertiesReader.getProperty("keyFile");
            String keyPassword = propertiesReader.getProperty("keyPassword");
            String tokenSecret = propertiesReader.getProperty("tokenSecret");
            String tokenExpiration = propertiesReader.getProperty("tokenExpiration");

            if (keyFile.isEmpty() || keyPassword.isEmpty()){
                throw new IllegalStateException("Missing keyFile or keyPassword");
            }

            if (!Files.exists(Path.of(keyFile))){
                throw new FileNotFoundException("No se encuentra el fichero " + keyFile);
            }

            Map<String,String> configMap = new HashMap<>();
            configMap.put("keyFile", keyFile);
            configMap.put("keyPassword", keyPassword);
            configMap.put("tokenSecret", tokenSecret);
            configMap.put("tokenExpiration", tokenExpiration);

            return configMap;
        }catch (FileNotFoundException e){
            logger.error("Error en clave: " + e.getLocalizedMessage());
            System.exit(1);
            return null;
        }
        catch (IOException e) {
          logger.error("Error al leer el fichero de propiedades: " + e.getLocalizedMessage());
            return null;
        }
    }
}
```
y main 
```java
package org.develop;

import org.develop.client.Client;

import java.io.IOException;
import java.util.ArrayList;

public class ClientLauncher {

    private ArrayList<Client> clients = new ArrayList<>();

   
    public ClientLauncher(){
        for (int i = 0; i < 10; i++) {
            clients.add(new Client());
        }
    }
    
    public static void main(String[] args) throws IOException {
        ClientLauncher launcher = new ClientLauncher();
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                launcher.clients.get(i).setUser("pepe");
                launcher.clients.get(i).setPassword("pepe1234");
            }else{
                launcher.clients.get(i).setUser("ana");
                launcher.clients.get(i).setPassword("ana1234");
            }
            launcher.clients.get(i).start();
        }
    }
}

```


