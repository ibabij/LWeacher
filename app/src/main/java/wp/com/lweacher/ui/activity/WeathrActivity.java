package wp.com.lweacher.ui.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import wp.com.lweacher.R;
import wp.com.lweacher.gson.Weather;
import wp.com.lweacher.util.HttpUtil;
import wp.com.lweacher.util.Utility;

public class WeathrActivity extends AppCompatActivity {

    @BindView(R.id.title_city)
    TextView titleCity;
    @BindView(R.id.title_updatetime)
    TextView titleUpdatetime;
    @BindView(R.id.degree_text)
    TextView degreeText;
    @BindView(R.id.weatherinfo_text)
    TextView weatherinfoText;
    @BindView(R.id.forecast_layout)
    LinearLayout forecastLayout;
    @BindView(R.id.aqi_text)
    TextView aqiText;
    @BindView(R.id.pm25_text)
    TextView pm25Text;
    @BindView(R.id.comfort_text)
    TextView comfortText;
    @BindView(R.id.carwash_text)
    TextView carwashText;
    @BindView(R.id.sport_text)
    TextView sportText;
    @BindView(R.id.weather_layout)
    ScrollView weatherLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weathr);
        ButterKnife.bind(this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather",null);
        if (weatherString != null){
            Weather.HeWeatherBean weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        }else {
            String weatherId = getIntent().getStringExtra("weather_id");
            requestWeather(weatherId);
        }


    }

    public void requestWeather(final String weatherId){
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=ee51f77e11144421903399d6dd3baefd" ;
        HttpUtil.sendOKHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeathrActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
final String responseText = response.body().string();
                final Weather.HeWeatherBean weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.getStatus())){
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(WeathrActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(WeathrActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void showWeatherInfo(Weather.HeWeatherBean weather){
        String cityName = weather.getBasic().getCity();
        String updateTime  = weather.getBasic().getUpdate().getLoc();
        String degree = weather.getNow().getTmp()+"℃";
        String weatherInfo = weather.getNow().getCond().getTxt();
        titleCity.setText(cityName);
        titleUpdatetime.setText(updateTime);
        degreeText.setText(degree);
        weatherinfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();

        for (Weather.HeWeatherBean.DailyForecastBean forecast : weather.getDaily_forecast()){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dataText = (TextView) view.findViewById(R.id.data_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dataText.setText(forecast.getDate());
            infoText.setText(forecast.getCond().getTxt_d());
            maxText.setText(forecast.getTmp().getMax());
            minText.setText(forecast.getTmp().getMax());
            forecastLayout.addView(view);
        }

        if (weather.getAqi() != null){
            aqiText.setText(weather.getAqi().getCity().getAqi());
            pm25Text.setText(weather.getAqi().getCity().getPm25());
        }
        String comfort = "舒适度："+weather.getSuggestion().getComf().getTxt();
        String carwash = "洗车指数："+weather.getSuggestion().getCw().getTxt();
        String sport = "运动建议：："+weather.getSuggestion().getSport().getTxt();
        comfortText.setText(comfort);
        carwashText.setText(carwash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);

    }
}
