package net.mvla.mvhs.aeries;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import net.mvla.mvhs.R;
import net.mvla.mvhs.ui.DrawerActivity;

public class AeriesActivity extends DrawerActivity {


    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aeries);

        FragmentManager fm = getFragmentManager();
        Fragment f = fm.findFragmentById(R.id.activity_aeries_fragment);
        if (f == null) {
            f = new AeriesFragment();
            fm.beginTransaction()
                    .replace(R.id.activity_aeries_fragment, f)
                    .commit();
        }

        mProgressBar = (ProgressBar) findViewById(R.id.activity_aeries_progress_bar);
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
    protected int getSelfNavDrawerItem() {
        return R.id.nav_aeries;
    }
}
