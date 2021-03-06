package com.app.coolweather.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.coolweather.R;
import com.app.coolweather.db.CoolWeatherDB;
import com.app.coolweather.model.City;
import com.app.coolweather.model.Country;
import com.app.coolweather.model.Province;
import com.app.coolweather.util.HttpCallbackListener;
import com.app.coolweather.util.HttpUtil;
import com.app.coolweather.util.Utility;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/8/22.
 */

public class ChooseActivity extends Activity {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTRY = 2;

    private ProgressDialog progressDialog;

    //activity title
    private TextView titleText;

    private ListView listView;

    private ArrayAdapter<String > adapter;

    private CoolWeatherDB coolWeatherDB;

    private List<String> datalist = new ArrayList<String>();

    /**
     * province list
     */
    private List<Province> provinceList;

    /**
     * city list
     */
    private List<City> cityList;

    /**
     * country list
     */
    private List<Country> countryList;

    /**
     * province chosen
     */
    private Province selectedProvince;

    /**
     * city chosen
     */
    private City selectedCity;

    /**
     * country chosen
     */
    private Country selectedCountry;

    /**
     * chosen level
     */
    private int currentLevel;


    private boolean isFromWeatherActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this must before setContentView.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_area);


        isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity",false);
        //sharedPreference
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean("city_selected",false) && !isFromWeatherActivity){
            Intent intent = new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        listView = (ListView) findViewById(R.id.list_view);
        titleText = (TextView) findViewById(R.id.title_text);

        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,datalist);
        listView.setAdapter(adapter);

        coolWeatherDB = CoolWeatherDB.getInstance(this);


        //此步骤是异步事件，当有点击事件才会执行
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                }else if (currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);
                    queryCountries();
                }else if (currentLevel == LEVEL_COUNTRY){
                    String countryCode = countryList.get(position).getCountryCode();
                    Intent intent = new Intent(ChooseActivity.this,WeatherActivity.class);
                    intent.putExtra("country_code",countryCode);
                    startActivity(intent);
                    finish();
                }
            }
        });
        queryProvinces();

    }

    private void queryProvinces(){
        Log.d("queryProvinces111","in queryProvinces");
        provinceList = coolWeatherDB.loadProvince();
        if (provinceList.size() > 0 ){
            datalist.clear();
            for (Province province:provinceList){
                datalist.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        }else {
            queryFromServer(null,"province");
        }
    }

    private void queryCities(){
        cityList = coolWeatherDB.loadCities(selectedProvince.getId());
        if (cityList.size() > 0 ){
            datalist.clear();
            for (City city:cityList){
                datalist.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        }else {
            queryFromServer(selectedProvince.getProvinceCode(),"city");
        }

    }

    /**
     *
     * check the countries of selected city,first in the DB,if not found check through the server
     */
    private void queryCountries(){
        countryList = coolWeatherDB.loadCounties(selectedCity.getId());
        if (countryList.size() > 0 ){
            datalist.clear();
            for (Country country: countryList){
                datalist.add(country.getCountryName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTRY;
        }else {
            queryFromServer(selectedCity.getCityCode(),"country");
        }
    }

    /**
     *
     * query data from server with code of city
     * @param code:provinceID or cityID or countryID
     * @param type:
     */
    private void queryFromServer(final String code,final String type){
        String address;
        if (!TextUtils.isEmpty(code)){
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
        }else {
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }

        showProgressDialog();

        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public int hashCode() {
                return super.hashCode();
            }

            @Override
            public void onFinish(String response) {
                boolean result = false;
                if ("province".equals(type)){
                    result = Utility.handleProvincesResponse(coolWeatherDB,response);
                }else if ("city".equals(type)){
                    result = Utility.handleCitiesResponse(coolWeatherDB,response,selectedProvince.getId());
                }else if ("country".equals(type)){
                    result = Utility.handleCountriesResponse(coolWeatherDB,response,selectedCity.getId());
                }

                if (result){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("country".equals(type)){
                                queryCountries();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseActivity.this,"load failed",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    private void showProgressDialog(){
        if (progressDialog == null){
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("loading......");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog(){
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {

        if (currentLevel == LEVEL_COUNTRY){
            queryCities();
        }else if (currentLevel == LEVEL_CITY){
            queryProvinces();
        }else {
            if (isFromWeatherActivity){
                Intent intent = new Intent(this,WeatherActivity.class);
                startActivity(intent);
            }
            finish();
        }
    }
}