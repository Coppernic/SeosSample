package fr.coppernic.demos.seos.view;

import fr.coppernic.demos.seos.model.PacsData;

/**
 * Created by benoist on 02/06/17.
 */

public interface MainView {

    /**
     * Displays a discovered tag
     * @param pacsData PACS data (Facility Code and Card Number)
     * @param updateList If true, the list of tags read will be updated
     */
    void showTag(PacsData pacsData, boolean updateList);

    /**
     * Show FloatingActionButton as play button
     */
    void showPlay();

    /**
     * Shows FloatingActionButton as stop button
     */
    void showWaitingForCard();

    /**
     * Shows reading status
     */
    void showReadingCard();

    /**
     * Shows number of time a same tag has been read
     */
    void showNbReads(int nbReads);
}
