package com.kodeholic.itbook.common;

import android.content.Context;
import android.os.IBinder;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.kodeholic.itbook.lib.util.Log;

public class Utils {
    public static final String TAG = Utils.class.getSimpleName();

    public static EditText showSoftInput(Context context, EditText edit) {
        InputMethodManager input = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        input.showSoftInput(edit, InputMethodManager.SHOW_FORCED);

        return edit;
    }

    public static void hideSoftInput(Context context, View v, String f) {
        if (context != null && v != null) {
            v.clearFocus();
            hideSoftInput(context, v.getWindowToken(), f);
        }
    }

    public static void hideSoftInput(Context context, IBinder windowToken, String f) {
        Log.d(TAG, "hideSoftInput() - f: " + f);
        if (context != null && windowToken != null) {
            InputMethodManager input = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            input.hideSoftInputFromWindow(windowToken, 0);
        }
    }

    public static void runNew(Runnable runnable) {
        Thread t = new Thread(runnable);
        t.start();
    }
}
