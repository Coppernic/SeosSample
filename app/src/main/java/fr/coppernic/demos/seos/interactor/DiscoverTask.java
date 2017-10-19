package fr.coppernic.demos.seos.interactor;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import fr.coppernic.demos.seos.model.PacsData;
import fr.coppernic.sdk.pcsc.ApduResponse;
import fr.coppernic.sdk.pcsc.Scard;
import fr.coppernic.sdk.utils.core.CpcBytes;
import fr.coppernic.sdk.utils.core.CpcDefinitions;
import fr.coppernic.sdk.utils.core.CpcResult;

/**
 * Created by benoist on 02/06/17.
 */

public class DiscoverTask extends AsyncTask<Scard, Void, Integer> {

    private static final String TAG = "DiscoverTask";

    private static final int POLLING_INTERVAL_MS = 50;

    public static final int ERROR_WITH_READER = -1;
    public static final int ERROR_WRONG_ANSWER = -2;
    public static final int ERROR_TASK_INTERRUPTED = -3;

    private RfidInteractor.TagDiscoveredListener listener;

    public DiscoverTask (RfidInteractor.TagDiscoveredListener listener) {
        this.listener = listener;
    }

    @Override
    protected Integer doInBackground(Scard... params) {
        Log.d(TAG, "doInBackground Start");
        Scard scard = params[0];

        while (!isCancelled()) {
            // Waiting for a card to be connected
            pollForCard(scard);
            // Stops task if it has been cancelled
            if (this.isCancelled()) {
                return ERROR_TASK_INTERRUPTED;
            }
            // Checks if ATR is OK
            if (isValidAtr(scard.getAtr())) {
                listener.onTagDiscovered();
                ApduResponse response = sendCommands(scard);
                scard.disconnect();

                if (isValidReponse(response)) {
                    PacsData pacsData = getPacsData(response.getData());
                    listener.onTagRead(pacsData);
                } else {
                    scard.disconnect();
                    SystemClock.sleep(1000);
                }
            } else {
                return ERROR_WITH_READER;
            }
        }

        return ERROR_TASK_INTERRUPTED;
    }

    /**
     * Blocks until a card is read
     * @param scard Scard instance
     */
    private void pollForCard (Scard scard) {
        CpcResult.RESULT res = CpcResult.RESULT.ERROR;

        listener.onWaitingForTag();

        while (res != CpcResult.RESULT.OK && !this.isCancelled()) {
            SystemClock.sleep(POLLING_INTERVAL_MS);
            res = scard.connect(CpcDefinitions.PCSC_DESCRIPTION_OK5127CKMINI_READER, 0, 0);
            if (scard.getAtr() == null || scard.getAtr().length == 0) {
                res = CpcResult.RESULT.ERROR;
            }
        }
    }

    /**
     * Returns true if ATR length is > 0
     * @param atr ATR to be validated
     * @return true/false
     */
    private boolean isValidAtr (byte[] atr) {
        return atr.length > 0;
    }

    /**
     * Sends commands to the reader to retrieve the PACS data
     * @param scard Scard instance
     */
    private ApduResponse sendCommands (Scard scard) {
        ApduResponse response = new ApduResponse();
        scard.transmit(null, new byte[]{(byte) 0xFF, (byte) 0xCA, 0x00, 0x00, 0x00}, null, response);
        scard.transmit(null, new byte[]{(byte) 0xFF, (byte) 0x68, 0x0D, 0x00, 0x00}, null, response);

        return response;
    }

    /**
     * Checks if APDU response is OK
     * @param response APDU response
     * @return true/false
     */
    private boolean isValidReponse(ApduResponse response) {
        return !CpcBytes.arrayCmp(response.getStatus(), new byte[]{0x6F, 0x00});
    }

    /**
     * Gets PACS data from byte array
     * @param data
     * @return
     */
    private PacsData getPacsData (byte[] data) {
        try {
            byte[] answer = CpcBytes.parseHexStringToArray(CpcBytes.byteArrayToAsciiString(data));
            PacsData pacsData = new PacsData(answer);
            return pacsData;
        } catch (Exception ex) {
            return new PacsData();
        }
    }
}
