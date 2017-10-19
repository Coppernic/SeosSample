package fr.coppernic.demos.seos.view;

import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import fr.coppernic.demos.seos.R;

/**
 * Created by benoist on 06/06/17.
 */

public class Row implements RowView {

    private View view;

    public Row(View view) {
        this.view = view;
    }

    @Override
    public void showFacilityCode(int facilityCode) {
        TextView tvFacilityCode = (TextView)view.findViewById(R.id.tvFacilityCodeValue);
        tvFacilityCode.setText(Integer.toString(facilityCode));
    }

    @Override
    public void showCardNumber(int cardNumber) {
        TextView tvCardNumber = (TextView)view.findViewById(R.id.tvCardNumberValue);
        tvCardNumber.setText(String.format("%d", cardNumber));
    }
}
