package xyz.jienan.pushpull;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by jienanzhang on 22/11/2017.
 */

public class ToastUtils {
    private static Toast toast;
    public final static void showToast(Context context, String text) {
        try{
            toast.getView().isShown();
            toast.setText(text);
        } catch (Exception e) {
            toast = Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_SHORT);
        }
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
