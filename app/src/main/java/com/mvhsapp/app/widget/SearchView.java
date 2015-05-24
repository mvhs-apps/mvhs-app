package com.mvhsapp.app.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.ResultReceiver;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mvhsapp.app.R;

import java.lang.reflect.Method;

/**
 * SearchView
 */
public class SearchView extends RelativeLayout {

    static final Reflector HIDDEN_METHOD_INVOKER = new Reflector();

    /*
     * SearchView can be set expanded before the IME is ready to be shown during
     * initial UI setup. The show operation is asynchronous to account for this.
     */
    private Runnable mShowImeRunnable = new Runnable() {
        public void run() {
            InputMethodManager imm = (InputMethodManager)
                    getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm != null) {
                HIDDEN_METHOD_INVOKER.showSoftInputUnchecked(imm, SearchView.this, 0);
            }
        }
    };

    private DrawerArrowView mDrawerArrowView;
    private SearchboxEditText mSearchboxEditText;
    private SearchViewCallback mSearchViewCallback;


    public SearchView(Context context) {
        this(context, null);
    }

    public SearchView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.searchbox, this, true);

        mSearchboxEditText = (SearchboxEditText) findViewById(R.id.searchbox_edittext);
        mSearchboxEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mSearchViewCallback != null) {
                    mSearchViewCallback.onQueryTextChange(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mSearchboxEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    clearFocus();
                }
                return false;
            }
        });
        mSearchboxEditText.setCallback(new SearchboxEditText.Callback() {
            @Override
            public boolean onKeyBackPreIme(KeyEvent event) {
                // special case for the back key, we do not even try to send it
                // to the drop down list but instead, consume it immediately
                if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                    KeyEvent.DispatcherState state = getKeyDispatcherState();
                    if (state != null) {
                        state.startTracking(event, this);
                    }
                    return true;
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    KeyEvent.DispatcherState state = getKeyDispatcherState();
                    if (state != null) {
                        state.handleUpEvent(event);
                    }
                    if (event.isTracking() && !event.isCanceled()) {
                        clearFocus();
                        return true;
                    }
                }
                return false;
            }
        });

        mDrawerArrowView = (DrawerArrowView) findViewById(R.id.searchbox_menu_button);
        mDrawerArrowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSearchViewCallback != null) {
                    mSearchViewCallback.onDrawerIconClicked();
                }
            }
        });

        setBackgroundResource(R.drawable.searchbox_bg);
        setFocusableInTouchMode(true);

        mSearchboxEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (mSearchViewCallback != null) {
                    mSearchViewCallback.onFocusChange(hasFocus);
                }
                setImeVisibility(hasFocus);
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMeasure = MeasureSpec.makeMeasureSpec(getResources().getDimensionPixelSize(R.dimen.searchbox_height), MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasure);
    }

    @Override
    public void clearFocus() {
        setImeVisibility(false);
        super.clearFocus();
        mSearchboxEditText.clearFocus();
    }

    public void setDrawerIconVisibility(boolean show, boolean animate) {
        float editTextTarget = show ? 0f : getResources().getDimensionPixelSize(R.dimen.offset);

        if (animate) {
            ObjectAnimator animator = new ObjectAnimator();
            animator.setTarget(mSearchboxEditText);
            animator.setProperty(TRANSLATION_X);
            animator.setFloatValues(editTextTarget);
            animator.setDuration(250);
            animator.setInterpolator(new FastOutSlowInInterpolator());
            animator.start();

            ObjectAnimator animator2 = new ObjectAnimator();
            animator2.setTarget(mDrawerArrowView);
            animator2.setDuration(150);
            if (show) animator2.setStartDelay(100);
            animator2.setProperty(SCALE_X);
            animator2.setFloatValues(show ? 1f : 0f);
            animator2.setInterpolator(new FastOutSlowInInterpolator());
            animator2.start();

            ObjectAnimator animator3 = new ObjectAnimator();
            animator3.setTarget(mDrawerArrowView);
            animator3.setDuration(150);
            if (show) animator3.setStartDelay(100);
            animator3.setProperty(SCALE_Y);
            animator3.setFloatValues(show ? 1f : 0f);
            animator3.setInterpolator(new FastOutSlowInInterpolator());
            animator3.start();
        } else {
            mSearchboxEditText.setTranslationX(editTextTarget);
            mDrawerArrowView.setScaleX(show ? 1f : 0f);
            mDrawerArrowView.setScaleY(show ? 1f : 0f);
        }
    }

    public void setCallback(SearchViewCallback listener) {
        mSearchViewCallback = listener;
    }

    private void setImeVisibility(final boolean visible) {
        if (visible) {
            post(mShowImeRunnable);
        } else {
            removeCallbacks(mShowImeRunnable);
            InputMethodManager imm = (InputMethodManager)
                    getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm != null) {
                imm.hideSoftInputFromWindow(getWindowToken(), 0);
            }
        }
    }

    public void clearText() {
        mSearchboxEditText.setText("");
    }

    public void setDrawerIconState(boolean burger, boolean animate) {
        mDrawerArrowView.setState(burger, animate);
    }

    public interface SearchViewCallback {
        // boolean onQueryTextSubmit(String query);

        void onQueryTextChange(String newText);

        void onFocusChange(boolean focused);

        void onDrawerIconClicked();
    }

    private static class Reflector {
        private Method showSoftInputUnchecked;

        Reflector() {
            try {
                showSoftInputUnchecked = InputMethodManager.class.getMethod(
                        "showSoftInputUnchecked", int.class, ResultReceiver.class);
                showSoftInputUnchecked.setAccessible(true);
            } catch (NoSuchMethodException e) {
                // Ah well.
            }
        }

        void showSoftInputUnchecked(InputMethodManager imm, View view, int flags) {
            if (showSoftInputUnchecked != null) {
                try {
                    showSoftInputUnchecked.invoke(imm, flags, null);
                    return;
                } catch (Exception ignored) {
                }
            }

            // Hidden method failed, call public version instead
            imm.showSoftInput(view, flags);
        }
    }


}
