package com.mvhsapp.app;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;

/**
 * Utilities
 */
public class Utils {


    public static void addOnGlobalLayoutListener(final View view, final Runnable r) {
        ViewTreeObserver vto = view.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                new Handler(Looper.getMainLooper()).post(r);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    //noinspection deprecation
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
    }

    public static int convertDpToPx(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static void executeJavascript(WebView view, String javascript, ValueCallback<String> valueCallback) {
        //TODO: callback before kitkat? perhaps by injecting javascript to call Android function
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            view.evaluateJavascript(javascript, valueCallback);
        } else {
            view.loadUrl("javascript:" + javascript);
        }
    }

    public static void hideSoftKeyBoard(Activity context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm.isAcceptingText()) {
            //If keyboard is open
            imm.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), 0);
        }
    }
}
