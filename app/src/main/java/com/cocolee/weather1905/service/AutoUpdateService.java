package com.cocolee.weather1905.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import com.cocolee.weather1905.receiver.AutoUpdateReceiver;
import com.cocolee.weather1905.util.HttpCallbackListener;
import com.cocolee.weather1905.util.HttpUtil;
import com.cocolee.weather1905.util.Utility;

/**
 * Created by Administrator on 2016/5/31.
 */

public class AutoUpdateService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateWeather();
            }
        }).start();
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long hour = 3 * 60 * 60 * 1000;//3小时的毫秒数
        long tirggerTime = SystemClock.elapsedRealtime() + hour;
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(AutoUpdateService.this, AutoUpdateReceiver.class), 0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, tirggerTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 更新天气信息的方法
     */
    private void updateWeather() {
        SharedPreferences sharedPreferences = getSharedPreferences("weatherInfo", MODE_PRIVATE);
        String weatherCode = sharedPreferences.getString("weather_code", "");
        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                //跟新天气信息，我们只要做到跟新sharedPreferences信息就行,接下来的在Ui界面显示，就交给showWeather()
                Utility.handleWeatherResponse(AutoUpdateService.this, response);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }
}
