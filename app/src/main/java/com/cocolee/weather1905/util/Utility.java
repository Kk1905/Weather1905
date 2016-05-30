package com.cocolee.weather1905.util;

import android.text.TextUtils;

import com.cocolee.weather1905.db.Weather1905DB;
import com.cocolee.weather1905.model.City;
import com.cocolee.weather1905.model.County;
import com.cocolee.weather1905.model.Province;

/**
 * Created by Administrator on 2016/5/29.
 */

public class Utility {
    /**
     * 解析和处理服务器返回的省级数据
     */
    public synchronized static boolean handleProvinceResponse(Weather1905DB db,String response)
    {
        if (!TextUtils.isEmpty(response))
        {
            String[] allProvinces=response.split(",");
            if (allProvinces!=null&&allProvinces.length>0)
            {
                for (String p:allProvinces)
                {
                    String[] array=p.split("\\|");
                    Province province=new Province();
                    province.setProvinceName(array[1]);
                    province.setProvinceCode(array[0]);
                    //将解析出来的数据，封装在province对象，再存储到数据库
                    db.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }
    /**
     * 解析和处理服务器返回的市级数据
     */
    public static boolean handleCityResponse(Weather1905DB db,String response,int provinceId)
    {
        if (!TextUtils.isEmpty(response))
        {
            String[] allCities=response.split(",");
            if (allCities!=null&&allCities.length>0)
            {
                for (String c:allCities)
                {
                    String[] array=c.split("\\|");
                    City city=new City();
                    city.setCityName(array[1]);
                    city.setCityCode(array[0]);
                    city.setProvinceId(provinceId);
                    db.saveCity(city);
                }
                return true;
            }
        }
        return false;
    }
    /**
     * 解析和处理服务器返回的县级数据
     */
    public static boolean handleCountyResponse(Weather1905DB db,String response,int cityId)
    {
        if (!TextUtils.isEmpty(response))
        {
            String[] allCounties=response.split(",");
            if (allCounties!=null&&allCounties.length>0)
            {
                for (String c:allCounties){
                    String[] array=c.split("\\|");
                    County county=new County();
                    county.setCountyName(array[1]);
                    county.setCountyCode(array[0]);
                    county.setCityId(cityId);
                    db.saveCounty(county);
                }
               return true;
            }
        }
        return false;
    }
}
