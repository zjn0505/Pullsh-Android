package xyz.jienan.pushpull.ui;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import io.reactivex.functions.Consumer;
import xyz.jienan.pushpull.MemoApplication;
import xyz.jienan.pushpull.R;

import static xyz.jienan.pushpull.base.Const.PREF_KEY_PUSH_ACCESS_COUNT;
import static xyz.jienan.pushpull.base.Const.PREF_KEY_PUSH_EXPIRED_TIME;
import static xyz.jienan.pushpull.base.Const.PREF_KEY_PUSH_EXPIRED_TYPE;

/**
 * Created by Jienan on 2018/1/5.
 */

public class FragmentPushConfig extends Fragment {

    private RadioGroup rgExpired;

    private TextView tvExpire;
    private SeekBar sbPeriod;
    private EditText edtAllowance;
    private CheckBox cbAllowance;
    private TextView tvAllowancePre;
    private TextView tvAllowanceSuf;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean savedStateCbAllowance = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_expire, container, false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = sharedPreferences.edit();

        rgExpired = view.findViewById(R.id.rg_expire);
        sbPeriod = view.findViewById(R.id.sb_period);
        tvExpire = view.findViewById(R.id.tv_expire);
        edtAllowance = view.findViewById(R.id.edt_allowance);
        cbAllowance = view.findViewById(R.id.cb_allowance);
        tvAllowancePre = view.findViewById(R.id.tv_allowance_pre);
        tvAllowanceSuf = view.findViewById(R.id.tv_allowance_suf);

        rgExpired.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_min:
                        sbPeriod.setMax(59);
                        tvExpire.setEnabled(true);
                        sbPeriod.setEnabled(true);
                        editor.putInt(PREF_KEY_PUSH_EXPIRED_TYPE, 0);
                        break;
                    case R.id.rb_hr:
                        sbPeriod.setMax(47);
                        tvExpire.setEnabled(true);
                        sbPeriod.setEnabled(true);
                        editor.putInt(PREF_KEY_PUSH_EXPIRED_TYPE, 1);
                        break;
                    case R.id.rb_day:
                        sbPeriod.setMax(29);
                        tvExpire.setEnabled(true);
                        sbPeriod.setEnabled(true);
                        editor.putInt(PREF_KEY_PUSH_EXPIRED_TYPE, 2);
                        break;
                    case R.id.rb_infi:
                        tvExpire.setEnabled(false);
                        sbPeriod.setEnabled(false);
                        editor.putInt(PREF_KEY_PUSH_EXPIRED_TYPE, 3);
                        break;
                }
                editor.apply();
            }
        });

        sbPeriod.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvExpire.setText(String.format("%d", progress+1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                editor.putInt(PREF_KEY_PUSH_EXPIRED_TIME, seekBar.getProgress()+1);
                editor.apply();
            }
        });
        cbAllowance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int allowance;
                if (isChecked) {
                    changeAllowance(true);
                    try {
                        allowance = Integer.valueOf(edtAllowance.getText().toString());
                    } catch (NumberFormatException e) {
                        allowance = 0;
                    }
                } else {
                    changeAllowance(false);
                    allowance = 0;
                }
                editor.putInt(PREF_KEY_PUSH_ACCESS_COUNT, allowance);
                editor.apply();
            }
        });
        edtAllowance.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int allowance;
                try {
                    allowance = Integer.valueOf(edtAllowance.getText().toString());
                } catch (NumberFormatException e) {
                    allowance = 0;
                }
                if (!cbAllowance.isChecked()) {
                    allowance = 0;
                }
                editor.putInt(PREF_KEY_PUSH_ACCESS_COUNT, allowance);
                editor.apply();

            }
        });
        ((MemoApplication)getActivity().getApplication()).bus().toObservable()
                .subscribe(new Consumer<Object>() {

                    @Override
                    public void accept(Object o) throws Exception {
                        updateUI();
                    }
                });
        updateUI();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    private void updateUI() {
        if (sharedPreferences != null) {
            int time = sharedPreferences.getInt(PREF_KEY_PUSH_EXPIRED_TIME, 1);
            int type = sharedPreferences.getInt(PREF_KEY_PUSH_EXPIRED_TYPE, 3);
            int count = sharedPreferences.getInt(PREF_KEY_PUSH_ACCESS_COUNT, 0);

            switch (type) {
                case 0:
                    rgExpired.check(R.id.rb_min);
                    sbPeriod.setMax(59);
                    break;
                case 1:
                    rgExpired.check(R.id.rb_hr);
                    sbPeriod.setMax(47);
                    break;
                case 2:
                    rgExpired.check(R.id.rb_day);
                    sbPeriod.setMax(29);
                    break;
                case 3:
                    rgExpired.check(R.id.rb_infi);
                    sbPeriod.setEnabled(false);
                    break;
            }
            sbPeriod.setProgress(time-1);
            if (count == 0) {
                changeAllowance(false);
                if (savedStateCbAllowance) {
                    changeAllowance(true);
                }
            } else {
                changeAllowance(true);
            }
            edtAllowance.setText(String.valueOf(count));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("check_allowance", cbAllowance.isChecked());
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            savedStateCbAllowance = savedInstanceState.getBoolean("check_allowance", false);
        }
        setRetainInstance(true);
    }

    private void changeAllowance(boolean enabled) {
        cbAllowance.setChecked(enabled);
        tvAllowancePre.setEnabled(enabled);
        edtAllowance.setEnabled(enabled);
        tvAllowanceSuf.setEnabled(enabled);
    }
}
