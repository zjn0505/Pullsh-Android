package com.neuandroid.departify;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

/**
 * Created by jienanzhang on 16/11/2017.
 */

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.departify_config);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.departify_config, false);
    }
}
