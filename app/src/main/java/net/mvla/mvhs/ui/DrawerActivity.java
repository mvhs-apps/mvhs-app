package net.mvla.mvhs.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import net.mvla.mvhs.PrefUtils;
import net.mvla.mvhs.R;

/**
 * Base Activity with the Navigation Drawer
 */
public abstract class DrawerActivity extends AppCompatActivity {
    protected static final int NAVDRAWER_ITEM_AERIES = 2;
    protected static final int NAVDRAWER_ITEM_MAP = 4;
    protected static final int NAVDRAWER_ITEM_CALENDAR = 6;
    protected static final int NAVDRAWER_ITEM_MVHSSITE = 8;
    protected static final int NAVDRAWER_ITEM_TODAYS_SCHED = 10;

    private static final int NAVDRAWER_ITEM_SETTINGS = -3;
    //private static final int NAVDRAWER_ITEM_HELP = -4;
    private static final int NAVDRAWER_ITEM_ABOUT = -5;
    private static final int NAVDRAWER_ITEM_INVALID = -1;
    private static final int NAVDRAWER_ITEM_SEPARATOR = -2;

    private static final int[] NAVDRAWER_ITEMS = new int[]{
            NAVDRAWER_ITEM_AERIES,
            NAVDRAWER_ITEM_TODAYS_SCHED,
            NAVDRAWER_ITEM_MAP,
            NAVDRAWER_ITEM_CALENDAR,
            NAVDRAWER_ITEM_MVHSSITE,
            NAVDRAWER_ITEM_SEPARATOR,
            NAVDRAWER_ITEM_SETTINGS,
            //NAVDRAWER_ITEM_HELP,
            NAVDRAWER_ITEM_ABOUT
    };
    private static final int[] NAVDRAWER_ITEMS_ICONS = new int[]{
            R.drawable.ic_grade_black_24dp,
            R.drawable.ic_view_agenda_black_24dp,
            R.drawable.ic_map_black_24dp,
            R.drawable.ic_calendar_black_24dp,
            R.drawable.ic_web_black_24dp,
            NAVDRAWER_ITEM_SEPARATOR,
            R.drawable.ic_settings_black_24dp,
            //NAVDRAWER_ITEM_HELP,
            R.drawable.ic_info_black_24dp
    };

    private static final int NAVDRAWER_LAUNCH_DELAY = 250;
    private static final int MAIN_CONTENT_FADEOUT_DURATION = 150;
    private static final int MAIN_CONTENT_FADEIN_DURATION = 250;

    private DrawerLayout mDrawerLayout;

    private Handler mHandler;

    @Nullable
    private Toolbar mActionBarToolbar;
    private LinearLayout mDrawerListLinearLayout;

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



        if (mActionBarToolbar != null) {
            mActionBarToolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp);
            mActionBarToolbar.setNavigationOnClickListener(view -> openDrawer());
        }

        mDrawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                onNavDrawerSlide(slideOffset);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                onNavDrawerClosed();
            }
        });

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

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

        mDrawerListLinearLayout = (LinearLayout) findViewById(R.id.activity_drawer_drawer_linearlayout);

        if (!PrefUtils.isWelcomeDone(this)) {
            PrefUtils.markWelcomeDone(this);
            mDrawerLayout.openDrawer(GravityCompat.START);
        }

        String[] navDrawerStrings = resources.getStringArray(R.array.nav_drawer_items);
        //INFLATE LAYOUTS AND SET CLICK LISTENERS
        for (int i = 0; i < NAVDRAWER_ITEMS.length; i++) {
            final int itemId = NAVDRAWER_ITEMS[i];

            View view = null;
            if (itemId == NAVDRAWER_ITEM_SEPARATOR) {
                view = getLayoutInflater().inflate(R.layout.list_item_separator,
                        mDrawerListLinearLayout, false);
                mDrawerListLinearLayout.addView(view);
            } else {
                FrameLayout layout = (FrameLayout) getLayoutInflater().inflate(R.layout.list_item_drawer, mDrawerListLinearLayout, false);
                TextView v = (TextView) layout.findViewById(android.R.id.text1);
                v.setText(navDrawerStrings[i]);

                Drawable drawable = ContextCompat.getDrawable(this, NAVDRAWER_ITEMS_ICONS[i]);
                drawable = DrawableCompat.wrap(drawable);
                drawable.mutate();
                DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
                if (itemId == getSelfNavDrawerItem()) {
                    DrawableCompat.setTint(drawable, getResources().getColor(R.color.primary_dark));
                    v.setTextColor(getResources().getColor(R.color.primary_dark));
                } else {
                    DrawableCompat.setTint(drawable, getResources().getColor(R.color.nav_drawer_icon));
                    v.setTextColor(getResources().getColor(R.color.primary_text_default_material_light));
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    v.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null);
                }
                v.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);


                if (itemId == getSelfNavDrawerItem()) {
                    v.setBackgroundColor(getResources().getColor(R.color.ripple_material_light));
                    setTitle(getString(R.string.nav_drawer_toolbar_prefix) + navDrawerStrings[i]);
                } else {
                    v.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                }


                layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onNavDrawerItemClicked(itemId);
                    }
                });

                mDrawerListLinearLayout.addView(layout);

                view = layout;
            }

            view.setTag(itemId);

        }
    }

    protected void openDrawer() {
        mDrawerLayout.openDrawer(GravityCompat.START);
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
    protected void onResume() {
        super.onResume();

        //TODO: Array of guest mode hidden items
        if (PrefUtils.isGuest(this) && (getSelfNavDrawerItem() == NAVDRAWER_ITEM_AERIES || getSelfNavDrawerItem() == NAVDRAWER_ITEM_CALENDAR)) {
            goToNavDrawerItem(NAVDRAWER_ITEM_MAP);
        }

        for (int i = 0; i < mDrawerListLinearLayout.getChildCount(); i++) {
            View item = mDrawerListLinearLayout.getChildAt(i);
            int itemId = (int) item.getTag();

            if (PrefUtils.isGuest(this) && (itemId == NAVDRAWER_ITEM_AERIES || itemId == NAVDRAWER_ITEM_CALENDAR)) {
                item.setVisibility(View.GONE);
            } else {
                item.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
                && itemId != NAVDRAWER_ITEM_ABOUT
                && itemId != NAVDRAWER_ITEM_MVHSSITE;
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
            case NAVDRAWER_ITEM_MVHSSITE:
                i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("http://www.mvla.net/MVHS/"));
                break;
            case NAVDRAWER_ITEM_CALENDAR:
                i = new Intent(this, StudentCalendarActivity.class);
                break;
            case NAVDRAWER_ITEM_SETTINGS:
                i = new Intent(this, SettingsActivity.class);
                break;
            case NAVDRAWER_ITEM_ABOUT:
                i = new Intent(this, AboutActivity.class);
                break;
            case NAVDRAWER_ITEM_TODAYS_SCHED:
                i = new Intent(this, ScheduleActivity.class);
                break;
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
