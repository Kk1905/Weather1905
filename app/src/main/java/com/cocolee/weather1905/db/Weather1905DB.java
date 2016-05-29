package com.cocolee.weather1905.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cocolee.weather1905.model.City;
import com.cocolee.weather1905.model.County;
import com.cocolee.weather1905.model.Province;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/5/29.
 * 这个类主要是将我们常用的数据库操作封装，这样会很便于以后我们数据库操作
 * 并且，这个类有必要设计成单例模式
 */

public class Weather1905DB {
    /**
     * 数据库名
     */
    public static final String DB_NAME = "weather_1905";
    /**
     * 数据库版本
     */
    public static int VERSION = 1;

    private static Weather1905DB weather1905DB;
    private SQLiteDatabase db;

    /**
     * 将构造函数private，单例设计模式
     */
    private Weather1905DB(Context context) {
        Weather1905DBHelper dbHelper = new Weather1905DBHelper(context, DB_NAME, null, VERSION);
        db = dbHelper.getWritableDatabase();
    }

    /**
     * 获取Weather1905DB实例,注意：
     * 我们采用的是懒汉式单例，要注意同步的问题
     */
    public synchronized static Weather1905DB getInstance(Context context) {
        if (weather1905DB == null)
            weather1905DB = new Weather1905DB(context);
        return weather1905DB;
    }

    /**
     * 将Province实例存储到数据库
     */
    public void saveProvince(Province province) {
        if (province != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("province_name", province.getProvinceName());
            contentValues.put("province_code", province.getProvinceName());
            db.insert("Province", null, contentValues);
        }
    }

    /**
     * 从数据库读取全国各省份的信息
     */
    public List<Province> loadProvinces() {
        List<Province> list = new ArrayList<>();
        Cursor cursor = db.query("Province", null, null, null, null, null, null);
        //接着就是一行行的读取cursor中的数据，并赋值给proince对象，再添加给list集合
        if (cursor.moveToFirst()) {
            do {
                Province province = new Province();
                province.setId(cursor.getInt(cursor.getColumnIndex("id")));
                province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
                province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
                list.add(province);
            } while (cursor.moveToNext());
        }
        if (cursor != null)
            cursor.close();
        return list;
    }

    /**
     * 将City实例存储到数据库
     */
    public void saveCity(City city) {
        if (city != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("city_name", city.getCityName());
            contentValues.put("city_code", city.getCityCode());
            contentValues.put("province_id", city.getProvinceId());
            db.insert("City",null,contentValues);
        }

    }
    /**
     * 将各省份的city信息从数据库读取
     */
    public List<City> loadCity()
    {
        List<City> list=new ArrayList<>();
        Cursor cursor=db.query("City",null,null,null,null,null,null);
        if (cursor.moveToFirst())
        {
            do {
                City city=new City();
                city.setId(cursor.getInt(cursor.getColumnIndex("id")));
                city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
                city.setProvinceId(cursor.getInt(cursor.getColumnIndex("province_id")));
                list.add(city);
            }while (cursor.moveToNext());
        }
        if (cursor!=null)
            cursor.close();
        return list;
    }
    /**
     * 将County实例存储到数据库
     */
    public void saveCounty(County county)
    {
        if (county!=null)
        {
            ContentValues contentValues=new ContentValues();
            contentValues.put("county_name",county.getCountyName());
            contentValues.put("county_code",county.getCountyCode());
            contentValues.put("city_id",county.getCityId());
            db.insert("County",null,contentValues);
        }
    }
    /**
     * 从数据库查询各城市下所有县的信息
     */
    public List<County> loadCounty()
    {
        List<County> list=new ArrayList<>();
        Cursor cursor=db.query("County",null,null,null,null,null,null);
        if (cursor.moveToFirst())
        {
            do {
                County county=new County();
                county.setId(cursor.getInt(cursor.getColumnIndex("id")));
                county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
                county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
                county.setCityId(cursor.getInt(cursor.getColumnIndex("city_id")));
                list.add(county);
            }while (cursor.moveToNext());
        }
        if (cursor!=null)
            cursor.close();
        return list;
    }
}
