# Zoom Recorder
Dockerized Ubuntu desktop for recording Zoom calls.


### How to run
#### Build image
Navigate to /recorder in CLI \
Run ``
docker build -t zoomalyzer-recorder .
``


#### Run container
Replace with your own meetingId/url and password. If URL is present, passcode is ignored.
````
docker run -d -e MEETING_ID=https://us05web.zoom.us/j/86068310413?pwd=9tshaIjkM8Xpy52SyOa7n31DLxsLnR.1 -e MEETING_PASSWORD=pass -e MEETING_DURATION=3 -e RECORDING_ID=1 -v %cd%\recordings:/home/zoomrec/recordings -p 5901:5901 -e DEBUG=True --security-opt seccomp:unconfined zoomalyzer-recorder
````


#### Output
Container records meetings to `/recordings` directory



### VNC (remote desktop)
Download VNC client (like RealVNC) and connect to linux desktop for remote access.

| Hostname       | Password |
|----------------|----------|
| localhost:5901 | zoomrec  |


### Mounted volumes
Recording location ``%cd%\recordings:/home/zoomrec/recordings``

### Environment variables
**MEETING_ID** - meeting id or url

**MEETING_PASSWORD** - meeting pw (ignored if URL is present)

**MEETING_DURATION** - recording duration in minutes

**RECORDING_ID** - recording ID

**DISPLAY_NAME** - bot display name during Zoom call, default `Recording bot on behalf of participant`

**SCHEDULER_LOGGING_URL** - endpoint to where bot sends status updates, default `http://host.docker.internal:8080/log/recorder`

**MAX_CONNECTING_DURATION** - max time to connect to meeting (including waiting room) in minutes

**DEBUG** - doesn't stop container if error occurs,
records joining process as separate file, saves screenshots of errors to `/recordings/screenshots`

