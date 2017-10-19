package fr.coppernic.demos.seos.view;

import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fr.coppernic.demos.seos.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class PreferencesActivityFragment extends PreferenceFragment {

    public PreferencesActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.prefs);
    }
}
