
# Getting Started
The application is used for creating, updating, and getting todo items.
You use a rest endpoint for that. 

### Automatically changing DueDate
There are many approaches:
- In GET endpoints it can be checked and if it was past, the entity can be updated. But changing data in GET method, is an anti-pattern
- A scheduled job can check and update bulk. But in this case, user can see some out of date data
- I implemented a hybrid solution. Once an item is created, a task is scheduled to run on Due Date of item. At the same time, for preventing to use a huge amount of the scheduled task, a cron runs at regular intervals and updates items with due date passed.


UUID has been used as an entity id due to prevent possible IDOR security vulnerabilities.

## Possible improvements
- The lastModifiedDate column can be added for logging,  lastModifiedBy and createdBy fields can be also added an authentication supported system.
- Only the id of the saving entity is returned from POST method. The whole object can be returned instead of that. It depends on context.
- A simple method can be written for filtering. An advanced filtering feature can be implemented. Ex:
```java
@PostMapping("/items-by-filter")
public ResponseEntity<List<ItemDto>> filterItems(@RequestBody ItemFilter filter) {
    
}

protected Specification<ItemEntity> createSpecs(ItemFilter filter) {
  Specification<ItemEntity> specs = where(null);
  if (filter.getDescription() != null) {
      specs = specs.and(description(filter.getDescription()));
  }
  ...
```
- Pagination can be added on bulk data fetching

## Assumptions
- By default item status is UNDONE
- If status is DONE, completed-date should be filled otherwise it should be null

## Known issues
The quartz jobs that are scheduled separately for each item don't work. And after fixing it, it needs to be tested.

## Requirements

For building and running the application you need:

- [JDK 17](https://www.oracle.com/java/technologies/downloads/#java17)
- [Maven 3](https://maven.apache.org)
- [Docker](https://www.docker.com/)

## Running the application locally

There are several ways to run a Spring Boot application on your local machine. One way is to execute the `main` method in the ` com.task.todo.TodoApplication` class from your IDE.

Alternatively you can use the [Spring Boot Maven plugin](https://docs.spring.io/spring-boot/docs/current/reference/html/build-tool-plugins-maven-plugin.html) like so:

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
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)