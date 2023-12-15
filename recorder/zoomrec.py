import logging
import os

import psutil
import pyautogui
import signal
import subprocess
import threading
import time
import atexit
import requests
from datetime import datetime, timedelta

global ONGOING_MEETING
global SCREENSHARING_SPEAKER_VIEW

# Turn DEBUG on:
#   - screenshot on error
#   - record joining
#   - do not exit container on error
DEBUG = True if os.getenv('DEBUG') == 'True' else False

# Disable failsafe
pyautogui.FAILSAFE = False

# Get vars
BASE_PATH = os.getenv('HOME')
IMG_PATH = os.path.join(BASE_PATH, "img")
REC_PATH = os.path.join(BASE_PATH, "recordings")
DEBUG_PATH = os.path.join(REC_PATH, "screenshots")

MEETING_ID = os.getenv('MEETING_ID')
MEETING_PASSWORD = os.getenv('MEETING_PASSWORD')
MEETING_DURATION = os.getenv('MEETING_DURATION') # in minutes

RECORDING_ID = os.getenv('RECORDING_ID', -1)
SCHEDULER_LOGGING_URL = os.getenv("SCHEDULER_LOGGING_URL", "http://host.docker.internal:8080/log/recorder")

DISPLAY_NAME = os.getenv('DISPLAY_NAME', "Recording bot on behalf of participant")
MAX_CONNECTING_DURATION = os.getenv('MAX_CONNECTING_DURATION', 15)

#TODO change timezone
# Configure logging to scheduler
class SchedulerLogHandler(logging.Handler):
    def emit(self, record):
        log_entry = self.format(record)
        if record.levelname != "CRITICAL":
            body = {"recordingId": int(RECORDING_ID), "logLevel": record.levelname, "message": record.message}
            try:
                requests.post(SCHEDULER_LOGGING_URL, json=body)
            except BaseException as e:
                logging.critical(e)

# quick and dirty fix
# TODO implement a proper fix

def _locateCenterOnScreen(*args, **kwargs):
    try:
        return pyautogui.locateCenterOnScreen(*args, **kwargs)
    except:
        return None


logging.basicConfig(
    format='%(asctime)s %(levelname)s %(message)s', level=logging.INFO,
    filename=os.path.join(DEBUG_PATH, "recording-") + str(RECORDING_ID) + ".log", filemode='w')
log_handler = SchedulerLogHandler()
logging.getLogger().addHandler(log_handler)
STATUS_LEVEL = 51
logging.addLevelName(STATUS_LEVEL, "STATUS")



TIME_FORMAT = "%Y-%m-%d_%H-%M-%S"
CSV_DELIMITER = ';'

ONGOING_MEETING = False
SCREENSHARING_SPEAKER_VIEW = False


class BackgroundThread:

    def __init__(self, interval=10):
        # Sleep interval between
        self.interval = interval

        thread = threading.Thread(target=self.run, args=())
        thread.daemon = True  # Daemonize thread
        thread.start()  # Start the execution

    def run(self):
        global ONGOING_MEETING
        ONGOING_MEETING = True

        logging.debug("Check continuously if meeting has ended..")

        while ONGOING_MEETING:

            # Check if recording
            if (_locateCenterOnScreen(os.path.join(IMG_PATH, 'meeting_is_being_recorded.png'), confidence=0.9,
                                               minSearchTime=2) is not None):
                logging.info("This meeting is being recorded by other participants")
                try:
                    x, y = _locateCenterOnScreen(os.path.join(
                        IMG_PATH, 'got_it.png'), confidence=0.9)
                    pyautogui.click(x, y)
                    logging.info("Accepted recording..")
                except TypeError:
                    logging.error("Could not accept 'this meeting is being recorded'!")

            # Check if ended
            if (_locateCenterOnScreen(os.path.join(IMG_PATH, 'meeting_ended_by_host_1.png'),
                                         confidence=0.9) is not None or _locateCenterOnScreen(
                os.path.join(IMG_PATH, 'meeting_ended_by_host_2.png'), confidence=0.9) is not None):
                ONGOING_MEETING = False
                logging.info("Meeting ended by host")
            time.sleep(self.interval)


