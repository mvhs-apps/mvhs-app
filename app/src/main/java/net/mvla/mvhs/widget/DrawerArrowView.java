package net.mvla.mvhs.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.widget.ImageButton;

/**
 * Arrow view
 */
public class DrawerArrowView extends ImageButton {

    private DrawerArrowDrawable mSearchboxArrowDrawable;
    private ValueAnimator mAnimator;

    public DrawerArrowView(Context context) {
        this(context, null);
    }

    public DrawerArrowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawerArrowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mSearchboxArrowDrawable = new DrawerArrowDrawable(context);
        setImageDrawable(mSearchboxArrowDrawable);
    }

    public void setState(boolean burger, boolean animate) {
        if (animate) {
            if (mAnimator != null && mAnimator.isRunning())
                mAnimator.cancel();
            if (burger) {
                mAnimator = ValueAnimator.ofFloat(mSearchboxArrowDrawable.getProgress(), 0f);
            } else {
                mAnimator = ValueAnimator.ofFloat(mSearchboxArrowDrawable.getProgress(), 1f);
            }
            mAnimator.setInterpolator(new FastOutSlowInInterpolator());
            mAnimator.setDuration(250);
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mSearchboxArrowDrawable.setProgress((float) animation.getAnimatedValue());
                    invalidate();
                }
            });
            mAnimator.start();
        } else {
            if (burger) {
                mSearchboxArrowDrawable.setProgress(0f);
            } else {
                mSearchboxArrowDrawable.setProgress(1f);
            }
        }
    }
}
