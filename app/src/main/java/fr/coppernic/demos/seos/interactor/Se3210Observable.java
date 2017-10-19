package fr.coppernic.demos.seos.interactor;

import android.os.SystemClock;

import java.util.concurrent.atomic.AtomicBoolean;

import fr.coppernic.demos.seos.model.PacsData;
import fr.coppernic.sdk.hid.iclassProx.Card;
import fr.coppernic.sdk.hid.iclassProx.ErrorCodes;
import fr.coppernic.sdk.hid.iclassProx.FrameProtocol;
import fr.coppernic.sdk.hid.iclassProx.Reader;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by benoist on 15/06/17.
 */

public class Se3210Observable implements ObservableOnSubscribe<PacsData> {
    private Observable<PacsData> observable;
    private Reader reader;

    public Se3210Observable(Reader reader) {
        observable = Observable.create(this);
        this.reader = reader;
    }

    private AtomicBoolean stopped = new AtomicBoolean(false);

    public void stop() {
        stopped.set(true);
    }

    @Override
    public void subscribe(ObservableEmitter<PacsData> e) throws Exception {
        stopped.set(false);

        Card card = new Card();
        FrameProtocol[] frameProtocolList = new FrameProtocol[3];
        frameProtocolList[0] = FrameProtocol.PICO15693;
        frameProtocolList[1] = FrameProtocol.ISO14443A;
        frameProtocolList[2] = FrameProtocol.ISO15693;

        while(!stopped.get()) {
            SystemClock.sleep(50);
            ErrorCodes er = reader.samCommandScanFieldForCard(frameProtocolList, card);

            if (er != ErrorCodes.ER_OK) {
                // TODO handle error
            }

            er = reader.getCardNumber(card);

            if (er == ErrorCodes.ER_OK) {
                PacsData pacsData = new PacsData();
                pacsData.setFc(card.getFacilityCode());
                pacsData.setCardNumber(card.getCardNumber());

                e.onNext(pacsData);
            } else {
                // TODO handle error
            }
        }

        e.onComplete();
    }

    public Observable<PacsData> getObservable() {
        return observable;
    }
}