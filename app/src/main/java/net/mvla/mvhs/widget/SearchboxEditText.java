package net.mvla.mvhs.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Test
 */
public class SearchboxEditText extends EditText {

    private Callback mCallback;

    public SearchboxEditText(Context context) {
        super(context);
    }

    public SearchboxEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SearchboxEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            boolean b = mCallback.onKeyBackPreIme(event);
            if (b) {
                return true;
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public interface Callback {
        boolean onKeyBackPreIme(KeyEvent event);
    }
}
