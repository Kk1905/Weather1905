package com.cocolee.weather1905.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cocolee.weather1905.R;
import com.cocolee.weather1905.service.AutoUpdateService;
import com.cocolee.weather1905.util.HttpCallbackListener;
import com.cocolee.weather1905.util.HttpUtil;
import com.cocolee.weather1905.util.Utility;

/**
 * Created by Administrator on 2016/5/30.
 */

public class WeatherActivity extends Activity implements View.OnClickListener {
    private LinearLayout weatherInfoLayout;
    /**
     * 用于显示城市名称
     */
    private TextView cityNameText;
    /**
     * 用于显示发布时间
     */
    private TextView publishTime;
    /**
     * 用于描述天气信息
     */
    private TextView weatherDespText;
    /**
     * 显示最高温度
     */
    private TextView temp1Text;
    /**
     * 显示最低温度
     */
    private TextView temp2Text;
    /**
     * 显示当前日期
     */
    private TextView currentDateText;

    /**
     * 切换城市的按钮
     */
    private Button switchCityButton;
    /**
     * 切换城市的按钮
     */
    private Button refreshWeatherButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_layout);
        //初始化各种控件
        weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        cityNameText = (TextView) findViewById(R.id.city_name);
        publishTime = (TextView) findViewById(R.id.publish_text);
        weatherDespText = (TextView) findViewById(R.id.weather_desp);
        temp1Text = (TextView) findViewById(R.id.temp1);
        temp2Text = (TextView) findViewById(R.id.temp2);
        currentDateText = (TextView) findViewById(R.id.current_date);

        //继续初始化控件，是我们添加的切换按钮和刷新按钮
        switchCityButton = (Button) findViewById(R.id.switch_city);
        refreshWeatherButton = (Button) findViewById(R.id.refresh_weather);

        //它们俩的点击事件，注册事件监听器
        switchCityButton.setOnClickListener(this);
        refreshWeatherButton.setOnClickListener(this);

        String countyCode = getIntent().getStringExtra("county_code");
        if (!TextUtils.isEmpty(countyCode)) {
            //有县级代号，就查询天气
            publishTime.setText("客官，别心急噻...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);//此时设置成不可见
            cityNameText.setVisibility(View.INVISIBLE);//一样，此时也不可见
            queryweatherCode(countyCode);
        } else
            //没有县级代号，就显示天气信息
            showWeather();
    }

    /**
     * 查询县级代号所对应额天气代号
     */
    private void queryweatherCode(String countyCode) {
        Log.d("kk", "--------->" + countyCode);
        String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
        queryFromServer(address, "countyCode");
    }

    /**
     * 查询天气代号所对应的天气信息
     */
    private void queryWeatherInfo(String weatherCode) {
        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
        queryFromServer(address, "weatherCode");
    }

    /**
     * 根据传入的地址和类型，从服务器查询天气代号或者天气信息
     */
    private void queryFromServer(final String address, final String type) {
        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                if ("countyCode".equals(type)) {
                    if (!TextUtils.isEmpty(response)) {
                        //从服务器返回的数据解析出天气代号
                        String[] array = response.split("\\|");
                        if (array != null && array.length == 2) {
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                } else if ("weatherCode".equals(type)) {
                    Utility.handleWeatherResponse(WeatherActivity.this, response);
                    //得到了天气信息，要去主线程进行改变Ui的操作，runOnUiThread()方法切换到Ui线程
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishTime.setText("客官，带了钱再来好伐？");

                    }
                });
            }
        });
    }

    /**
     * 从SharedPreferences文件中读取存储的天气信息，并显示到界面上
     */
    private void showWeather() {
        Log.d("kk", "--------->" + "showWeather()");
        SharedPreferences sharedPreferences = getSharedPreferences("weatherInfo", MODE_PRIVATE);
        cityNameText.setText(sharedPreferences.getString("city_name", ""));
        temp1Text.setText(sharedPreferences.getString("temp1", 0 + ""));
        temp2Text.setText(sharedPreferences.getString("temp2", 0 + ""));
        weatherDespText.setText(sharedPreferences.getString("weather_desp", ""));
        publishTime.setText("今天" + sharedPreferences.getString("publish_time", "") + "发布");
        currentDateText.setText(sharedPreferences.getString("current_date", ""));
        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);
        //第一次执行showWeather()的时候，就会开启服务，之后服务会一直在后台运行，并且每3小时更新sharedPreferences
        Intent intent=new Intent(this, AutoUpdateService.class);
        startService(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch_city:
                Intent intent = new Intent(this, ChooseAreaActivity.class);
                intent.putExtra("from WeatherActivity", true);//设置标志位，表示从WeatherActivity跳转
                startActivity(intent);
                finish();
                break;
            case R.id.refresh_weather:
                publishTime.setText("客官，你又急...");
                SharedPreferences sharedPreferences = getSharedPreferences("weatherInfo", MODE_PRIVATE);
                String weatherCode = sharedPreferences.getString("weather_code", "");
                if (!TextUtils.isEmpty(weatherCode))
                    queryWeatherInfo(weatherCode);
                break;
            default:
                break;
        }
    }
}
