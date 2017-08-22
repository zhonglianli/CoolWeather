package com.app.coolweather.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.app.coolweather.model.City;
import com.app.coolweather.model.Country;
import com.app.coolweather.model.Province;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/8/21.
 */

public class CoolWeatherDB {

    public static final String DB_NAME = "cool_weather";

    public static final int VERSION = 1;

    public static CoolWeatherDB coolWeatherDB;

    public SQLiteDatabase db;


    /**
     *
     * let the constructor be private.饿汉模式
     * @param context
     */
    private CoolWeatherDB(Context context){
        CoolWeatherOpenHelper dbHelper = new CoolWeatherOpenHelper(context,DB_NAME,null,VERSION);
        db = dbHelper.getWritableDatabase();
    }

    public synchronized static CoolWeatherDB getInstance(Context context){
        if (null == coolWeatherDB){
            coolWeatherDB = new CoolWeatherDB(context);
        }
        return coolWeatherDB;
    }

    /**
     * 
     * Save instance of Province into database
     * @param province
     */
    public void saveProvince(Province province){
        if (province != null){

            ContentValues values = new ContentValues();
            values.put("province_name",province.getProvinceName());
            values.put("province_code",province.getProvinceCode());
            db.insert("Province",null,values);
        }
    }

    /**
     * 
     * read Province data to a new created instance.
     * @return
     */
    public List<Province> loadProvince(){
        
        List<Province> list = new ArrayList<>();
        Cursor cursor = db.query("Province",null,null,null,null,null,null);
        if (cursor.moveToFirst()){
            do {
                Province province = new Province();
                province.setId(cursor.getInt(cursor.getColumnIndex("id")));
                province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
                province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
                list.add(province);                
            }while (cursor.moveToNext());
        }
        if (cursor != null){
            cursor.close();
        }
        return list;
    }

    /**
     * 
     * save instance of City to database.
     * @param city
     */
    public void saveCity(City city){
        
        if (city != null){
            ContentValues values = new ContentValues();
            values.put("city_name",city.getCityName());
            values.put("city_code",city.getCityCode());
            values.put("province_id",city.getProvinceId());
            db.insert("City",null,values);
        }
    }

    /**
     * 
     * read all the cities of one province from the database
     * @param provinceId
     * @return
     */
    private List<City> loadCities(int provinceId){
        
        List<City> list = new ArrayList<City>();
        Cursor cursor = db.query("City",null,"province_id",new String[]{String.valueOf(provinceId)},null,null,null);
        if (cursor.moveToFirst()){
            do {
                City city = new City();
                city.setId(cursor.getInt(cursor.getColumnIndex("id")));
                city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
                city.setProvinceId(provinceId);
                
                list.add(city);
            }while (cursor.moveToNext());
        }
        if (cursor != null){
            cursor.close();
        }
        return list;
    }

    /**
     * 
     * save instance of Country into the database
     * @param country
     */
    public void saveCountry(Country country){
        if (country != null){
            ContentValues values = new ContentValues();
            values.put("content_name",country.getCountryName());
            values.put("content_code",country.getCountryCode());
            values.put("city_id",country.getCityId());
            db.insert("Country",null,values);
            
        }
    }
    
    public List<Country> loadCounties(int cityId){
        
        List<Country> list = new ArrayList<Country>();
        Cursor cursor = db.query("Country",null,"city_id",new String[]{String.valueOf(cityId)},null,null,null);

        if (cursor != null){
            do {

                Country country = new Country();
                country.setId(cursor.getInt(cursor.getColumnIndex("id")));
                country.setCountryName(cursor.getString(cursor.getColumnIndex("country_name")));
                country.setCountryCode(cursor.getString(cursor.getColumnIndex("country_code")));
                country.setCityId(cityId);
                list.add(country);
            }while (cursor.moveToNext());
        }
        if (cursor != null){
            cursor.close();
        }
        return list;
    }
}