class HideViewOptionsThread:

    def __init__(self, interval=10):
        # Sleep interval between
        self.interval = interval

        thread = threading.Thread(target=self.run, args=())
        thread.daemon = True  # Daemonize thread
        thread.start()  # Start the execution

    def run(self):
        global SCREENSHARING_SPEAKER_VIEW
        logging.debug("Check continuously if screensharing is active..")
        while ONGOING_MEETING:
            # TODO check if host asked to unmute
            # TODO check if host kicked / put back to waiting room
            # TODO sharing screen is not always fullscreen

            # popup close
            if _locateCenterOnScreen(os.path.join(
                    IMG_PATH, 'popup2_close.png'), confidence=0.9) is not None:
                try:
                    x, y = _locateCenterOnScreen(os.path.join(
                        IMG_PATH, 'popup2_close.png'), confidence=0.9)
                    pyautogui.click(x, y)
                except TypeError:
                    logging.debug("Could not close enhanced multi popup")

            # Check if host is sharing poll results
            if (_locateCenterOnScreen(os.path.join(IMG_PATH, 'host_is_sharing_poll_results.png'),
                                               confidence=0.9,
                                               minSearchTime=2) is not None):
                logging.info("Host is sharing poll results..")
                try:
                    x, y = _locateCenterOnScreen(os.path.join(
                        IMG_PATH, 'host_is_sharing_poll_results.png'), confidence=0.9)
                    pyautogui.click(x, y)
                    try:
                        x, y = _locateCenterOnScreen(os.path.join(
                            IMG_PATH, 'exit.png'), confidence=0.9)
                        pyautogui.click(x, y)
                        logging.info("Closed poll results window..")
                    except TypeError:
                        logging.error("Could not exit poll results window!")
                        if DEBUG:
                            pyautogui.screenshot(os.path.join(DEBUG_PATH, time.strftime(
                                TIME_FORMAT) + "-") + "_close_poll_results_error.png")
                except TypeError:
                    logging.error("Could not find poll results window anymore!")
                    if DEBUG:
                        pyautogui.screenshot(os.path.join(DEBUG_PATH, time.strftime(
                            TIME_FORMAT) + "-") + "_find_poll_results_error.png")

            # Check if screensharing
            if _locateCenterOnScreen(os.path.join(IMG_PATH, 'view_options.png'), confidence=0.9) is not None:
                if not SCREENSHARING_SPEAKER_VIEW:
                    logging.info("Screensharing active..")
                    if _locateCenterOnScreen(os.path.join(IMG_PATH, 'side_by_side_separator.png'), confidence=0.9) is None:
                        logging.info("Changing screensharing view")
                        show_toolbars()
                        try:
                            x, y = _locateCenterOnScreen(os.path.join(IMG_PATH, 'view.png'), confidence=0.9)
                            pyautogui.click(x, y)
                            time.sleep(2)
                            # Set side by side: Speaker view during screensharing
                            pyautogui.press('down')
                            pyautogui.press('down')
                            pyautogui.press('enter')
                            logging.info("Set view to side-by-side speaker")
                            SCREENSHARING_SPEAKER_VIEW = True
                        except TypeError:
                            logging.error("Could not find view options!")
                    else:
                        SCREENSHARING_SPEAKER_VIEW = True
            else:
                SCREENSHARING_SPEAKER_VIEW = False
                # Set fit to screen
                if (_locateCenterOnScreen(os.path.join(IMG_PATH, 'zoom_window_title.png'), confidence=0.9) is not None
                        and
                        _locateCenterOnScreen(os.path.join(IMG_PATH, 'zoom_exit_btn.png'), confidence=0.9) is None
                ):
                    logging.info("Window is not fit to screen, resizing")
                    pyautogui.keyDown('alt')
                    pyautogui.press('f10')
                    pyautogui.keyUp('alt')

            time.sleep(self.interval)
       
def check_connecting(zoom_pid, start_date, duration):
    # Check if connecting
    check_periods = 0
    connecting = False
    # Check if connecting
    if _locateCenterOnScreen(os.path.join(IMG_PATH, 'connecting.png'), confidence=0.9) is not None:
        connecting = True
        logging.info("Connecting..")

    # Wait while connecting
    # Exit when connecting takes more time than allowed
    while connecting:
        if (datetime.now() - start_date).total_seconds() > duration:
            logging.info("Connecting to meeting has taken more time than " + str(duration / 60) + " minutes")
            logging.info("Exit Zoom!")
            os.killpg(os.getpgid(zoom_pid), signal.SIGQUIT)
            return

        if _locateCenterOnScreen(os.path.join(IMG_PATH, 'connecting.png'), confidence=0.9) is None:
            logging.info("Possibly not connecting anymore")
            check_periods += 1
            if check_periods >= 2:
                connecting = False
                logging.info("Not connecting anymore")
                return
        time.sleep(2)


