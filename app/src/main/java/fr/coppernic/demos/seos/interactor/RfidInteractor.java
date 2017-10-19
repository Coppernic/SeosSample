package fr.coppernic.demos.seos.interactor;

import fr.coppernic.demos.seos.model.PacsData;

/**
 * Created by benoist on 02/06/17.
 */

public interface RfidInteractor {
    String TAG = "RfidInteractor";

    /**
     * Listener for discovered tags
     */
    interface TagDiscoveredListener {

        /**
         * Callback triggered when the reader is waiting for a card
         */
        void onWaitingForTag();

        /**
         * Callback triggered when a tag has been read
         * @param pacsData PACS data (Facility Code and Card Number)
         */
        void onTagRead(PacsData pacsData);

        /**
         * Callback triggered when a tag has been discovered
         */
        void onTagDiscovered();

        /**
         * Callback triggered when the discover task is ended
         */
        void onDiscoveryFinished();
    }

    /**
     * Starts polling for contact less cards
     */
    void startDiscovery(TagDiscoveredListener listener);

    /**
     * Stops polling for contact less cards
     */
    void stopDiscovery();

    interface PowerStateListener {
        void onStateChanged(boolean state);
    }

    /**
     * Powers on/off RFID reader
     * @param on True on, false off
     */
    void powerOn(boolean on, PowerStateListener listener);
}
