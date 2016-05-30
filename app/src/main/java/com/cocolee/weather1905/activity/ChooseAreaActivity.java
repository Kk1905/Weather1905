package com.cocolee.weather1905.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cocolee.weather1905.R;
import com.cocolee.weather1905.db.Weather1905DB;
import com.cocolee.weather1905.model.City;
import com.cocolee.weather1905.model.County;
import com.cocolee.weather1905.model.Province;
import com.cocolee.weather1905.util.HttpCallbackListener;
import com.cocolee.weather1905.util.HttpUtil;
import com.cocolee.weather1905.util.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/5/29.
 */

public class ChooseAreaActivity extends Activity {
    private static final int LEVEL_PROVINCE = 0;
    private static final int LEVEL_CITY = 1;
    private static final int LEVEL_COUNTY = 2;

    private ProgressDialog dialog;
    private TextView textView;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private Weather1905DB db;
    private List<String> dataList = new ArrayList<>();//dataList相当于一个缓冲用的集合
    /**
     * 省列表
     */
    private List<Province> provinceList;
    /**
     * 市列表
     */
    private List<City> cityList;
    /**
     * 县列表
     */
    private List<County> countyList;
    /**
     * 被选中的省
     */
    private Province selectedProvince;
    /**
     * 被选中的市
     */
    private City selectedCity;
    /**
     * 被选中的县
     */
    private County selectedCounty;
    /**
     * 当前选中的级别
     */
    private int currentLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences=getSharedPreferences("weatherInfo",MODE_PRIVATE);
        if (sharedPreferences.getBoolean("city_selected",false))
        {
            Intent intent=new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);
        listView = (ListView) findViewById(R.id.list_view);
        textView = (TextView) findViewById(R.id.title_text);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);//初始化适配器，item_1是系统提供的一个子项
        listView.setAdapter(adapter);//设置适配器
        db = Weather1905DB.getInstance(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCity();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounty();
                }else if (currentLevel==LEVEL_COUNTY)
                {
                    String countyCode=countyList.get(position).getCountyCode();
                    Intent intent=new Intent(ChooseAreaActivity.this,WeatherActivity.class);
                    intent.putExtra("county_code",countyCode);
                    startActivity(intent);
                    finish();
                }
            }
        });

        queryProvince();//加载省级数据
    }

    /**
     * 查询全国所有省，优先从数据库查询，如果没有，就要到服务器上查询
     */
    private void queryProvince() {
        provinceList = db.loadProvinces();
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);//每次回到最上头位置
            textView.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        } else
            queryFromServer(null, "province");
    }

    /**
     * 查询全国所有市，优先从数据库查询，如果没有，就要到服务器上查询
     */
    private void queryCity() {
        cityList = db.loadCity(selectedProvince.getId());
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            textView.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        } else
            queryFromServer(selectedProvince.getProvinceCode(), "city");
    }

    /**
     * 查询全国所有县，优先从数据库查询，如果没有，就要到服务器上查询
     */
    private void queryCounty() {
        countyList = db.loadCounty(selectedCity.getId());
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            textView.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        } else
            queryFromServer(selectedCity.getCityCode(), "county");
    }

    /**
     * 根据传入的代号和类型，从服务器上查询省市县数据
     */
    private void queryFromServer(final String code, final String type) {
        String address;
        if (!TextUtils.isEmpty(code)) {
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
            Log.d("kk","---------->"+code);
        } else
            address = "http://www.weather.com.cn/data/list3/city.xml";

        showProgressDialog();

        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onFinish(String response) {
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(db, response);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(db, response, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(db, response, selectedCity.getId());
                }
                if (result) {
                    //通过runOnUiThread()方法返回到主线程处理逻辑
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            clossProgressDialog();
                            if ("province".equals(type))
                                queryProvince();
                            else if ("city".equals(type))
                                queryCity();
                            else if ("county".equals(type))
                                queryCounty();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
//通过runOnUiThread()方法返回到主线程处理逻辑
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        clossProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this, "加载失败0_0", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (dialog == null) {
            dialog = new ProgressDialog(this);
            dialog.setMessage("奴家正在努力加载...");
            dialog.setCanceledOnTouchOutside(false);
        }
        dialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void clossProgressDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    /**
     * 捕获Back键，根据当前的级别来判断，此时应该返回市列表，省列表还是直接退出
     */
    @Override
    public void onBackPressed() {
        if (currentLevel == LEVEL_COUNTY)
            queryCity();
        else if (currentLevel == LEVEL_CITY)
            queryProvince();
        else
            finish();//退出activity
    }
}
