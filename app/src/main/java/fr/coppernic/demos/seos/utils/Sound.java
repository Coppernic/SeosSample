package fr.coppernic.demos.seos.utils;

import android.media.AudioManager;
import android.media.ToneGenerator;

/**
 * Created by benoist on 13/06/17.
 */

public class Sound {
    private ToneGenerator tg;
    private static Sound instance;

    private Sound() {
        tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
    }

    public static Sound getInstance() {
        if (instance == null) {
            instance = new Sound();
        }

        return instance;
    }

    public void beep() {
        try {
            tg.startTone(ToneGenerator.TONE_PROP_BEEP);
        } catch (Exception e) {

        }
    }

}
