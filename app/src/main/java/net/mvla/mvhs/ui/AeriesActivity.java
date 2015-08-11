package net.mvla.mvhs.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.crashlytics.android.Crashlytics;

import net.mvla.mvhs.BuildConfig;
import net.mvla.mvhs.PrefUtils;
import net.mvla.mvhs.R;

import io.fabric.sdk.android.Fabric;

public class AeriesActivity extends DrawerActivity {


    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
        setContentView(R.layout.activity_aeries);

        if (!PrefUtils.isWelcomeDone(this)) {
            startActivity(new Intent(this, WelcomeActivity.class));
        }

        FragmentManager fm = getFragmentManager();
        Fragment f = fm.findFragmentById(R.id.activity_aeries_fragment);
        if (f == null) {
            f = new AeriesFragment();
            fm.beginTransaction()
                    .replace(R.id.activity_aeries_fragment, f)
                    .commit();
        }

        mProgressBar = (ProgressBar) findViewById(R.id.activity_aeries_progress_bar);

        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        AeriesFragment f = (AeriesFragment) getFragmentManager().findFragmentById(R.id.activity_aeries_fragment);
        if (!f.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AeriesFragment.RC_SAVE || requestCode == AeriesFragment.RC_READ || requestCode == AeriesFragment.RC_HINT) {
            Fragment f = getFragmentManager().findFragmentById(R.id.activity_aeries_fragment);
            f.onActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    public void setProgressBarProgress(int newProgress) {
        if (newProgress < 100) {
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setIndeterminate(false);
            mProgressBar.setProgress(newProgress);
        } else if (newProgress == 100) {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    public void showIndeterminateProgressBar() {
        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    public void hideIndeterminateProgressBar() {
        mProgressBar.setIndeterminate(false);
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_AERIES;
    }
}
