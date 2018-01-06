package xyz.jienan.pushpull.ui;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import io.reactivex.functions.Consumer;
import xyz.jienan.pushpull.MemoApplication;
import xyz.jienan.pushpull.R;
import xyz.jienan.pushpull.ToastUtils;

import static xyz.jienan.pushpull.base.Const.PREF_KEY_ALIGN;

/**
 * Created by Jienan on 2018/1/5.
 */

public class FragmentInput extends Fragment {

    private EditText edtMemo;
    private SharedPreferences sharedPref;
    private InputMethodManager imm;
    private ImageView ivPushNotice;

    private boolean isPush = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = inflater.inflate(R.layout.fragment_input, container, false);
        edtMemo = view.findViewById(R.id.edt_memo);
        ivPushNotice = view.findViewById(R.id.iv_push_config_notice);
        ivPushNotice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtils.showToast(getActivity(), getResources().getString(R.string.toast_push_config_notice));
            }
        });
        if (isPush) {
            setupPush();
        } else {
            setupPull();
        }
        ((MemoApplication)getActivity().getApplication()).bus().toObservable()
                .subscribe(new Consumer<Object>() {

                    @Override
                    public void accept(Object o) throws Exception {
                        setPushNoticeVisibility();
                    }
                });
        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("edit_content", edtMemo.getText().toString() + "");
        outState.putInt("edit_content_selection_start", edtMemo.getSelectionStart());
        outState.putInt("edit_content_selection_end", edtMemo.getSelectionEnd());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            edtMemo.setText(savedInstanceState.getString("edit_content", ""));
            edtMemo.setSelection(savedInstanceState.getInt("edit_content_selection_start", 0),
                    savedInstanceState.getInt("edit_content_selection_end", 0));
        }
    }

    public void clearTextView() {
        edtMemo.setText("");
        edtMemo.clearComposingText();
    }

    public void setupPull() {
        isPush = false;
        if (!isAdded()) {
            return;
        }
        edtMemo.setHint(getString(R.string.input_area_hint_pull));
        edtMemo.setMaxEms(10);
        edtMemo.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        edtMemo.setPadding(dp2px(20), dp2px(20), dp2px(20), dp2px(20));
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(10);
        edtMemo.setFilters(filters);
        edtMemo.setSingleLine(true);
        edtMemo.setGravity(Gravity.CENTER);
        setPushNoticeVisibility();
    }

    public void setupPush() {
        isPush = true;
        if (!isAdded()) {
            return;
        }
        Log.d("zjn", "setupPush: ");
        edtMemo.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        edtMemo.setPadding(dp2px(20), dp2px(20), dp2px(20), dp2px(20));
        edtMemo.setHint(getString(R.string.input_area_hint_push));
        edtMemo.setSingleLine(false);
        edtMemo.setMaxEms(Integer.MAX_VALUE);
        String align = sharedPref.getString(PREF_KEY_ALIGN, "align_center");
        edtMemo.setFilters(new InputFilter[0]);
        if ("align_center".equals(align)) {
            edtMemo.setGravity(Gravity.CENTER);
        } else if ("align_left".equals(align)) {
            edtMemo.setGravity(Gravity.START);
        }
        setPushNoticeVisibility();
    }

    public void setupInput() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                edtMemo.requestFocus();
                imm.showSoftInput(edtMemo, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 500);
    }

    public void setEdtText(String edtText) {
        if (edtMemo != null) {
            edtMemo.setText(edtText);
        }
    }

    public String getInput() {
        return edtMemo.getText().toString();
    }

    public void setEdtGravity(int edtGravity) {
        edtMemo.setGravity(edtGravity);
    }

    private int dp2px(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp*scale + 0.5f);
    }

    public void setPushNoticeVisibility() {
        if (!isPush) {
            ivPushNotice.setVisibility(View.GONE);
            return;
        }
        int type = sharedPref.getInt("EXPIRED_TYPE", 3);
        int count = sharedPref.getInt("ACCESS_COUNT", 0);

        if (count != 0 || type != 3) {
            ivPushNotice.setVisibility(View.VISIBLE);
        } else {
            ivPushNotice.setVisibility(View.GONE);
        }
    }
}
