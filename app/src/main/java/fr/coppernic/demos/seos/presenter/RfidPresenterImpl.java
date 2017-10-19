package fr.coppernic.demos.seos.presenter;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;

import fr.coppernic.demos.seos.R;
import fr.coppernic.demos.seos.interactor.CkMiniRfidInteractorImpl;
import fr.coppernic.demos.seos.interactor.RfidInteractor;
import fr.coppernic.demos.seos.interactor.RfidInteractorFactory;
import fr.coppernic.demos.seos.model.Device;
import fr.coppernic.demos.seos.model.PacsData;
import fr.coppernic.demos.seos.utils.Sound;
import fr.coppernic.demos.seos.view.MainView;

public class RfidPresenterImpl implements RfidPresenter, RfidInteractor.TagDiscoveredListener, RfidInteractor.PowerStateListener{
    private static final String TAG = "RfidPresenter";
    private RfidInteractor rfidInteractor;
    private MainView mainView;
    private boolean reading;
    private PacsData latest;
    private Device device;

    /**
     * List of read tags
     */
    private ArrayList<PacsData> tagsRead = new ArrayList<>();
    private int nbReads = 0;

    public RfidPresenterImpl(MainView mainView) {
        this.mainView = mainView;
    }

    @Override
    public void readTags(boolean enabled) {
        reading = enabled;
        if (enabled) {
            rfidInteractor.powerOn(reading, this);
        } else {
            rfidInteractor.stopDiscovery();
        }
    }

    @Override
    public void resetTagsRead() {
        tagsRead.clear();
        latest = new PacsData();
        nbReads = 0;
    }

    @Override
    public void onWaitingForTag() {
        mainView.showWaitingForCard();
    }

    @Override
    public void onTagRead(PacsData pacsData) {
        if (pacsData.getCardNumber() != latest.getCardNumber()) {
            nbReads = 1;
            // Updates latest tag read
            mainView.showTag(pacsData, !hasAlreadyBeenRead(pacsData));
            latest = pacsData;
            // Updates list of all tags read
            if (!hasAlreadyBeenRead(pacsData)) {
                Log.d(TAG, "First");
                tagsRead.add(pacsData);
            } else {
                Log.d(TAG, "Already read");
            }
        } else {
            nbReads++;
        }

        Sound.getInstance().beep();

        mainView.showNbReads(nbReads);
    }

    @Override
    public void onTagDiscovered() {
        mainView.showReadingCard();
    }

    @Override
    public void onDiscoveryFinished() {
        rfidInteractor.powerOn(false, this);
    }

    @Override
    public void onStateChanged(boolean state) {
        Log.d(TAG, "onStateChanged(" + Boolean.toString(state) + ")");
        if (state) {
            rfidInteractor.startDiscovery(this);
        } else {
            mainView.showPlay();
        }
    }

    private boolean hasAlreadyBeenRead(PacsData pacsData) {
        for (PacsData data:tagsRead) {
            if (pacsData.getCardNumber() == data.getCardNumber()) return true;
        }

        return false;
    }

    @Override
    public boolean isReading() {
        return reading;
    }

    @Override
    public String getCurrentDevice() {
        Context context = (Context)mainView;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String device = prefs.getString(context.getString(R.string.pref_device_key), context.getString(R.string.no_device_selected));
        if (device.equals(context.getString(R.string.cone_iclass))) {
            this.device = Device.C_One_SE3210;
        } else if (device.equals(context.getString(R.string.cone_eid_iclass))) {
            this.device = Device.C_One_e_ID_SE3210;
        } else if (device.equals(context.getString(R.string.cone_seos))) {
            this.device = Device.C_One_OK_5127_CK_Mini;
        } else if (device.equals(context.getString(R.string.id_platform))) {
            this.device = Device.ID_Platform;
        } else {
            this.device = Device.No_device_selected;
        }

        rfidInteractor = RfidInteractorFactory.create((Context)mainView, this.device);
        rfidInteractor.powerOn(false, new RfidInteractor.PowerStateListener() {
            @Override
            public void onStateChanged(boolean state) {

            }
        });
        latest = new PacsData();

        return device;
    }
}
