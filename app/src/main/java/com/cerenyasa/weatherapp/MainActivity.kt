package com.cerenyasa.weatherapp

import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.cerenyasa.weatherapp.databinding.ActivityMainBinding
import com.google.android.gms.location.*
import im.delight.android.location.SimpleLocation
import org.json.JSONObject
import java.util.*
import java.util.jar.Manifest
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainBinding
    lateinit var weatherApiUrl: String
    var city: String = "Eskişehir"
    var citiesInTurkey = ArrayList<String>()
    private val dayArray = arrayOf("Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi", "Pazar")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        getCitiesData()
        getWeatherData()

    }

    private fun getCitiesData() {
        var citiesUrl = "https://gist.githubusercontent.com/ozdemirburak/4821a26db048cc0972c1beee48a408de/raw/4754e5f9d09dade2e6c461d7e960e13ef38eaa88/cities_of_turkey.json"
        var cityRequest = JsonArrayRequest(Request.Method.GET, citiesUrl, null, { response ->
            for (i in 0..response.length() - 1) {
                citiesInTurkey.add((response.get(i) as JSONObject).getString("name"))
            }
            var arrayAdapter = ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, citiesInTurkey)
            arrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item)
            mainBinding.spinner.adapter = arrayAdapter
            mainBinding.spinner.setSelection(citiesInTurkey.indexOf(city))

            mainBinding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    city = citiesInTurkey[position]
                    getWeatherData()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    //Log.e("nothing selected", " ")
                }

            }
        }, { //Log.e("cities", "Error")
        })
        MySingleton.getInstance(this).addToRequestQueue(cityRequest)
    }

    private fun getWeatherData() {

        weatherApiUrl = "https://api.openweathermap.org/data/2.5/forecast?q=$city&units=metric&cnt=3&lang=tr&appid=5ee6d1e094facd39beac6f9b2b357d57"

        var weatherRequest = JsonObjectRequest(Request.Method.GET, weatherApiUrl, null,
            { response ->
                val currentCity = response.getJSONObject("city").getString("name")
                val threeDaysList = response.getJSONArray("list")
                val todayTemp = threeDaysList.getJSONObject(0).getJSONObject("main").getDouble("temp")
                val todayMaxTemp = threeDaysList.getJSONObject(0).getJSONObject("main").getDouble("temp_max")
                val todayMinTemp = threeDaysList.getJSONObject(0).getJSONObject("main").getDouble("temp_min")
                val todayDesc = threeDaysList.getJSONObject(0).getJSONArray("weather").getJSONObject(0).getString("description")
                val todayIcon = threeDaysList.getJSONObject(0).getJSONArray("weather").getJSONObject(0).getString("icon")
                val tomorrowMaxTemp = threeDaysList.getJSONObject(1).getJSONObject("main").getDouble("temp_max")
                val tomorrowMinTemp = threeDaysList.getJSONObject(1).getJSONObject("main").getDouble("temp_min")
                val twoDaysLaterMaxTemp = threeDaysList.getJSONObject(2).getJSONObject("main").getDouble("temp_max")
                val twoDaysLaterMinTemp = threeDaysList.getJSONObject(2).getJSONObject("main").getDouble("temp_min")

                val dataArray = arrayOf(todayTemp, todayMinTemp, todayMaxTemp, tomorrowMinTemp, tomorrowMaxTemp, twoDaysLaterMinTemp, twoDaysLaterMaxTemp)

                setData(currentCity, todayIcon, todayDesc, dataArray)
            }, { error -> Toast.makeText(this, "Request error $error", Toast.LENGTH_LONG).show() })


        MySingleton.getInstance(this).addToRequestQueue(weatherRequest)
    }

    private fun setData(currentCity: String, todayIcon: String, todayDesc: String, dataArray: Array<Double>) {
        val animation = AnimationUtils.loadAnimation(this, R.anim.move_animation)
        mainBinding.icon.animation = animation
        mainBinding.icon.setImageResource(getIcon(todayIcon))
        mainBinding.todayTemp.text = "${Math.ceil(dataArray[0]).toInt()} °C"
        mainBinding.todayDesc.text = todayDesc

        setRecyclerView(dataArray)

    }

    fun setRecyclerView(dataArray: Array<Double>) {
        val todayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
        var tomorrowIndex: Int
        var twoDaysAfterIndex: Int
        if (todayIndex == 6) tomorrowIndex = 0
        else tomorrowIndex = todayIndex + 1
        if (todayIndex == 5) twoDaysAfterIndex = 0
        else if (todayIndex == 6) twoDaysAfterIndex = 1
        else twoDaysAfterIndex = todayIndex + 2

        val dayNamesList = arrayOf("Bugün", dayArray[tomorrowIndex], dayArray[twoDaysAfterIndex])
        val tempList = arrayOf(arrayOf(dataArray[1], dataArray[2]), arrayOf(dataArray[3], dataArray[4]), arrayOf(dataArray[5], dataArray[6]))

        val adapter = WeatherRecyclerAdapter(dayNamesList, tempList)
        mainBinding.rvNextTemp.adapter = adapter

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mainBinding.rvNextTemp.layoutManager = layoutManager
    }

    private fun getIcon(todayIcon: String): Int {
        var icon: Int? = null
        var backgroundMode = R.drawable.day_linear_background
        when (todayIcon) {
            "01d" -> icon = R.drawable.day
            "01n" -> icon = R.drawable.night
            "02d" -> icon = R.drawable.cloudy_day
            "02n" -> icon = R.drawable.cloudy_night
            "03d" -> icon = R.drawable.cloudy
            "03n" -> icon = R.drawable.cloudy
            "04d" -> icon = R.drawable.cloudy
            "04n" -> icon = R.drawable.cloudy
            "09d" -> icon = R.drawable.rainy1
            "09n" -> icon = R.drawable.rainy1
            "10d" -> icon = R.drawable.rainy7
            "10n" -> icon = R.drawable.rainy1
            "11d" -> icon = R.drawable.thunder
            "11n" -> icon = R.drawable.thunder
            "13d" -> icon = R.drawable.snowy6
            "13n" -> icon = R.drawable.snowy6
            "50d" -> icon = R.drawable.fog
            "50n" -> icon = R.drawable.fog
            else -> icon = R.drawable.day
        }

        if (todayIcon.last() == 'n') backgroundMode = R.drawable.night_linear_background
        else backgroundMode = R.drawable.day_linear_background

        mainBinding.constraintLayout.setBackgroundResource(backgroundMode)
        return icon!!
    }


}