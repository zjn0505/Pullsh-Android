package xyz.jienan.pushpull.ui.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDelegate;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.Random;

import xyz.jienan.pushpull.R;

import static xyz.jienan.pushpull.base.Const.PREF_KEY_ALIGN;
import static xyz.jienan.pushpull.base.Const.PREF_KEY_CLICK;
import static xyz.jienan.pushpull.base.Const.PREF_KEY_COPY;
import static xyz.jienan.pushpull.base.Const.PREF_KEY_NIGHT;
import static xyz.jienan.pushpull.base.Const.PREF_KEY_PULLSH_HOST;
import static xyz.jienan.pushpull.base.Const.PREF_KEY_REVERSE;

/**
 * Created by jienanzhang on 23/11/2017.
 */

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    private ListPreference clickPref;
    private SwitchPreference copyPref;
    private SwitchPreference reversePref;
    private SwitchPreference nightPref;
    private ListPreference alignPref;
    private SharedPreferences sharedPreferences;
    private AlertDialog restoreDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        addPreferencesFromResource(R.xml.pullsh_config);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        clickPref = (ListPreference) findPreference(PREF_KEY_CLICK);
        reversePref = (SwitchPreference) findPreference(PREF_KEY_REVERSE);
        nightPref = (SwitchPreference) findPreference(PREF_KEY_NIGHT);
        copyPref = (SwitchPreference) findPreference(PREF_KEY_COPY);
        alignPref = (ListPreference) findPreference(PREF_KEY_ALIGN);

        clickPref.setSummary(clickPref.getEntry());
        clickPref.setOnPreferenceChangeListener(this);
        reversePref.setOnPreferenceChangeListener(this);
        copyPref.setOnPreferenceChangeListener(this);
        nightPref.setOnPreferenceChangeListener(this);
        setCopyPref(copyPref.isChecked());
        alignPref.setSummary(alignPref.getEntry());
        alignPref.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case PREF_KEY_CLICK:
            case PREF_KEY_ALIGN:
                ((ListPreference) preference).setValue(newValue.toString());
                preference.setSummary(((ListPreference) preference).getEntry());
                break;
            case PREF_KEY_COPY:
                setCopyPref((boolean) newValue);
                break;
            case PREF_KEY_NIGHT:
                if ((boolean) newValue) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                getActivity().recreate();
                break;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_settings, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_restore) {
            if (restoreDialog == null) {
                restoreDialog = new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.restore_alert_title)
                        .setMessage(R.string.restore_alert_msg)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().clear().commit();
                                PreferenceManager.setDefaultValues(getActivity(), R.xml.pullsh_config, true);
                                reset();
                                dialogInterface.dismiss();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).create();
            }
            restoreDialog.show();
        }

        return true;
    }

    private void reset() {
        String click = sharedPreferences.getString(PREF_KEY_CLICK ,"click_push");
        boolean copy = sharedPreferences.getBoolean(PREF_KEY_COPY,true);
        boolean reverse = sharedPreferences.getBoolean(PREF_KEY_REVERSE ,false);
        boolean night = sharedPreferences.getBoolean(PREF_KEY_NIGHT, false);
        String align = sharedPreferences.getString(PREF_KEY_ALIGN ,"align_center");

        int resClick = getResources().getIdentifier(click, "string", getActivity().getPackageName());
        clickPref.setSummary(getString(resClick));
        clickPref.setValue(click);
        copyPref.setChecked(copy);
        reversePref.setChecked(reverse);
        if (night != nightPref.isChecked()) {
            nightPref.setChecked(night);
            if (night) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            getActivity().recreate();
        }
        int resAlign = getResources().getIdentifier(align, "string", getActivity().getPackageName());
        alignPref.setSummary(getString(resAlign));
        alignPref.setValue(align);
    }

    private void setCopyPref(boolean isChecked) {
        if (isChecked) {
            String url = sharedPreferences.getString(PREF_KEY_PULLSH_HOST, "https://pullsh.me/");
            copyPref.setSummary(String.format(getString(R.string.pref_copy_full), url + randId(4)));
        } else {
            copyPref.setSummary(String.format(getString(R.string.pref_copy_id), randId(4)));
        }
    }

    private String randId(int length) {
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();
        for (int i = 0; i < length; i++) {
            int index = rand.nextInt(62);
            if (index < 10) {
                sb.append(index);
            } else if (index < 36) {
                sb.append((char) ('a' + index - 10));
            } else {
                sb.append((char) ('A' + index - 36));
            }
        }
        return sb.toString();
    }
}
