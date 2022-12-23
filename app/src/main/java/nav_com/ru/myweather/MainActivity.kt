package nav_com.ru.myweather

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import nav_com.ru.myweather.models.ForecastItem
import nav_com.ru.myweather.models.ForecastResponse
import nav_com.ru.myweather.models.WeatherResponse
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import kotlin.math.roundToInt

const val PREFS_NAME = "myWeather_prefs"
const val KEY_POSITION = "prefs.position"
const val KEY_TEMPERATURE = "prefs.temp"
const val KEY_WIND_POWER = "prefs.wind_power"
const val KEY_WIND_DIRECTION = "prefs.wind_direction"
const val KEY_PRESSURE = "prefs.pressure"
class MainActivity : AppCompatActivity() {

    private val listOfCities = arrayOf("Адлер", "Алушта", "Джанкой", "Евпатория", "Елец", "Керчь", "Москва", "Нижневартовск", "Саки", "Санкт-Петербург", "Севастополь",  "Симферополь", "Сочи", "Феодосия", "Чайковский", "Ялта")
    private val listOfCityIds = arrayOf("584243", "713513", "709334", "688105", "467978", "706524", "524894", "1497543", "2323390", "498817", "694423",  "693805", "491422", "709161", "569742", "688532")
    private var position : Int = 10

    private val sharedPrefs by lazy {  getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setVisibleContent()

        position = getSavedPosition()

        val citySelector = findViewById<Button>(R.id.citySelection)
        citySelector.text = listOfCities[position]
        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        val settings = findViewById<Button>(R.id.settings)

        swipeRefresh.setOnRefreshListener {
            setContent(position)
        }

        settings.setOnClickListener {
            intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }

        val reloadErrBtn = findViewById<Button>(R.id.reload_in_error)

        reloadErrBtn.setOnClickListener {
            setContent(position)
        }

        citySelector.setOnClickListener {
            val citiesAdapter =
                ArrayAdapter(this, android.R.layout.simple_list_item_1, listOfCities)
            AlertDialog.Builder(this)
                .setTitle("Выберите город")
                .setAdapter(citiesAdapter) { _, pos: Int ->
                    position = pos
                    setContent(position)
                }
                .show()
        }
        setContent(position)

    }

    override fun onResume() {
        setContent(getSavedPosition())
        super.onResume()
    }

