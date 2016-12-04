package com.wangyi.demo;

import android.app.Application;
import android.content.Context;

/**
 * Created on 2016/12/4.
 *
 * @author WangYi
 */

public class AppIml extends Application {
    public static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
    }
}
