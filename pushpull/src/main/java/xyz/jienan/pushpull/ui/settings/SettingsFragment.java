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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.Random;

import xyz.jienan.pushpull.R;

/**
 * Created by jienanzhang on 23/11/2017.
 */

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    private final static String PULLSH_HOST = "pullsh_host";

    private static boolean hasReveseChanged = false;

    private SwitchPreference copyPref;
    private SharedPreferences sharedPreferences;
    private AlertDialog restoreDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        addPreferencesFromResource(R.xml.pullsh_config);
        ListPreference clickPref = (ListPreference) findPreference("pref_click");
        SwitchPreference reversePref = (SwitchPreference) findPreference("pref_reverse");
        copyPref = (SwitchPreference) findPreference("pref_copy");

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        clickPref.setSummary(clickPref.getEntry());
        clickPref.setOnPreferenceChangeListener(this);
        reversePref.setOnPreferenceChangeListener(this);
        copyPref.setOnPreferenceChangeListener(this);
        setCopyPref(copyPref.isChecked());

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case "pref_click":
                ((ListPreference) preference).setValue(newValue.toString());
                preference.setSummary(((ListPreference) preference).getEntry());
                break;
            case "pref_copy":
                setCopyPref((boolean) newValue);
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
                                getActivity().recreate();
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

    private void setCopyPref(boolean isChecked) {
        if (isChecked) {
            String url = sharedPreferences.getString(PULLSH_HOST, "https://jienan.xyz/m/");
            copyPref.setSummary(String.format(getString(R.string.pref_copy_full), url + randId(4)));
        } else {
            copyPref.setSummary(String.format(getString(R.string.pref_copy_id), randId(4)));
        }
    }

    private String randId(int length) {
        System.out.println("zjn");
        System.out.println('a' + 1);
        System.out.println('A' + 1);
        System.out.println('1' + 1);
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
