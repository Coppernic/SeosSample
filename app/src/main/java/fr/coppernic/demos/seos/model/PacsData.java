package fr.coppernic.demos.seos.model;

import fr.coppernic.sdk.utils.core.CpcBytes;

/**
 * Created by benoist on 29/03/17.
 */

public class PacsData {

    private static final int FC_MASK = 0x00FE0000;
    private static final int CN_MASK = 0x0001FFFE;

    private int facilityCode;
    private int cardNumber;

    public PacsData() {

    }

    public PacsData(byte[] wiegand26) {
        int wiegand = CpcBytes.byteArrayToInt(wiegand26, true);

        int fcNotAligned = wiegand&FC_MASK;
        facilityCode = (fcNotAligned>>17)&0x000000FF;
        int cardNumberNotALigned = wiegand & CN_MASK;
        cardNumber = (cardNumberNotALigned>>1)&0x0000FFFF;
    }

    public int getFc() {
        return facilityCode;
    }

    public void setFc (int fc) {
        facilityCode = fc;
    }

    public int getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber (int cn) {
        cardNumber = cn;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(Integer.toString(facilityCode));
        sb.append(" - ");
        sb.append(Integer.toString(cardNumber));

        return sb.toString();
    }
}
