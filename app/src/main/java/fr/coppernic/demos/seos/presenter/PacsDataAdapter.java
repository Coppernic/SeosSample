package fr.coppernic.demos.seos.presenter;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import fr.coppernic.demos.seos.model.PacsData;
import fr.coppernic.demos.seos.view.Row;
import fr.coppernic.demos.seos.view.RowView;

/**
 * Created by benoist on 06/06/17.
 */

public class PacsDataAdapter extends ArrayAdapter<PacsData> {
    private List<PacsData> data;
    private LayoutInflater layoutInflater;
    private int layout;

    public PacsDataAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<PacsData> objects) {
        super(context, resource, objects);

        data = objects;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layout = resource;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Nullable
    @Override
    public PacsData getItem(int position) {
        return data.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = layoutInflater.inflate(layout, null);
        }

        RowView row = new Row(view);

        PacsData pacsData = data.get(position);

        row.showFacilityCode(pacsData.getFc());
        row.showCardNumber(pacsData.getCardNumber());

        return view;
    }
}
