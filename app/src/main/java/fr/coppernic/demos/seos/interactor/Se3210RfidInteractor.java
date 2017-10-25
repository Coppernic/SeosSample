package fr.coppernic.demos.seos.interactor;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import fr.coppernic.sdk.power.PowerManager;
import fr.coppernic.sdk.power.api.PowerListener;
import fr.coppernic.sdk.power.api.peripheral.Peripheral;
import fr.coppernic.sdk.power.impl.idplatform.IdPlatformPeripheral;
import fr.coppernic.sdk.powermgmt.PowerMgmt;
import fr.coppernic.sdk.powermgmt.PowerMgmtFactory;
import fr.coppernic.sdk.powermgmt.PowerUtilsNotifier;
import fr.coppernic.sdk.powermgmt.cone.identifiers.InterfacesCone;
import fr.coppernic.sdk.powermgmt.cone.identifiers.ManufacturersCone;
import fr.coppernic.sdk.powermgmt.cone.identifiers.ModelsCone;
import fr.coppernic.sdk.powermgmt.cone.identifiers.PeripheralTypesCone;
import fr.coppernic.sdk.powermgmt.cone.utils.PowerUtils;
import fr.coppernic.demos.seos.model.Device;
import fr.coppernic.demos.seos.model.PacsData;
import fr.coppernic.sdk.hid.iclassProx.BaudRate;
import fr.coppernic.sdk.hid.iclassProx.ErrorCodes;
import fr.coppernic.sdk.hid.iclassProx.Reader;
import fr.coppernic.sdk.powermgmt.idplatform.helper.PowerHelper;
import fr.coppernic.sdk.utils.core.CpcDefinitions;
import fr.coppernic.sdk.utils.core.CpcResult;
import fr.coppernic.sdk.utils.io.InstanceListener;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

class Se3210RfidInteractor implements RfidInteractor, PowerUtilsNotifier, Observer<PacsData>, PowerListener {
    private static final String TAG = "RfidInteractor";
    private PowerMgmt powerMgmt;
    private PowerManager powerManager;
    private Peripheral peripheral;
    private PowerStateListener powerStateListener;
    private Reader reader;
    private Se3210Observable observable;
    private TagDiscoveredListener listener;
    private Device device;
    private Context context;

    Se3210RfidInteractor(Context context, Device device) {
        this.device = device;
        this.context = context;
        if (device != Device.ID_Platform) {
            try {
                powerMgmt = PowerMgmtFactory.get()
                        .setContext(context)
                        .build();
            } catch (Exception e) {
                Log.d(TAG, "Error building PowerMgmt");
            }
        } else {
            peripheral = IdPlatformPeripheral.RFID;
            PowerManager.get().registerListener(this);
        }

        Reader.getInstance(context, new InstanceListener<Reader>() {
            @Override
            public void onCreated(Reader instance) {
                reader = instance;
                observable = new Se3210Observable(reader);
            }

            @Override
            public void onDisposed(Reader instance) {
                Log.d(TAG, "onDisposed");
            }
        });
    }

    @Override
    public void startDiscovery(TagDiscoveredListener listener) {
        this.listener = listener;
        openPort();
        observable.getObservable()
                .subscribeOn(Schedulers.newThread())
                .subscribe(this);
        listener.onWaitingForTag();
    }

    @Override
    public void stopDiscovery() {
        if (observable != null) observable.stop();
    }

    @Override
    public void powerOn(boolean on, PowerStateListener listener) {
        powerStateListener = listener;
        if (device.equals(Device.C_One_SE3210)) {
            powerCone(on);
        } else if (device.equals(Device.C_One_e_ID_SE3210)) {
            powerConeEid(on);
        } else if (device.equals(Device.ID_Platform)) {
            powerIdPlatform(on);
            powerStateListener.onStateChanged(on);
        }
    }

    @Override
    public void onPowerUp(CpcResult.RESULT result, int vid, int pid) {
        if (vid == CpcDefinitions.VID_COPPERNIC && pid == CpcDefinitions.PID_RFID_HID_ICLASS) {
            powerStateListener.onStateChanged(true);
        }
    }

    @Override
    public void onPowerDown(CpcResult.RESULT result, int vid, int pid) {
        if (vid == CpcDefinitions.VID_COPPERNIC && pid == CpcDefinitions.PID_RFID_HID_ICLASS) {
            powerStateListener.onStateChanged(false);
        }
    }

    private void openPort(){

        String serialPortName = CpcDefinitions.HID_ICLASS_PROX_READER_PORT;

        if (device.equals(Device.ID_Platform)) {
            serialPortName = "/dev/ttyHSL0";
        }

        ErrorCodes res = reader.open(serialPortName, BaudRate.B9600);

        if (res == ErrorCodes.ER_OK) {
            Log.d(TAG, "Port opened");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            res = reader.samCommandSuspendAutonomousMode();
            if (res == ErrorCodes.ER_OK) {
                StringBuilder sbVersion, sbFirmware, sbSerialNumber;
                sbFirmware = new StringBuilder();
                sbVersion = new StringBuilder();
                sbSerialNumber = new StringBuilder();
                reader.samCommandGetVersionInfo(sbVersion,sbFirmware);
                reader.samCommandGetSerialNumber(sbSerialNumber);
            }else{
                //closePort();
            }
        } else {
        }
    }

    private void powerCone(boolean on) {
        if (powerMgmt != null) {
            powerMgmt.setPower(PeripheralTypesCone.RfidSc,
                    ManufacturersCone.Hid,
                    ModelsCone.IClassProx,
                    InterfacesCone.ExpansionPort,
                    on);

            SystemClock.sleep(100);
            powerStateListener.onStateChanged(on);
        }
    }

    private void powerConeEid(boolean on) {
        if (powerMgmt != null) {
            powerMgmt.setPower(PeripheralTypesCone.RfidSc,
                    ManufacturersCone.Hid,
                    ModelsCone.IClassProx,
                    InterfacesCone.UsbGpioPort,
                    on);
        }
    }

    private void powerIdPlatform(boolean on) {
        if (peripheral != null) {
            if (on) {
                peripheral.on(context);
            } else {
                peripheral.off(context);
            }
        }
    }

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onNext(PacsData pacsData) {
        listener.onTagRead(pacsData);
        listener.onWaitingForTag();
    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onComplete() {
        reader.close();
        listener.onDiscoveryFinished();
    }

    @Override
    public void onPowerUp(CpcResult.RESULT result, Peripheral peripheral) {

    }

    @Override
    public void onPowerDown(CpcResult.RESULT result, Peripheral peripheral) {

    }
}
