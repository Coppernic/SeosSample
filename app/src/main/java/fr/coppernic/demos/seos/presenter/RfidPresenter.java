package fr.coppernic.demos.seos.presenter;

import java.util.ArrayList;

import fr.coppernic.demos.seos.model.PacsData;

/**
 * Created by benoist on 02/06/17.
 */

public interface RfidPresenter {
    /**
     * Starts/stops RFID tags polling
     * @param enabled True enables, false disables
     */
    void readTags(boolean enabled);

    /**
     * Resets list of all tags read
     */
    void resetTagsRead();

    /**
     * Returns wether or not the RFID reader is reading
     * @return
     */
    boolean isReading();

    String getCurrentDevice();
}