    private fun setContent(
        position: Int = 10
    ){
        val citySelector = findViewById<Button>(R.id.citySelection)
        citySelector.text = listOfCities[position]
        setVisibleContent(0, 1, 0)

        val units = if (getSavedTemperature() == 0)
            "metric"
        else
            "imperial"

        val requestUrl =
            "https://api.openweathermap.org/data/2.5/weather?" +
                    "id=" + listOfCityIds[position] +
                    "&appid=ddd069d11b1e504d8268d4ee774ddd64" +
                    "&lang=ru" +
                    "&units=" + units

        val forecastUrl =
            "https://api.openweathermap.org/data/2.5/forecast?" +
                    "id=" + listOfCityIds[position] +
                    "&appid=ddd069d11b1e504d8268d4ee774ddd64" +
                    "&lang=ru" +
                    "&units=" + units

        val request = Request()
        val forecast = Request()

        savePosition(position)

        request.run(
            requestUrl,
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    setErrorCode(0)
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (response.body != null) {
                        val stringResponse = response.body!!.string()
                        val builder = GsonBuilder()
                        val gson: Gson = builder.create()

                        val weatherObject: WeatherResponse = gson.fromJson(stringResponse, WeatherResponse::class.java)

                        if (weatherObject.cod == 200) {
                            val currentTemperature = findViewById<TextView>(R.id.current_temperature)
                            val feels = findViewById<Chip>(R.id.feeling_chip)
                            val wind = findViewById<Chip>(R.id.wind_chip)
                            val pressure = findViewById<Chip>(R.id.press_chip)
                            val humidity = findViewById<Chip>(R.id.humidity_chip)
                            val weatherIcon = findViewById<ImageView>(R.id.weather_icon)

                            runOnUiThread {
                                currentTemperature.text = getTemperature(weatherObject.main.temp)
                                feels.text = getTemperature(weatherObject.main.feels_like)
                                wind.text = getWindInfo(weatherObject.wind.speed, weatherObject.wind.deg)
                                pressure.text = getPressure(weatherObject.main.pressure)
                                humidity.text = "" + weatherObject.main.humidity.toString().toFloat().roundToInt() + "%"
                                val uri: Uri = Uri.parse("android.resource://nav_com.ru.myweather/drawable/w_" + weatherObject.weather[0].icon )
                                weatherIcon.setImageURI(null)
                                weatherIcon.setImageURI(uri)

                                setVisibleContent()

                                wind.setOnClickListener {
                                    Toast.makeText(this@MainActivity, "Скорость и направление ветра", Toast.LENGTH_SHORT).show()
                                }
                                pressure.setOnClickListener {
                                    Toast.makeText(this@MainActivity, "Атмосферное давление", Toast.LENGTH_SHORT).show()
                                }
                                humidity.setOnClickListener {
                                    Toast.makeText(this@MainActivity, "Влажность", Toast.LENGTH_SHORT).show()
                                }
                                feels.setOnClickListener {
                                    Toast.makeText(this@MainActivity, "Температура \"по ощущениям\"", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            runOnUiThread {
                                setErrorCode(1, weatherObject.cod)
                            }
                        }
                    }
                }
            }
        )

        forecast.run(
            forecastUrl,
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    setErrorCode(0)
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (response.body != null) {
                        val stringResponse = response.body!!.string()
                        val builder = GsonBuilder()
                        val gson: Gson = builder.create()

                        val forecastObject: ForecastResponse = gson.fromJson(stringResponse, ForecastResponse::class.java)

                        if (forecastObject.cod == "200") {
                            runOnUiThread {
                                val listView = findViewById<ListView>(R.id.forecast_list)
                                val listOfWeathers = forecastObject.list
                                val firstItem = checkDayForecast(listOfWeathers)
                                val lastItem = forecastObject.cnt - 1
                                val forecastList = mutableListOf<Array<ForecastItem>>()
                                var index = 0
                                for (i in firstItem until listOfWeathers.size) {
                                    var tempArray : Array<ForecastItem> = emptyArray()
                                    for (j in 0..3){
                                        val currentIndex = index * 7 + i + j * 2
                                        tempArray += if (currentIndex <= lastItem)
                                            listOfWeathers[currentIndex]
                                        else if (currentIndex - 1 <= lastItem)
                                            listOfWeathers[currentIndex - 1]
                                        else
                                            break
                                    }
                                    index++
                                    if (tempArray.isNotEmpty()) {
                                        forecastList += tempArray
                                    }else
                                        break
                                }

                                val forecastAdapter = ForecastAdapter(
                                    this@MainActivity,
                                    R.layout.forecast_item,
                                    forecastList
                                )
                                listView.adapter = forecastAdapter

                            }
                        } else {
                            runOnUiThread {
                                setErrorCode(1, forecastObject.cod.toInt())
                            }
                        }
                    }
                }
            }
        )
    }

    private fun getTemperature(temp: Any) : String {
        val intTemp = temp.toString().toFloat().roundToInt()
        val ending = if (getSavedTemperature() == 0) "C" else "F"
        return if (intTemp > 0)
            "+$intTemp°$ending"
        else
            "$intTemp°$ending"
    }

    private fun getPressure(pressure: Any) : String {
        return if (getSavedPressure() == 0)
            "" + (pressure.toString().toFloat() * 0.750064).roundToInt().toString() + " мм.рт.ст"
        else
            "" + pressure.toString().toFloat().roundToInt() + " гПа"
    }

    private fun getWindInfo(speed: Any, degrees: Any) : String {
        val resultSystem = getSavedTemperature()
        var power = ""
        if (resultSystem == 0){
            if (getSavedWindPower() == 0)
                power += "" + speed.toString().toFloat().roundToInt() + " м/с, "
            else
                power += "" + String.format("%.1f", speed.toString().toFloat().roundToInt() * 2.236936) + " миль/ч, "
        } else {
            if (getSavedWindPower() == 0)
                power += "" + (speed.toString().toFloat().roundToInt() * 0.44704).roundToInt() + " м/с, "
            else
                power += "" + String.format("%.1f", speed.toString().toFloat()) + " миль/ч, "
        }

        val deg = degrees.toString().toFloat().roundToInt()

        var direction = ""
        if (getSavedWindDirection() == 0){
            when (deg) {
                in 0..23 -> direction = "С"
                in 24..66 -> direction = "СВ"
                in 67..113 -> direction = "В"
                in 114..158 -> direction = "ЮВ"
                in 159..203 -> direction = "Ю"
                in 204..248 -> direction = "ЮЗ"
                in 249..293 -> direction = "З"
                in 294..338 -> direction = "СЗ"
                in 339..360 -> direction = "С"
            }
        } else
            direction = "$deg°"

        return power + direction
    }

    private fun setVisibleContent (
        main: Int = 1,
        loading: Int = 0,
        error: Int = 0
    ) {
        val mainContent = findViewById<ConstraintLayout>(R.id.mainContent)
        val loadingContent = findViewById<ConstraintLayout>(R.id.loadingContent)
        val citySelector = findViewById<Button>(R.id.citySelection)
        val errorContent = findViewById<ConstraintLayout>(R.id.errorContent)
        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)

        when (main) {
            0 -> mainContent.visibility = View.GONE
            1 -> mainContent.visibility = View.VISIBLE
        }

        when (loading) {
            0 -> {
                loadingContent.visibility = View.GONE
                citySelector.isEnabled = true
                swipeRefresh.isRefreshing = false
            }
            1 -> {
                loadingContent.visibility = View.VISIBLE
                citySelector.isEnabled = false
            }
        }

        when (error) {
            0 -> errorContent.visibility = View.GONE
            1 -> errorContent.visibility = View.VISIBLE
        }
    }

    private fun setErrorCode(
        errCode: Int,
        cod: Int = 404
    ) {
        runOnUiThread {
            val errorText = findViewById<TextView>(R.id.error_tw)
            errorText.text = when (errCode) {
                0 -> "Нет соединения с сервером"
                1 -> "Ошибка сервера. Код: $cod"
                else -> "Неизвестная ошибка. Попробуйте позже"
            }
            setVisibleContent(0, 0, 1)
        }
    }

    private fun savePosition (theme: Int) = sharedPrefs.edit().putInt(KEY_POSITION, theme).apply()

    private fun getSavedPosition() = sharedPrefs.getInt(KEY_POSITION, 10)

    private fun getSavedTemperature() = sharedPrefs.getInt(KEY_TEMPERATURE, 0)

    private fun getSavedWindPower() = sharedPrefs.getInt(KEY_WIND_POWER, 0)

    private fun getSavedWindDirection() = sharedPrefs.getInt(KEY_WIND_DIRECTION, 0)

    private fun getSavedPressure() = sharedPrefs.getInt(KEY_PRESSURE, 0)

    private fun checkDayForecast(list : List<ForecastItem>) : Int {
        var counter = 0
        for (item in list) {
            if (isNewDayForecast(item.dt_txt)){
                return counter
            } else {
                counter++
            }
        }
        return counter
    }

    private fun isNewDayForecast(date: String) : Boolean {
        val time = date.split(" ")[1].split(":")[0].toInt()
        return time == 6
    }
}