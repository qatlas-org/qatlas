# QAtlas
This tool stores the test automation results and represents them in a dashboard format. 
## How To Use This Image
### Prerequisites
```
Operating System : Windows/ Linux
Docker and docker-compose is installed ("docker-compose --version")
```
### Integration with UI Framework
* Use the below maven dependency, make sure to use version grater than <b>0.0.21</b>
```xml
<dependency>
    <groupId>org.qatlas</groupId>
    <artifactId>ui-framework</artifactId>
    <version>0.0.21</version>
</dependency>
```
* Adopt the config.properties with the report URL
```
TestReportPath=http://localhost:8888
```
### Prepare Input File
* Save the below code snippet as "docker-compose.yml"
```yml
version: '3.8'
services:
  init:
    container_name: init
    image: busybox
    command: >
      /bin/sh -c "touch /app/.initialized && chown -R 1000:1000 /app && chmod -R 774 /app"
    volumes:
      - app:/app

  mysql_db:
    container_name: mysql
    image: mysql/mysql-server
    environment:
      MYSQL_DATABASE: 'reports_db'
      MYSQL_USER: 'reports_user'
      MYSQL_PASSWORD: 'password'
    restart: always
    healthcheck:
      test: "/usr/bin/mysql --user=reports_user --password=password --execute \"show databases;\""
      interval: 5s
      retries: 5
      timeout: 1s
    volumes:
      - mysql:/var/lib/mysql

  db_admin:
    container_name: mysqldb
    image: adminer
    depends_on:
      - mysql_db
    restart: always
    ports:
      - 9090:8080

  backend:
    container_name: backend
    image: qatlas/backend
    restart: always
    depends_on:
      - mysql_db
    volumes:
      - app:/app
    environment:
      - spring_profiles_active=container
      - app.open-api.servers[0].url=http://localhost:9091
      - app.open-api.servers[0].description=Local Server
      - JAVA_OPTS=-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/data/hdump -Xmx1024m -XX:ReservedCodeCacheSize=32M -XX:MaxDirectMemorySize=10M
    deploy:
      resources:
        limits:
          memory: 3072M

  web:
    container_name: frontend
    image: qatlas/frontend
    restart: always
    depends_on:
      - backend
    ports:
      - 9091:8080
    volumes:
      - app:/app
    environment: 
      BACKEND_SERVICE_NAME: backend
      BACKEND_SERVICE_PORT: 8080
      LISTENING_PORT: 8080

volumes:
  mysql:
  app:
```
### Choose Preferred Port Numbers:
* To host on preferred <b>port</b> numbers update the <b>docker-compose.yml</b>.
* <b>Note</b>: Do not change the right-side port number as they are used in docker container.
```
The “db_admin”: port" 9090 to 7777.
The “web:ports" port 9091 to 8888.
The “app.open-api.servers[0].url”=http://localhost:9091 to same as web:ports.
```
Example:
```yml
  db_admin:
    ports:
      - 7777:8080
  backend:
    environment:
      - app.open-api.servers[0].url=http://localhost:8888
  web:
    ports:
      - 8888:8080
```
### Setup Procedure:
* Choose the drive and folder where the <b>db</b>, <b>attachments</b> and <b>logs</b> to be stored.  
Example:
```
Windows - C:/testreports
Linux - opt/testreports
```
* Create the following folders with read and write access under the <b>testreports</b>  
Example:
```
db/changelog    -   testreports/db/changelog
attachments     -   testreports/attachments
logs            -   testreports/logs
```
* Move the <b>docker-compose.yml</b> file to the <b>testreports</b> folder.  
Example:
```
testreports/docker-compose.yml
```
* Run the command to deploy  
Open the terminal in “testreports” folder.
```shell
$ docker-compose up -d
```
* Access the Application at the below URL
```
http://localhost:9091/classic/index.html
```
* Access the Rest API docs at the below URL
```
http://localhost:9091/swagger/index.html
```
* Access the UI - DB at the below URL
```
http://localhost:9090/mysql?server=mysql&username=reports_user&db=reports_db
Passowrd is in docker-compose.yml
```
