package com.leo;

import android.app.Application;

import com.leo.system.ContextHelp;
import com.leo.system.LogUtil;
import com.leo.system.enume.LogType;

public class LeoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ContextHelp.setContext(this);
        LogUtil.setType(LogType.DEBUG);
    }
}
