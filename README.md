# QAtlas
Open-source test execution reporting and dashboard for QA automation teams. Push results from Jenkins or local runs via REST API or the Java client library, and view pass/fail/in-progress status across executions, suites, and test cases — with screenshots attached at the step level.

Licensed under Apache-2.0.

## Getting Started
### Prerequisites
```
Java 21
Maven 3.9+
```

### Download & run the application
Follow through the below steps to setup the application in your machine.
* Clone the project into your local machine
* Go inside the cloned directory
* Start a MySQL instance (see `docker-compose.yml`), or point `spring.datasource` in `application.yml` at your own
* Change the below property value(s) in "backend/src/main/resources/application.yml"
```
app.data.root-path: <base path location where test step attachments & logs will be stored>
```
* Execute the below command line to build the artifacts in your local
```
mvn clean install 
```
* To run the application execute the below command inside <b>backend</b> folder 
```
mvn spring-boot:run
```
* To stop the application execute the below command in the terminal.
```
Ctrl + c
```
* Access the application at the below URL
```
http://localhost:8080/classic/index.html  -- classic UI
http://localhost:8080/  -- new UI (in progress)
```
* Access the Rest API docs at the below URL
```
http://localhost:8080/swagger-ui.html
```
* Check sample Rest requests in below location
```
backend/src/main/resources/REST_JSON_Input.txt
```
* Add this dependency to your project's POM as below
```
<dependency>
    <groupId>org.qatlas</groupId>
    <artifactId>qatlas-client</artifactId>
    <version>{project.version}</version>
    <scope>compile</scope>
</dependency>
```
* Set the below system property in your project
```
spring.jackson.serialization.write_dates_as_timestamps=false
```
* Below is the sample code snippet to use API client library
```
ApiClient client = new ApiClient();
client.setBasePath("http://localhost:8080");
ApplicationApi api =new ApplicationApi(client);
Application app = new Application();
app.setName("TESTAPP");
app.setDescription("Test Application");
api.createApplication(app);
```