def join_meeting_id(meet_id):
    logging.info("Join a meeting by ID")
    found_join_meeting = False
    try:
        x, y = _locateCenterOnScreen(os.path.join(
            IMG_PATH, 'join_meeting.png'), minSearchTime=2, confidence=0.9)
        pyautogui.click(x, y)
        found_join_meeting = True
    except TypeError:
        pass

    if not found_join_meeting:
        logging.error("Could not find 'Join Meeting' on screen!")
        return False

    time.sleep(2)

    # Insert meeting id
    pyautogui.press('tab')
    pyautogui.press('tab')
    pyautogui.write(meet_id, interval=0.1)

    # Insert name
    pyautogui.press('tab')
    pyautogui.press('tab')
    pyautogui.hotkey('ctrl', 'a')
    pyautogui.write(DISPLAY_NAME, interval=0.1)

    # Configure
    pyautogui.press('tab')
    pyautogui.press('space')
    pyautogui.press('tab')
    pyautogui.press('tab')
    pyautogui.press('space')
    pyautogui.press('tab')
    pyautogui.press('tab')
    pyautogui.press('space')

    time.sleep(2)

    return check_error()


def join_meeting_url():
    logging.info("Join a meeting by URL")

    # Insert name
    pyautogui.hotkey('ctrl', 'a')
    pyautogui.write(DISPLAY_NAME, interval=0.1)

    # Configure
    pyautogui.press('tab')
    pyautogui.press('space')
    pyautogui.press('tab')
    pyautogui.press('space')
    pyautogui.press('tab')
    pyautogui.press('space')

    time.sleep(2)

    return check_error()
    

def check_error():
    # Sometimes invalid id error is displayed
    if _locateCenterOnScreen(os.path.join(
            IMG_PATH, 'invalid_meeting_id.png'), confidence=0.9) is not None:
        logging.error("Invalid meeting id window popped up")
        left = False
        try:
            x, y = _locateCenterOnScreen(
                os.path.join(IMG_PATH, 'leave.png'), confidence=0.9)
            pyautogui.click(x, y)
            left = True
        except TypeError:
            logging.debug("Could not press 'leave' on invalid meeting id popup, maybe valid id")
            pass
            # Valid id

        if left:
            if _locateCenterOnScreen(os.path.join(
                    IMG_PATH, 'join_meeting.png'), confidence=0.9) is not None:
                logging.error("Invalid meeting id!")
                logging.log(STATUS_LEVEL, "FAILED")
                return False
        else:
            return True

    if _locateCenterOnScreen(os.path.join(
            IMG_PATH, 'authorized_attendees_only.png'), confidence=0.9) is not None:
        logging.error("This meeting is for authorized attendees only!")
        logging.log(STATUS_LEVEL, "FAILED")
        return False

    return True


def find_process_id_by_name(process_name):
    list_of_process_objects = []
    # Iterate over the all the running process
    for proc in psutil.process_iter():
        try:
            pinfo = proc.as_dict(attrs=['pid', 'name'])
            # Check if process name contains the given name string.
            if process_name.lower() in pinfo['name'].lower():
                list_of_process_objects.append(pinfo)
        except (psutil.NoSuchProcess, psutil.AccessDenied, psutil.ZombieProcess):
            pass
    return list_of_process_objects


def show_toolbars():
    # Mouse move to show toolbar
    width, height = pyautogui.size()
    y = (height / 2)
    pyautogui.moveTo(width + 1, y + 1, duration=0.5)
    pyautogui.moveTo(width - 1, y - 1, duration=0.5)


def join_audio():
    audio_joined = False
    try:
        x, y = _locateCenterOnScreen(os.path.join(
            IMG_PATH, 'join_with_computer_audio.png'), confidence=0.9)
        logging.info("Join with computer audio")
        pyautogui.click(x, y)
        audio_joined = True
        return True
    except TypeError:
        logging.error("Could not join with computer audio!")
        if DEBUG:
            pyautogui.screenshot(os.path.join(DEBUG_PATH, time.strftime(
                TIME_FORMAT)) + "_join_with_computer_audio_error.png")
    time.sleep(1)
    if not audio_joined:
        try:
            show_toolbars()
            x, y = _locateCenterOnScreen(os.path.join(
                IMG_PATH, 'join_audio.png'), confidence=0.9)
            pyautogui.click(x, y)
            join_audio()
        except TypeError:
            logging.error("Could not join audio!")
            logging.log(STATUS_LEVEL, "FAILED")
            if DEBUG:
                pyautogui.screenshot(os.path.join(DEBUG_PATH, time.strftime(
                   TIME_FORMAT)) + "_join_audio_error.png")
            return False


