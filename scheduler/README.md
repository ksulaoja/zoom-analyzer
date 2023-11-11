# Zoomalyzer BACKEND
Records Zoom calls locally. Analyzes recorded data to track which participants were the most active.

## Setup
Java 21, Docker, Recorder docker image (zoomalyzer-recorder)

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

#### Recorder image
Navigate to /recorder, then enter
``
    docker build -t zoomalyzer-recorder .
``

#### /resources/application.properties file contents
````
# Database connection properties
spring.datasource.url=jdbc:mysql://localhost:3306/zoomalyzer
spring.datasource.username=root
spring.datasource.password=test

# Hibernate dialect for MySQL
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

recorder.path=C:/Users/paulb/Projects/School/zoom-analyzer/recorder/recordings
recorder.image=zoomalyzer-recorder
# max minutes for startup -> recording
recorder.max-joining-time=15
#in seconds, period to check recorder status
recorder.status-check-period=20
recorder.file-type=.mkv

frontend.recording.url=http://localhost:3000/recordings/recording/

# email
mail.smtp.auth=true
mail.smtp.starttls.enable=true
mail.smtp.host=sandbox.smtp.mailtrap.io
mail.smtp.port=2525
#mail.smtp.ssl.trust=smtp.mailtrap.io for live version
mailtramp.username=d15b687d8effcb
mailtramp.password=4955a70c4cc22c
````