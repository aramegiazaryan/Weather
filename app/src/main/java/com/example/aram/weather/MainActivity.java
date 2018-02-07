package com.example.aram.weather;

import android.app.SearchManager;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.aram.weather.googleMapModel.modelGoogleMap;
import com.example.aram.weather.weatherModel.modelWeather;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private final String BASE_URL_GOOGLE_MAP = "https://maps.googleapis.com/";
    private final String BASE_URL_OPEN_WEATHER_MAP = "http://api.openweathermap.org/";
    private final String OPEN_WEATHER_MAP_KEY = "701c53d9ada5974da05377c6702afea4";
    private final String GOOGLE_MAP_KEY = "AIzaSyCZuClWvQyMaFUX2I5pU0oA3cxpQ2PCbqM";
    private Api clientGoogleMap;
    ListView listSearch;
    private ArrayAdapter<String> adapter;
    private Api clientOpenWeatherMap;
    private TextView tvCity;
    private TextView tvDescription;
    private TextView tvTemp;
    private TextView tvHumidity;
    private TextView tvPressure;
    private TextView tvWindSpeed;
    private RelativeLayout rlWeather;
    private char degreeSymbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         rlWeather = findViewById(R.id.rl_weather);
         tvCity = findViewById(R.id.tv_city);
         tvDescription = findViewById(R.id.tv_description);
         tvTemp = findViewById(R.id.tv_temp);
         tvHumidity = findViewById(R.id.tv_humidity);
         tvPressure = findViewById(R.id.tv_pressure);
         tvWindSpeed = findViewById(R.id.tv_wind_speed);
        listSearch = findViewById(R.id.list_search);
        listSearch.setOnItemClickListener(this);
        degreeSymbol = 0x00B0;
        clientGoogleMap = new Retrofit.Builder()
                .baseUrl(BASE_URL_GOOGLE_MAP)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(Api.class);


        clientOpenWeatherMap = new Retrofit.Builder()
                .baseUrl(BASE_URL_OPEN_WEATHER_MAP)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(Api.class);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.search_button).getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.isEmpty()) {
                    rlWeather.setVisibility(View.GONE);
                    clientGoogleMap.getCities(newText, "(cities)", GOOGLE_MAP_KEY).enqueue(new Callback<modelGoogleMap>() {

                        @Override
                        public void onResponse(Call<modelGoogleMap> call, Response<modelGoogleMap> response) {
                            if (response.body().getPredictions() != null) {
                                if (response.body().getPredictions().size() != 0) {
                                    String[] cities = new String[response.body().getPredictions().size()];
                                    listSearch.setVisibility(View.VISIBLE);
                                    for (int i = 0; i < cities.length; i++) {
                                        cities[i] = response.body().getPredictions().get(i).getDescription();
                                    }
                                    adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.row_item, R.id.tv_item, cities);
                                    listSearch.setAdapter(adapter);
                                } else {
                                    listSearch.setVisibility(View.GONE);
                                    Toast.makeText(MainActivity.this, "Not Results", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<modelGoogleMap> call, Throwable t) {
                            Toast.makeText(MainActivity.this, "Connection Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {

                    if (adapter != null && !adapter.isEmpty()) {
                        listSearch.removeAllViewsInLayout();
                        listSearch.setVisibility(View.GONE);
                        rlWeather.setVisibility(View.GONE);
                    }
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        TextView item = view.findViewById(R.id.tv_item);
        String cityTemp = item.getText().toString();
        String[] allTextCity = (cityTemp.split(","));
        String city = allTextCity[0];
        clientOpenWeatherMap.getWeather(city, OPEN_WEATHER_MAP_KEY).enqueue(new Callback<modelWeather>() {
            @Override
            public void onResponse(Call<modelWeather> call, Response<modelWeather> response) {
                       if(response.body()!=null){
                           listSearch.setVisibility(View.GONE);
                           rlWeather.setVisibility(View.VISIBLE);
                           tvCity.setText(response.body().getName());
                           tvDescription.setText(response.body().getWeather().get(0).getDescription());
                           double temp = Double.valueOf(response.body().getMain().getTemp())-273.15;
                           tvTemp.setText(String.format("%.2f", temp)+degreeSymbol+" C");
                           tvHumidity.setText(""+response.body().getMain().getHumidity()+"  %");
                           tvPressure.setText(""+response.body().getMain().getPressure()+"  hPa");
                           tvWindSpeed.setText(""+response.body().getWind().getSpeed()+"  m/s");
                       } else {
                           Toast.makeText(MainActivity.this, "There is no information for this city", Toast.LENGTH_SHORT).show();
                       }
            }

            @Override
            public void onFailure(Call<modelWeather> call, Throwable t) {
                Toast.makeText(MainActivity.this, "There is no information for this city", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
