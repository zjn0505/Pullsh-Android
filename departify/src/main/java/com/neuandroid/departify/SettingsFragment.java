package com.neuandroid.departify;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
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
        ListPreference langPreference = (ListPreference) findPreference("pref_lang");
        if (langPreference.getValue() == null) {
            langPreference.setValueIndex(0);
        }
        langPreference.setSummary(langPreference.getEntry());
        langPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ((ListPreference)preference).setValue(newValue.toString());
                preference.setSummary(((ListPreference)preference).getEntry());
                LocaleManager.setNewLocale(getActivity());
                return true;
            }
        });

    }

}
