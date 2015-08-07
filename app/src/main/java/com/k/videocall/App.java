package com.k.videocall;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import com.easemob.chat.EMChat;
import com.easemob.chat.EMChatManager;

import java.util.Iterator;
import java.util.List;

/**
 * Created by Kai on 2015/8/7.
 */
public class App extends Application {

    public static Context appContext;

    private CallReceiver callReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
        int pid = android.os.Process.myPid();
        String processAppName = getAppName(pid);

        if (processAppName == null || !processAppName.equalsIgnoreCase("com.k.videocall")){
            return;
        }

        EMChat.getInstance().init(this);
        EMChat.getInstance().setDebugMode(true);

        IntentFilter intentFilter = new IntentFilter(EMChatManager.getInstance().getIncomingCallBroadcastAction());
        if (callReceiver == null){
            callReceiver = new CallReceiver();
        }

        registerReceiver(callReceiver, intentFilter);
    }

    private String getAppName(int pId) {
        String processName = null;
        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List l = am.getRunningAppProcesses();
        Iterator i = l.iterator();
        PackageManager pm = this.getPackageManager();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());

            try {
                if (info.pid == pId) {
                    CharSequence c = pm.getApplicationLabel(pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
                    processName = info.processName;
                    return processName;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return processName;
    }
}
