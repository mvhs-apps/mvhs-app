package com.mvhsapp.app.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.mvhsapp.app.R;

/**
 * Base Activity with the Navigation Drawer
 */
public abstract class DrawerActivity extends AppCompatActivity {
    //TODO: Drawer shadow

    private static final int NAVDRAWER_ITEM_AERIES = 0;
    private static final int NAVDRAWER_ITEM_MAP = 1;
    private static final int NAVDRAWER_ITEM_SETTINGS = -3;
    //private static final int NAVDRAWER_ITEM_HELP = -4;
    private static final int NAVDRAWER_ITEM_ABOUT = -5;
    private static final int NAVDRAWER_ITEM_INVALID = -1;
    private static final int NAVDRAWER_ITEM_SEPARATOR = -2;
    private static final int[] NAVDRAWER_ITEMS = new int[]{
            NAVDRAWER_ITEM_AERIES,
            NAVDRAWER_ITEM_MAP,
            NAVDRAWER_ITEM_SEPARATOR,
            NAVDRAWER_ITEM_SETTINGS,
            //NAVDRAWER_ITEM_HELP,
            NAVDRAWER_ITEM_ABOUT
    };
    private static final int NAVDRAWER_LAUNCH_DELAY = 250;
    private static final int MAIN_CONTENT_FADEOUT_DURATION = 150;
    private static final int MAIN_CONTENT_FADEIN_DURATION = 250;

    private String[] mNavDrawerStrings;
    private DrawerLayout mDrawerLayout;

    private Handler mHandler;

    @Nullable
    private Toolbar mActionBarToolbar;

    /**
     * Returns the navigation drawer item that corresponds to this Activity.
     * Subclasses of BaseActivity override this to indicate what nav drawer item
     * corresponds to them.
     */
    int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_INVALID;
    }

    void onNavDrawerSlide(float offset) {
    }

    void onNavDrawerClosed() {
    }

    Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            if (mActionBarToolbar != null) {
                setSupportActionBar(mActionBarToolbar);
            }
        }
        return mActionBarToolbar;
    }

    /**
     * Sets up the navigation drawer as appropriate.
     */
    private void setupNavDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.activity_drawer_drawerlayout);
        Resources resources = getResources();

        mNavDrawerStrings = resources.getStringArray(R.array.nav_drawer_items);

        if (mActionBarToolbar != null) {
            mActionBarToolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp);
            mActionBarToolbar.setNavigationOnClickListener(new View
                    .OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                onNavDrawerSlide(slideOffset);
            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                onNavDrawerClosed();
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,GravityCompat.START);

        ScrollView mDrawerScrollView = (ScrollView) findViewById(R.id
                .activity_drawer_drawer_scrollview);
        int actionBarSize = resources.getDimensionPixelSize(R.dimen.navigation_drawer_margin);
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        int navDrawerWidthLimit = resources.getDimensionPixelSize(R.dimen.navigation_drawer_limit);
        int navDrawerWidth = displayMetrics.widthPixels - actionBarSize;
        if (navDrawerWidth > navDrawerWidthLimit) {
            navDrawerWidth = navDrawerWidthLimit;
        }
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) mDrawerScrollView
                .getLayoutParams();
        params.width = navDrawerWidth;
        mDrawerScrollView.setLayoutParams(params);

        LinearLayout mDrawerListLinearLayout = (LinearLayout) findViewById(R.id
                .activity_drawer_drawer_linearlayout);

        resetTitle();

        /*if (!PrefUtils.isWelcomeDone(this)) {
            PrefUtils.markWelcomeDone(this);
            mDrawerLayout.openDrawer(Gravity.START);
        }*/

        //INFLATE LAYOUTS AND SET CLICK LISTENERS
        for (int i = 0; i < NAVDRAWER_ITEMS.length; i++) {
            final int itemId = NAVDRAWER_ITEMS[i];
            if (itemId == NAVDRAWER_ITEM_SEPARATOR) {
                mDrawerListLinearLayout.addView(getLayoutInflater().inflate(R.layout.list_item_separator,
                        mDrawerListLinearLayout, false));
            } else {
                TextView v = (TextView) getLayoutInflater().inflate(R.layout.list_item_drawer, mDrawerListLinearLayout, false);
                v.setText(mNavDrawerStrings[i]);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onNavDrawerItemClicked(itemId);
                    }
                });
                mDrawerListLinearLayout.addView(v);
            }

        }
    }

    void resetTitle() {
        /*setTitle(NAVDRAWER_ACTIONBAR_TITLE_RES_ID[getSelfNavDrawerItem()]);
        ViewTreeObserver vto = findViewById(android.R.id.content).getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                if (getActionBarToolbar().isTitleTruncated()) {
                    setTitle(null);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    findViewById(android.R.id.content).getViewTreeObserver()
                            .removeOnGlobalLayoutListener(this);
                } else {
                    findViewById(android.R.id.content).getViewTreeObserver()
                            .removeGlobalOnLayoutListener(this);
                }
            }
        });*/
    }

    void lockDrawer(boolean lock) {
        if (lock) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        getActionBarToolbar();
    }

    @Override
    public void onBackPressed() {
        if (isNavDrawerOpen()) {
            closeNavDrawer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        resetTitle();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupNavDrawer();
        View mainContent = findViewById(R.id.activity_drawer_content);
        mainContent.setAlpha(0);
        mainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
    }

    private void onNavDrawerItemClicked(final int itemId) {
        if (itemId == getSelfNavDrawerItem()) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                goToNavDrawerItem(itemId);
            }
        }, NAVDRAWER_LAUNCH_DELAY);
        if (isNormalItem(itemId)) {
            View mainContent = findViewById(R.id.activity_drawer_content);
            if (mainContent != null) {
                mainContent.animate().alpha(0).setDuration
                        (MAIN_CONTENT_FADEOUT_DURATION);
            }
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private boolean isNormalItem(int itemId) {
        return itemId != NAVDRAWER_ITEM_SETTINGS
                //&& itemId != NAVDRAWER_ITEM_HELP
                && itemId != NAVDRAWER_ITEM_ABOUT;
    }

    boolean isNavDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    void closeNavDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void goToNavDrawerItem(int itemId) {
        Intent i;
        switch (itemId) {
            case NAVDRAWER_ITEM_AERIES:
                i = new Intent(this, AeriesActivity.class);
                break;
            case NAVDRAWER_ITEM_MAP:
                i = new Intent(this, MapActivity.class);
                break;
            /*case NAVDRAWER_ITEM_SETTINGS:
                i = new Intent(this, SettingsActivity.class);
                break;
            case NAVDRAWER_ITEM_ABOUT:
                i = new Intent(this, AboutActivity.class);
                break;*/
            default:
                Toast.makeText(getApplicationContext(), "Work in Progress",
                        Toast.LENGTH_SHORT).show();
                return;
        }

        startActivity(i);
        //If it is not a special item, finish this activity
        if (isNormalItem(itemId)) finish();
    }
}
