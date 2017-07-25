package com.neuandroid.xkcd.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.NumberPicker;

import com.neuandroid.refreshed.R;


/**
 * Created by max on 14/07/17.
 */

/**
 * A generic number picker dialog. Requires a min and max int value to be created.
 * Provides listener that the creator should implement.
 */
public class NumberPickerDialog extends DialogFragment {

    public interface INumberPickerDialogListener{
        void onPositiveClick(int number);
        void onNegativeClick();
    }

    private String title;
    private String content;
    private int min;
    private int max;
    private INumberPickerDialogListener listener;

    public void setTitle(String title){
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setRange(int min, int max){
        this.min = min;
        this.max = max;
    }

    public void setListener(INumberPickerDialogListener listener){
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View contentView = getActivity().getLayoutInflater().inflate(R.layout.number_picker_dialog, null);
        final NumberPicker numberPicker = (NumberPicker) contentView.findViewById(R.id.number_picker);
        numberPicker.setMaxValue(max);
        numberPicker.setMinValue(min);
        builder.setView(contentView)
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onPositiveClick(numberPicker.getValue());
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onNegativeClick();
                        dismiss();
                    }
                });
        return builder.create();
    }
}
