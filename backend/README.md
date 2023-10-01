# Zoomalyzer BACKEND
Records Zoom calls locally. Analyzes recorded data to track which participants were the most active.

## Setup
Java 21, Docker

#### Database
``
docker pull mysql
``\
``
docker run --name zoomalyzer-db -e MYSQL_ROOT_PASSWORD=test -p 3306:3306 -d mysql
``

Connect to database, run ``
CREATE DATABASE zoomalyzer;
``

#### /resources/appliation.properties file contents
````
spring.datasource.url=jdbc:mysql://localhost:3306/zoomalyzer
javax.persistence.jdbc.url=jdbc:mysql://localhost:3306/zoomalyzer
spring.datasource.username=root
spring.datasource.password=test
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
````




````
docker run -d --restart unless-stopped \
  -v %cd%\recordings:/home/zoomrec/recordings \
  -v %cd%\example\audio:/home/zoomrec/audio \
  -v %cd%\example\meetings.csv:/home/zoomrec/meetings.csv:ro \
  -p 5901:5901 \
  --security-opt seccomp:unconfined \
kastldratza/zoomrec:latest
````
..