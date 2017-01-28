package net.mvla.mvhs.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import net.mvla.mvhs.PrefUtils;
import net.mvla.mvhs.R;
import net.mvla.mvhs.Utils;
import net.mvla.mvhs.aeries.AeriesActivity;
import net.mvla.mvhs.customtabs.CustomTabActivityHelper;
import net.mvla.mvhs.map.MapActivity;
import net.mvla.mvhs.schedulecalendar.ScheduleCalendarActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Base Activity with the Navigation Drawer
 */
public abstract class DrawerActivity extends AppCompatActivity {
    private static final int NAVDRAWER_LAUNCH_DELAY = 250;
    @Nullable
    @BindView(R.id.toolbar_actionbar)
    Toolbar actionBarToolbar;
    @BindView(R.id.drawer_navview)
    NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private Handler handler;

    public static boolean openApp(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        Intent i = manager.getLaunchIntentForPackage(packageName);
        if (i == null) {
            return false;
        }
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        context.startActivity(i);
        return true;
    }

    /**
     * Returns the navigation drawer item that corresponds to this Activity.
     * Subclasses of DrawerActivity override this to indicate what nav drawer item
     * corresponds to them.
     */
    protected abstract int getSelfNavDrawerItem();

    void onNavDrawerSlide(float offset) {
    }

    void onNavDrawerClosed() {
    }

    /**
     * Sets up the navigation drawer as appropriate.
     */
    private void setupNavDrawer() {
        drawerLayout = (DrawerLayout) findViewById(R.id.activity_drawer_drawerlayout);

        if (actionBarToolbar != null) {
            actionBarToolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp);
            actionBarToolbar.setNavigationOnClickListener(view -> openDrawer());
        }

        drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                onNavDrawerSlide(slideOffset);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                onNavDrawerClosed();
            }
        });

        navigationView.setLayoutParams(new DrawerLayout.LayoutParams(Utils.getNavDrawerWidth(this),
                DrawerLayout.LayoutParams.MATCH_PARENT, Gravity.START));

        if (!PrefUtils.isWelcomeDone(this)) {
            PrefUtils.markWelcomeDone(this);
            drawerLayout.openDrawer(GravityCompat.START);
        }

        navigationView.setCheckedItem(getSelfNavDrawerItem());
        navigationView.setNavigationItemSelectedListener(item -> {
            onNavDrawerItemClicked(item.getItemId());
            return false;
        });

        navigationView.setItemTextColor(ContextCompat.getColorStateList(this, R.color.drawer_text));
        navigationView.setItemIconTintList(ContextCompat.getColorStateList(this, R.color.drawer_icons));
    }

    private boolean isNormalItem(int itemId) {
        return itemId != R.id.nav_settings
                && itemId != R.id.nav_changelog
                && itemId != R.id.nav_about
                && itemId != R.id.nav_site
                && itemId != R.id.nav_classroom
                && itemId != R.id.nav_feedback;
    }

    protected String getToolbarTitle(String navDrawerString) {
        return getString(R.string.nav_drawer_toolbar_prefix) + navDrawerString;
    }

    protected void openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START);
    }

    void lockDrawer(boolean lock) {
        if (lock) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
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
        handler = new Handler();
    }

    @Override
    protected void onResume() {
        super.onResume();

       /* //TODO: Guest mode hidden items
        if (PrefUtils.getMode(this) == 1 && (
                getSelfNavDrawerItem() == NAVDRAWER_ITEM_AERIES
                        || getSelfNavDrawerItem() == NAVDRAWER_ITEM_CALENDAR)) {
            goToNavDrawerItem(NAVDRAWER_ITEM_MAP);
        }

        for (int i = 0; i < mDrawerListLinearLayout.getChildCount(); i++) {
            View item = mDrawerListLinearLayout.getChildAt(i);
            int itemId = (int) item.getTag();

            if (PrefUtils.getMode(this) == 1 &&
                    (itemId == NAVDRAWER_ITEM_AERIES
                            || itemId == NAVDRAWER_ITEM_CALENDAR
                            || itemId == NAVDRAWER_ITEM_GCLASSROOM)
                    ) {
                item.setVisibility(View.GONE);
            } else {
                item.setVisibility(View.VISIBLE);
            }
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupNavDrawer();
    }

    private void onNavDrawerItemClicked(final int itemId) {
        if (itemId == getSelfNavDrawerItem()) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        handler.postDelayed(() -> goToNavDrawerItem(itemId), NAVDRAWER_LAUNCH_DELAY);

        drawerLayout.closeDrawer(GravityCompat.START);
    }

    boolean isNavDrawerOpen() {
        return drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START);
    }

    void closeNavDrawer() {
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public void showChangelog() {
        ChangelogDialog.newInstance().show(getFragmentManager(), "CHANGELOG_DIALOG");
    }

    private void goToNavDrawerItem(int itemId) {
        Intent intent = null;
        switch (itemId) {
            case R.id.nav_sched:
                intent = new Intent(this, ScheduleCalendarActivity.class);
                break;
            case R.id.nav_aeries:
                intent = new Intent(this, AeriesActivity.class);
                break;
            case R.id.nav_map:
                intent = new Intent(this, MapActivity.class);
                break;
            case R.id.nav_site:
                Uri website = Uri.parse("http://www.mvla.net/MVHS/");

                CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                        .setToolbarColor(ContextCompat.getColor(this, R.color.primary))
                        .enableUrlBarHiding()
                        .setShowTitle(true)
                        .build();
                CustomTabActivityHelper.openCustomTab(
                        this, customTabsIntent, website, (activity, uri) -> {
                            Intent open = new Intent(Intent.ACTION_VIEW);
                            open.setData(uri);
                            activity.startActivity(open);
                        }
                );
                break;
            case R.id.nav_cal:
                intent = new Intent(this, StudentCalendarActivity.class);
                break;
            case R.id.nav_settings:
                intent = new Intent(this, SettingsActivity.class);
                break;
            case R.id.nav_changelog:
                showChangelog();
                return;
            case R.id.nav_classroom:
                if (!openApp(this, "com.google.android.apps.classroom")) {
                    Uri gclassroom = Uri.parse("http://classroom.google.com");

                    CustomTabsIntent tabsIntent = new CustomTabsIntent.Builder()
                            .setToolbarColor(ContextCompat.getColor(this, R.color.primary))
                            .enableUrlBarHiding()
                            .setShowTitle(true)
                            .build();
                    CustomTabActivityHelper.openCustomTab(
                            this, tabsIntent, gclassroom, (activity, uri) -> {
                                Intent open = new Intent(Intent.ACTION_VIEW);
                                open.setData(uri);
                                activity.startActivity(open);
                            }
                    );
                }
                return;
            case R.id.nav_feedback:
                new FeedbackDialog().show(getFragmentManager(), "FEEDBACK");
                return;
            case R.id.nav_about:
                intent = new Intent(this, AboutActivity.class);
                break;
            default:
                Toast.makeText(getApplicationContext(), "Work in Progress",
                        Toast.LENGTH_SHORT).show();
                return;
        }

        if (intent != null) {
            startActivity(intent);


            //If it is not a special item, finish this activity
            if (isNormalItem(itemId)) {
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        }
    }
}
