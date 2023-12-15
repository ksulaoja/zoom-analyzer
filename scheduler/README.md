# Zoomalyzer
Record and perform speaker diarization on Zoom calls.

## Setup Frontend
Open CMD, navigate to /project/front.
Run
`npm i` and
`npm start`
Open http://localhost:3000



## Setup Backend
Make sure you have Java 21 and Docker installed

### Database
``
docker pull mysql
``\
``
docker run --name zoomalyzer-db -e MYSQL_ROOT_PASSWORD=test -p 3306:3306 -d mysql
``

Connect to database, run ``
CREATE DATABASE zoomalyzer;
``

### Recorder

#### Build base image
Navigate to /recorder/base, then enter
``
    docker build -t up-zoomrec .
``
#### Build second image
Navigate to /recorder, then enter
``
docker build -t zoomalyzer-recorder .
``

## Spring setup

Add properties file then run scheduler/src/main/java/../ZoomalyzerApplication.java

#### scheduler/src/main/resources/application.properties file contents
````
# Database connection properties
spring.datasource.url=jdbc:mysql://localhost:3306/zoomalyzer
spring.datasource.username=root
spring.datasource.password=test

# Hibernate dialect for MySQL
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

recorder.path={full_path_to_project}/recorder/recordings
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