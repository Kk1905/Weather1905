package com.cocolee.weather1905.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cocolee.weather1905.service.AutoUpdateService;

/**
 * Created by Administrator on 2016/5/31.
 */

public class AutoUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1=new Intent(context, AutoUpdateService.class);
        context.startService(intent);
    }
}
