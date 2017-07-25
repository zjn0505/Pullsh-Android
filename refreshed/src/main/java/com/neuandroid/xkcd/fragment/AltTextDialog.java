package com.neuandroid.xkcd.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.neuandroid.refreshed.R;

/**
 * Created by max on 14/07/17.
 */

/**
 * A dialog which was intended for displaying the alt text of a comic, but can also be used to
 * display other generic messages like errors or notices to the user.
 * Provides a listener interface for the creating activity to react differently to a positive
 * button click and a negative button click.
 */
public class AltTextDialog extends DialogFragment {

    public interface IAltTextInterfaceListener{
        void onPositiveClick();
        void onNegativeClick();
    }

    private String title;
    private String altText;
    private IAltTextInterfaceListener listener;

    public void setTitle(String title){
        this.title = title;
    }

    public void setAltText(String altText){
        this.altText = altText;
    }

    public void setListener(IAltTextInterfaceListener listener){
        this.listener = listener;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(altText).setTitle(title)
            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int id){
                    listener.onPositiveClick();
                    dismiss();
                }

            })
            .setNegativeButton(R.string.goto_explain, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    listener.onNegativeClick();
                    dismiss();
                }
            });
        return builder.create();

    }
}
