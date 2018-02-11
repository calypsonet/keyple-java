/*
 * Copyright 2018 Keyple - https://keyple.org/
 *
 * Licensed under GPL/MIT/Apache ???
 */

package org.keyple.plugin.androidnfc;

import android.app.Activity;
import android.app.Application;
import android.widget.TextView;


/**
 * Created by ixxi on 12/01/2018.
 */

public class AndroidApp extends Application {

    private static AndroidApp myInstance = null;
    private static Activity myActivity;
    public static ReaderThread myThread;
    private static AndroidNFCPlugin myAndroidNFCPlugin;
    private static TextView myTxtView;

    @Override
    public void onCreate() {
        super.onCreate();
        myInstance = this;
    }

    public static AndroidApp getInstance() {
        return myInstance;
    }

    public static void CreateThread(TextView txtView, Activity CurrentActivity,
            AndroidNFCPlugin androidNFCPlugin) throws Exception {
        myActivity = CurrentActivity;
        myTxtView = txtView;
        myAndroidNFCPlugin = androidNFCPlugin;
        StartThread();
    }

    public static void StartThread() throws Exception {
        if (myThread != null)
            myThread = null;
        myThread = new ReaderThread(myTxtView, myActivity, myAndroidNFCPlugin);
    }



}
