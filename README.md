# Zoomalyzer
Record and perform speaker diarization on Zoom calls.

Robot, that joins zoom meeting and starts recording and analysing:
- meeting sound and video
- participants talking time (Mart 243s, Piret 12s)
- the time of the participants "speech order transition" and from whom it was transferred (04:03 Mart → Piret)


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


### Analyzer Python script
Navigate to /analyzer directory. Create virtual env for Python:
`python -m venv venv`

Install pyannote.audio 3.1 with `pip install pyannote.audio`

Install pytorch with `pip3 install torch torchvision torchaudio --index-url https://download.pytorch.org/whl/cu118`



### Spring setup
Configure properties file then run Run scheduler/src/main/java/../ZoomalyzerApplication.java.

Below is the content of properties file. Add full path to project as `recorder.root-path`.

Generate new project on www.mailtrap.io and add credentials for the email service to `mailtrap.username&password`

Create access token at [hf.co/settings/tokens](https://huggingface.co/settings/tokens).
Add your access token to `recorder.token`

Accept [pyannote/segmentation-3.0](https://huggingface.co/pyannote/segmentation-3.0) user conditions

Accept [pyannote/speaker-diarization-3.1](https://huggingface.co/pyannote/speaker-diarization-3.1) user conditions


#### scheduler/src/main/resources/application.properties file contents
````
# CONFIGURE THIS

recorder.token={access_token_from_hf.co}
# e.g recorder.token=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
recorder.root-path={full_path_to_project}
# e.g. recorder.root-path=C:/Users/paulb/Projects/School/zoom-analyzer

# Uncomment the line below for mailtrap.io live version
#mail.smtp.ssl.trust=smtp.mailtrap.io

mailtrap.username={mailtrap.io generated username}
mailtrap.password={mailtrap.io generated password}
# e.g. mailtrap.username=d15b687d8effcb
# e.g. mailtrap.password=4955a70c4cc22c

# IGNORE BELOW

# Database connection properties
spring.datasource.url=jdbc:mysql://localhost:3306/zoomalyzer
spring.datasource.username=root
spring.datasource.password=test

# Hibernate dialect for MySQL
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

recorder.image=zoomalyzer-recorder
# max minutes for startup -> recording
recorder.max-joining-time=15
#in seconds, period to check recorder status
recorder.status-check-period=20
recorder.file-type=.mkv

frontend.recording.url=http://localhost:3000/recordings/recording/

mail.smtp.auth=true
mail.smtp.starttls.enable=true
mail.smtp.host=sandbox.smtp.mailtrap.io
mail.smtp.port=2525
````