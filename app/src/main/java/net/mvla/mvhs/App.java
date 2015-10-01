package net.mvla.mvhs;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //LeakCanary.install(this);
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
    }
}