def join(meet_id, meet_pw, duration):
    global SCREENSHARING_SPEAKER_VIEW
    global MAX_CONNECTING_DURATION
    MAX_CONNECTING_DURATION = int(MAX_CONNECTING_DURATION) * 60  # seconds
    ffmpeg_debug = None

    logging.log(STATUS_LEVEL, "JOINING")

    if DEBUG:
        # Start recording
        width, height = pyautogui.size()
        resolution = str(width) + 'x' + str(height)
        disp = os.getenv('DISPLAY')

        logging.debug("Start recording joining process")

        filename = os.path.join(
            REC_PATH, "recording-") + RECORDING_ID + "-join.mkv"

        command = "ffmpeg -nostats -loglevel quiet -f pulse -ac 2 -i 1 -f x11grab -r 30 -s " + resolution + " -i " + \
                  disp + " -acodec pcm_s16le -vcodec libx264rgb -preset ultrafast -crf 0 -threads 0 -async 1 -vsync 1 " + filename

        ffmpeg_debug = subprocess.Popen(
            command, stdout=subprocess.PIPE, shell=True, preexec_fn=os.setsid)
        atexit.register(os.killpg, os.getpgid(
            ffmpeg_debug.pid), signal.SIGQUIT)

    # Exit Zoom if running
    exit_process_by_name("zoom")

    join_by_url = meet_id.startswith('https://') or meet_id.startswith('http://')

    env = os.environ.copy()
    del env["LD_LIBRARY_PATH"]
    del env["QT_QPA_PLATFORM_PLUGIN_PATH"]

    if not join_by_url:
        # Start Zoom
        zoom = subprocess.Popen("zoom", stdout=subprocess.PIPE,
                                shell=True, preexec_fn=os.setsid, env=env)
        logging.info("Opened zoom")
        img_name = 'join_meeting.png'
    else:
        logging.info("Starting Zoom with url")
        zoom = subprocess.Popen(f'zoom --url="{meet_id}"', stdout=subprocess.PIPE,
                                shell=True, preexec_fn=os.setsid, env=env)
        img_name = 'join.png'
    
    # Wait while zoom process is there
    list_of_process_ids = find_process_id_by_name('zoom')
    tries_left = 50
    while len(list_of_process_ids) <= 0 and tries_left > 0:
        logging.debug("No Running Zoom Process found!")
        list_of_process_ids = find_process_id_by_name('zoom')
        tries_left -= 1
        time.sleep(1)

    if tries_left == 0:
        logging.error("Could not start Zoom, Zoom process not found")
        logging.log(STATUS_LEVEL, "FAILED")
        os.killpg(os.getpgid(zoom.pid), signal.SIGQUIT)
        return

    # Wait for Zoom to start up
    tries_left = 50
    while _locateCenterOnScreen(os.path.join(IMG_PATH, img_name), confidence=0.9) is None and tries_left > 0:
        check_error()
        logging.debug("Zoom not ready yet!")
        tries_left -= 1
        time.sleep(1)

    if tries_left == 0:
        logging.error("Could not start Zoom, join button not found")
        logging.log(STATUS_LEVEL, "FAILED")
        os.killpg(os.getpgid(zoom.pid), signal.SIGQUIT)
        return

    logging.info("Zoom started!")
    start_date = datetime.now()

    if not join_by_url:
        joined = join_meeting_id(meet_id)
    else:
        time.sleep(2)
        joined = join_meeting_url()

    if not joined:
        logging.error("Failed to join meeting!")
        logging.log(STATUS_LEVEL, "FAILED")
        os.killpg(os.getpgid(zoom.pid), signal.SIGQUIT)
        if DEBUG and ffmpeg_debug is not None:
            # closing ffmpeg
            os.killpg(os.getpgid(ffmpeg_debug.pid), signal.SIGQUIT)
            atexit.unregister(os.killpg)
        return

    # Check if connecting
    check_connecting(zoom.pid, start_date, MAX_CONNECTING_DURATION)

    if not join_by_url:
        pyautogui.write(meet_pw, interval=0.2)
        pyautogui.press('tab')
        pyautogui.press('space')

    # Joined meeting
    # Check if connecting
    check_connecting(zoom.pid, start_date, MAX_CONNECTING_DURATION)

    # Check if meeting is started by host
    check_periods = 0
    meeting_started = True

    time.sleep(2)

    # Check if waiting for host
    if _locateCenterOnScreen(os.path.join(
            IMG_PATH, 'wait_for_host.png'), confidence=0.9, minSearchTime=3) is not None:
        meeting_started = False
        logging.info("Waiting for the host to start this meeting")

    # Wait for the host to start this meeting
    # Exit when meeting ends after time
    while not meeting_started:
        if (datetime.now() - start_date).total_seconds() > MAX_CONNECTING_DURATION:
            logging.error("Meeting hasn't started after " + str(MAX_CONNECTING_DURATION / 60)  +" minutes")
            logging.error("Exit Zoom!")
            logging.log(STATUS_LEVEL, "FAILED")
            os.killpg(os.getpgid(zoom.pid), signal.SIGQUIT)
            if DEBUG:
                os.killpg(os.getpgid(ffmpeg_debug.pid), signal.SIGQUIT)
                atexit.unregister(os.killpg)
            return

        if _locateCenterOnScreen(os.path.join(
                IMG_PATH, 'wait_for_host.png'), confidence=0.9) is None:
            logging.info("Maybe meeting was started by host.")
            check_periods += 1
            if check_periods >= 2:
                meeting_started = True
                logging.info("Meeting started by host.")
                break
        time.sleep(2)

    # Check if connecting
    check_connecting(zoom.pid, start_date, MAX_CONNECTING_DURATION)

    # Check if in waiting room
    check_periods = 0
    in_waitingroom = False

    time.sleep(2)

    # Check if joined into waiting room
    if _locateCenterOnScreen(os.path.join(IMG_PATH, 'waiting_room.png'), confidence=0.9,
                                      minSearchTime=3) is not None:
        in_waitingroom = True
        logging.info("Waiting for the host to admit bot to meeting")
    # Wait while host will let you in
    # Exit when connecting time is too long
    while in_waitingroom:
        if (datetime.now() - start_date).total_seconds() > MAX_CONNECTING_DURATION:
            logging.error("Host did not admit the bot to meeting in 10 minutes")
            logging.error("Exit Zoom!")
            logging.log(STATUS_LEVEL, "FAILED")
            os.killpg(os.getpgid(zoom.pid), signal.SIGQUIT)
            if DEBUG:
                os.killpg(os.getpgid(ffmpeg_debug.pid), signal.SIGQUIT)
                atexit.unregister(os.killpg)
            return

        if _locateCenterOnScreen(os.path.join(
                IMG_PATH, 'waiting_room.png'), confidence=0.9) is None:
            logging.info("Maybe no longer in the waiting room..")
            check_periods += 1
            if check_periods == 2:
                logging.info("No longer in the waiting room..")
                break
        time.sleep(2)

    # Meeting joined
    # Check if connecting
    check_connecting(zoom.pid, start_date, MAX_CONNECTING_DURATION)

    logging.info("Joined meeting")

    # Check if recording warning is shown at the beginning
    if (_locateCenterOnScreen(os.path.join(IMG_PATH, 'meeting_is_being_recorded.png'), confidence=0.9,
                                       minSearchTime=2) is not None):
        logging.info("This meeting is being recorded by other participants")
        try:
            x, y = _locateCenterOnScreen(os.path.join(
                IMG_PATH, 'got_it.png'), confidence=0.9)
            pyautogui.click(x, y)
            logging.info("Accepted recording")
            time.sleep(1)
        except TypeError:
            logging.error("Could not accept recording!")


    # Start BackgroundThread
    BackgroundThread()

    # Set computer audio
    if not join_audio():
        logging.info("Audio not joined, trying again from the beginning!")
        os.killpg(os.getpgid(zoom.pid), signal.SIGQUIT)
        if DEBUG:
            os.killpg(os.getpgid(ffmpeg_debug.pid), signal.SIGQUIT)
            atexit.unregister(os.killpg)
        time.sleep(1)
        join(meet_id, meet_pw, duration)
    time.sleep(1)


    logging.info("Switch to speaker view")
    try:
        show_toolbars()
        x, y = _locateCenterOnScreen(
        os.path.join(IMG_PATH, 'view.png'), confidence=0.9)
        pyautogui.click(x, y)
    except TypeError:
        logging.error("Could not find view button!")
        if DEBUG:
            pyautogui.screenshot(os.path.join(DEBUG_PATH, time.strftime(
                TIME_FORMAT)) + "_view_error.png")

    time.sleep(1)

    try:
        # speaker view
        x, y = _locateCenterOnScreen(os.path.join(
            IMG_PATH, 'speaker_view.png'), confidence=0.9)
        pyautogui.click(x, y)
    except TypeError:
        logging.debug("Could not find speaker view button, trying side-by-side: speaker view")
        try:
            x, y = _locateCenterOnScreen(os.path.join(
                IMG_PATH, 'side_by_side_speaker.png'), confidence=0.9)
            pyautogui.click(x, y)
        except TypeError:
            logging.error("Could not change to speaker nor side-by-side speaker view")
            if DEBUG:
                pyautogui.screenshot(os.path.join(DEBUG_PATH, time.strftime(
                    TIME_FORMAT)) + "_speaker_view_error.png")

    time.sleep(1)
    try:
        # speaker view
        x, y = _locateCenterOnScreen(os.path.join(
            IMG_PATH, 'popup1_close.png'), confidence=0.9)
        pyautogui.click(x, y)
    except TypeError:
        logging.debug("Could not close whiteboard popup")

    # Move mouse from screen
    pyautogui.moveTo(0, 100)
    pyautogui.click(0, 100)

    if DEBUG and ffmpeg_debug is not None:
        os.killpg(os.getpgid(ffmpeg_debug.pid), signal.SIGQUIT)
        atexit.unregister(os.killpg)

    # Audio
    # Start recording
    logging.info("Start recording. Duration " + str(duration/60) + " minutes")
    logging.log(STATUS_LEVEL, "RECORDING")

    filename = os.path.join(REC_PATH, "recording-") + RECORDING_ID + ".mkv"

    width, height = pyautogui.size()
    resolution = str(width) + 'x' + str(height)
    disp = os.getenv('DISPLAY')

    command = "ffmpeg -nostats -loglevel error -f pulse -ac 2 -i 1 -f x11grab -r 30 -s " + resolution + " -i " + \
              disp + " -acodec pcm_s16le -vcodec libx264rgb -preset ultrafast -crf 0 -threads 0 -async 1 -vsync 1 " + filename

    ffmpeg = subprocess.Popen(
        command, stdout=subprocess.PIPE, shell=True, preexec_fn=os.setsid)

    atexit.register(os.killpg, os.getpgid(
        ffmpeg.pid), signal.SIGQUIT)

    start_date = datetime.now()
    end_date = start_date + timedelta(seconds=duration + 30)  # Add 30s for buffer
    # Start thread to check active screensharing
    HideViewOptionsThread()
    
    meeting_running = True
    while meeting_running:
        time_remaining = end_date - datetime.now()
        if time_remaining.total_seconds() < 0 or not ONGOING_MEETING:
            if ONGOING_MEETING:
                logging.info(str(duration /60) + " minutes recorded, ending")
            meeting_running = False
        else:
            print(f"Meeting ends in {time_remaining}", end="\r", flush=True)
        time.sleep(5)

    logging.info("Meeting ended")
    logging.debug("Shutting down container")
    logging.log(STATUS_LEVEL, "ENDED")


    # Close everything
    if DEBUG and ffmpeg_debug is not None:
        os.killpg(os.getpgid(ffmpeg_debug.pid), signal.SIGQUIT)
        atexit.unregister(os.killpg)

    os.killpg(os.getpgid(zoom.pid), signal.SIGQUIT)
    os.killpg(os.getpgid(ffmpeg.pid), signal.SIGQUIT)
    atexit.unregister(os.killpg)


def exit_process_by_name(name):
    list_of_process_ids = find_process_id_by_name(name)
    if len(list_of_process_ids) > 0:
        logging.info(name + " process exists | killing..")
        for elem in list_of_process_ids:
            process_id = elem['pid']
            try:
                os.kill(process_id, signal.SIGKILL)
            except Exception as ex:
                logging.error("Could not terminate " + name +
                              "[" + str(process_id) + "]: " + str(ex))


def main():
    logging.log(STATUS_LEVEL, "STARTUP")
    try:
        if DEBUG and not os.path.exists(DEBUG_PATH):
            os.makedirs(DEBUG_PATH)
    except Exception:
        logging.error("Failed to create screenshot folder!")
        raise

    global MEETING_ID
    global MEETING_PASSWORD
    global MEETING_DURATION
    try:
        join(meet_id=MEETING_ID, meet_pw=MEETING_PASSWORD,
            duration=int(MEETING_DURATION) * 60 + 60)
    except Exception as e:
        logging.error("Process stopped: " + str(e))


if __name__ == '__main__':
    main()
