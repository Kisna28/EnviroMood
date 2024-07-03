package com.example.enviromood


import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.VideoView
import androidx.appcompat.widget.SearchView
import com.example.enviromood.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//Api Key :  3e2389280f23fecafca4a2662de5278d
class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        fetchWeatherData("Gandhinagar")
        SearchCity()
    }

    private fun SearchCity() {
        val searchView = binding.search
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchWeatherData(query)
                    closeKeyboard()  // Close the keyboard after searching
                }
                return true

            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }
    private fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
    private fun fetchWeatherData(cityName: String) {

        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)

        val response = retrofit.getWeatherData(
            cityName, "3e2389280f23fecafca4a2662de5278d", "matric"
        )

        response.enqueue(object : Callback<WeatherApp?> {
            override fun onResponse(call: Call<WeatherApp?>, response: Response<WeatherApp?>) {

                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    val temperatureInKelvin = responseBody.main.temp
                    val temperatureInCelsius = temperatureInKelvin - 273.15
                    val temperatureText =
                        String.format("%.1f", temperatureInCelsius) // Format to one decimal place
                    val humidity = responseBody.main.humidity
                    val windspeed = responseBody.wind.speed
                    val sunRise = responseBody.sys.sunrise.toLong()
                    val sunSet = responseBody.sys.sunset.toLong()
                   // val seaLevel = responseBody.sys.country
                    val condition = responseBody.weather.firstOrNull()?.main ?: "unknown"

                    val MaxtemperatureInKelvin = responseBody.main.temp_max
                    val MaxtemperatureInCelsius = MaxtemperatureInKelvin - 273.15
                    val MaxtemperatureText = String.format(
                        "%.1f",
                        MaxtemperatureInCelsius
                    ) // Format to one decimal place

                    val MintemperatureInKelvin = responseBody.main.temp_min
                    val MintemperatureInCelsius = MintemperatureInKelvin - 273.15
                    val MintemperatureText = String.format(
                        "%.1f",
                        MintemperatureInCelsius
                    ) // Format to one decimal place


                    binding.temp.text = "$temperatureText °C"
                    binding.weather.text = condition
                    binding.maxtemp.text = "Max Temp: $MaxtemperatureText °C"
                    binding.mintemp.text = "Min Temp: $MintemperatureText °C"
                    binding.humidity.text = "$humidity %"
                    binding.windspeed.text = "$windspeed m/s"
                    binding.sunrise.text = "${time((sunRise))}"
                    binding.sunset.text = "${time((sunSet))}"
                   // binding.sea.text = "$seaLevel "
                   // binding.conditi.text = condition
                    binding.day.text = dayName(System.currentTimeMillis())
                    binding.date.text = date()
                    binding.cityname.text = "$cityName"

                    changeImages(condition)
                }
            }

            override fun onFailure(call: Call<WeatherApp?>, t: Throwable) {
                Log.d("Main Activity", "OnFailure " + t.message)


            }
        })
    }

    private fun changeImages(conditions: String) {
        val weatherVideoView  = binding.weatherVideo
        when (conditions) {
            "Clear Sky", "Sunny", "Clear" -> {
                binding.root.setBackgroundResource(R.drawable.main)
                binding.lottie.setAnimation(R.raw.sun)
                weatherVideoView.visibility = View.GONE
                //playWeatherVideo(weatherVideoView, R.raw.clere)

            }

            "Partly Clouds", "Clouds", "Overcast", "Mist", "Foggy" -> {
                binding.root.setBackgroundResource(R.drawable.cloud)
                binding.lottie.setAnimation(R.raw.cloud)
                weatherVideoView.visibility = View.GONE
            }

            "Light Rain", "Drizzle", "Moderate Rain", "Showers", "Heavy Rain", "Rain" -> {
                binding.root.setBackgroundResource(R.drawable.rain)
                binding.lottie.setAnimation(R.raw.rain)
                weatherVideoView.visibility = View.GONE
             //   playWeatherVideo(weatherVideoView, R.raw.raining)

            }

            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" -> {
                binding.root.setBackgroundResource(R.drawable.snow)
                binding.lottie.setAnimation(R.raw.snow)
                weatherVideoView.visibility = View.GONE
            }

            else -> {
                binding.root.setBackgroundResource(R.drawable.main)
                binding.lottie.setAnimation(R.raw.sun)
                weatherVideoView.visibility = View.GONE
            }

        }
        binding.lottie.playAnimation()
    }

    private fun playRainVideo(videoView: VideoView) {
        val uri = Uri.parse("android.resource://${this.packageName}/${R.raw.raining}")
        videoView.setVideoURI(uri)
        videoView.start()
        videoView.visibility = View.VISIBLE
        videoView.setOnPreparedListener { mp ->
            mp.isLooping = true
        }

    }
    private fun playWeatherVideo(videoView: VideoView, videoResId: Int) {
        val uri = Uri.parse("android.resource://${packageName}/$videoResId")
        videoView.setVideoURI(uri)
        videoView.visibility = View.VISIBLE
        videoView.setOnPreparedListener { mp ->
            mp.isLooping = true
            videoView.start()
        }
    }


        fun date(): String {
            val sdf = SimpleDateFormat("dd MMMM YYYY", Locale.getDefault())
            return sdf.format((Date()))
        }

        fun time(timeStamp: Long): String {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format((Date(timeStamp * 1000)))
        }


        fun dayName(timeStamp: Long): String {
            val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
            return sdf.format((Date()))
        }

}



