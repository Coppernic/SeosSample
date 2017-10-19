package fr.coppernic.demos.seos.interactor;

import android.content.Context;

import java.util.ArrayList;

import fr.coppernic.sdk.powermgmt.PowerMgmt;
import fr.coppernic.sdk.powermgmt.PowerMgmtFactory;
import fr.coppernic.sdk.powermgmt.PowerUtilsNotifier;
import fr.coppernic.sdk.powermgmt.cone.identifiers.InterfacesCone;
import fr.coppernic.sdk.powermgmt.cone.identifiers.ManufacturersCone;
import fr.coppernic.sdk.powermgmt.cone.identifiers.ModelsCone;
import fr.coppernic.sdk.powermgmt.cone.identifiers.PeripheralTypesCone;
import fr.coppernic.sdk.powermgmt.cone.utils.PowerUtils;
import fr.coppernic.sdk.pcsc.Scard;
import fr.coppernic.sdk.utils.core.CpcDefinitions;
import fr.coppernic.sdk.utils.core.CpcResult;

/**
 * Created by benoist on 02/06/17.
 */

public class CkMiniRfidInteractorImpl implements RfidInteractor, PowerUtilsNotifier {

    private PowerMgmt powerMgmt;
    private PowerStateListener powerStateListener;
    private Scard scard;

    private DiscoverTask discoverTask;

    public CkMiniRfidInteractorImpl(Context context) {
        powerMgmt = PowerMgmtFactory.get().build();
        scard = new Scard();
        scard.establishContext(context);
    }

    @Override
    public void startDiscovery(TagDiscoveredListener listener) {
        discoverTask = new DiscoverTask(listener);
        discoverTask.execute(scard);
    }

    @Override
    public void stopDiscovery() {
        if (discoverTask != null) {
            discoverTask.cancel(false);
        }
    }

    @Override
    public void powerOn(boolean on, PowerStateListener listener) {
        powerStateListener = listener;
        powerMgmt.setPower(PeripheralTypesCone.RfidSc,
                ManufacturersCone.Hid,
                ModelsCone.Ok5127CkMini,
                InterfacesCone.ExpansionPort,
                on);
    }

    @Override
    public void onPowerUp(CpcResult.RESULT result, int i, int i1) {
        if (i == CpcDefinitions.VID_HID_OK5127MINICK_READER && i1 == CpcDefinitions.PID_HID_OK5127MINICK_READER) {
            powerStateListener.onStateChanged(true);
            ArrayList<String> readers = new ArrayList<>();
            scard.listReaders(readers);
        }
    }

    @Override
    public void onPowerDown(CpcResult.RESULT result, int i, int i1) {
        if (i == CpcDefinitions.VID_HID_OK5127MINICK_READER && i1 == CpcDefinitions.PID_HID_OK5127MINICK_READER) {
            powerStateListener.onStateChanged(false);
        }
    }
}
