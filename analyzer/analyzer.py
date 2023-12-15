import os
import sys
import time
from pyannote.audio import Pipeline

AUTH_TOKEN = "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"

pipeline = Pipeline.from_pretrained(
    "pyannote/speaker-diarization-3.1",
    use_auth_token=AUTH_TOKEN)

# send pipeline to GPU (when available)
import torch
pipeline.to(torch.device("cuda"))


def analyse(filename):
    print("Starting analysis")
    current_file_path = os.path.abspath(__file__)
    current_file_dir = os.path.dirname(current_file_path)

    diarization = pipeline(f"{current_file_dir}/../recorder/recordings/{filename}")
    total_speaker_dict = {}
    analysis_time = int(time.time())

    f_audio = open(f"{current_file_dir}/../recorder/recordings/audio_data/{analysis_time}.csv", "x")
    f_audio.write("start,end,speaker\n")

    for turn, _, speaker in diarization.itertracks(yield_label=True):
        f_audio.write(f"{turn.start:.1f},{turn.end:.1f},{speaker}\n")
        if (speaker in total_speaker_dict):
            total_speaker_dict[speaker] += turn.end - turn.start
        else:
            total_speaker_dict[speaker] = turn.end - turn.start

    f_analyzer = open(f"{current_file_dir}/../recorder/recordings/analyzer_data/{analysis_time}.csv", "x")
    f_analyzer.write(f"speaker,total_time\n")

    for x in total_speaker_dict:
        f_analyzer.write(f"{x},{total_speaker_dict[x]:.1f}\n")
        print(x)
    f_audio.close()
    f_analyzer.close()



if __name__ == '__main__':
    analyse(sys.argv[1])