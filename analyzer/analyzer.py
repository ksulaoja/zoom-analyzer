import sys
import time
from pyannote.audio import Pipeline

print("Script started")
AUTH_TOKEN = sys.argv[1]
FILENAME = sys.argv[2]

pipeline = Pipeline.from_pretrained(
    "pyannote/speaker-diarization-3.1",
    use_auth_token=AUTH_TOKEN)

# send pipeline to GPU (when available)
import torch
pipeline.to(torch.device("cuda"))

# apply pretrained pipeline
diarization = pipeline(FILENAME + ".wav")

total_speaker_dict = {}
analysis_time = int(time.time())

f_audio = open(FILENAME + "-1.csv", "x")
f_audio.write("start,end,speaker\n")

for turn, _, speaker in diarization.itertracks(yield_label=True):
    f_audio.write(f"{turn.start:.1f},{turn.end:.1f},{speaker}\n")
    if (speaker in total_speaker_dict):
        total_speaker_dict[speaker] += turn.end - turn.start
    else:
        total_speaker_dict[speaker] = turn.end - turn.start

f_analyzer = open(FILENAME + "-2.csv", "x")
f_analyzer.write(f"speaker,total_time\n")

for x in total_speaker_dict:
    f_analyzer.write(f"{x},{total_speaker_dict[x]:.1f}\n")
    print(x)
f_audio.close()
f_analyzer.close()