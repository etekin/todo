# Getting Started

The application is used for creating, updating, and getting todo items.
You use a rest endpoint for that.

### Automatically changing DueDate

There are many approaches:

- In GET endpoints it can be checked and if the due date was past, the entity can be updated. But changing data in GET
  method, is an anti-pattern. Anyway this solution can be considered.
- A scheduled job can check and update bulk. But in this case, user can see some out of date data
- A Hybrid solution was implemented. Once an item is created, a task is scheduled to run on Due Date of item. For
  preventing huge increase of the scheduled task the schedulers can be set only a closer date/time(ex. next one day).
  The hybrid solution is a better approach in case of any data sharing situation directly from DB.(ex. a BI, a DWH
  service)
- In case of any synchronization problem between schedulers and DB, Another cron job is set and synchronize them.

UUID has been used as an entity id due to prevent possible IDOR security vulnerabilities.

## Possible improvements

- The lastModifiedDate column can be added for logging, lastModifiedBy and createdBy fields can be also added an
  authentication supported system.
- Only the id of the saving entity is returned from POST method. The whole object can be returned instead of that. It
  depends on context.
- A very simple filtering method was implemented. An advanced filtering feature can be implemented. Ex:

```java
@PostMapping("/items-by-filter")
public ResponseEntity<List<ItemDto>>filterItems(@RequestBody ItemFilter filter){

        }

protected Specification<ItemEntity> createSpecs(ItemFilter filter){
        Specification<ItemEntity> specs=where(null);
        if(filter.getDescription()!=null){
        specs=specs.and(description(filter.getDescription()));
        }
        ...
```

- Pagination can be added on bulk data fetching

## Assumptions

- By default item status is UNDONE
- If status is DONE, completed-date should be filled otherwise it should be null

## TODOs
- Unit tests can be improved

## Requirements

For building and running the application you need:

- [JDK 17](https://www.oracle.com/java/technologies/downloads/#java17)
- [Maven 3](https://maven.apache.org)
- [Docker](https://www.docker.com/)

## Running the application locally

There are several ways to run a Spring Boot application on your local machine. One way is to execute the `main` method
in the ` com.task.todo.TodoApplication` class from your IDE.

Alternatively you can use
the [Spring Boot Maven plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html)
like so:

```shell
mvn spring-boot:run
```

Running tests

```shell
mvn test
```

Running with docker<br/>
Build

```shell
docker build -t todo .
```

Run

```shell
docker run -p 8080:8080 --name todo-container -idt todo
```

### Reference Documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.2.0/maven-plugin/reference/html/)
* [Spring Web](https://docs.spring.io/spring-boot/docs/3.2.0/reference/htmlsingle/index.html#web)
* [Quartz Scheduler](https://docs.spring.io/spring-boot/docs/3.2.0/reference/htmlsingle/index.html#io.quartz)
* [Flyway Migration](https://docs.spring.io/spring-boot/docs/3.2.0/reference/htmlsingle/index.html#howto.data-initialization.migration-tool.flyway)

### Guides

The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)