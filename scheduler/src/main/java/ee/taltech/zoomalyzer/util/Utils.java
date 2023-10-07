package ee.taltech.zoomalyzer.util;

import ee.taltech.zoomalyzer.entities.Recording;

public class Utils {
    public static String getUniqueName(Recording recording) {
        return String.format("recording-%s", recording.getId());
    }
}
