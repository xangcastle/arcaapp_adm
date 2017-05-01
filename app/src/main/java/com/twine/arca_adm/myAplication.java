package com.twine.arca_adm;

import com.activeandroid.ActiveAndroid;

/**
 * Created by TWINE-DELL on 16/3/2017.
 */

public class myAplication extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ActiveAndroid.initialize(this);
    }
}
