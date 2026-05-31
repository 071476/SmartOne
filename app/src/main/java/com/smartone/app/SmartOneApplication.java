package com.smartone.app;

import android.app.Application;
import com.smartone.app.di.AppContainer;

public class SmartOneApplication extends Application {

    public AppContainer container;

    @Override
    public void onCreate() {
        super.onCreate();
        container = new AppContainer(this);
    }

    public static SmartOneApplication from(Application app) {
        return (SmartOneApplication) app;
    }
}
