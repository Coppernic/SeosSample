package fr.coppernic.demos.seos.interactor;

import android.content.Context;

import fr.coppernic.demos.seos.model.Device;

/**
 * Created by benoist on 13/06/17.
 */

public class RfidInteractorFactory {
    public static RfidInteractor create (Context context, Device device) {
        switch (device) {
            case C_One_SE3210: return new Se3210RfidInteractor(context, Device.C_One_SE3210);
            case C_One_e_ID_SE3210: return new Se3210RfidInteractor(context, Device.C_One_e_ID_SE3210);
            case C_One_OK_5127_CK_Mini: return new CkMiniRfidInteractorImpl(context);
            case ID_Platform: return new Se3210RfidInteractor(context, Device.ID_Platform);
            default: return new StubRfidInteractorImpl();
        }
    }
}
