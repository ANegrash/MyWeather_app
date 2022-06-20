package nav_com.ru.myweather

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import nav_com.ru.myweather.models.WeatherResponse
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import kotlin.math.roundToInt

const val PREFS_NAME = "myWeather_prefs"
const val KEY_POSITION = "prefs.position"
class MainActivity : AppCompatActivity() {

    private val listOfCities = arrayOf("Адлер", "Алушта", "Джанкой", "Евпатория", "Елец", "Керчь", "Москва", "Нижневартовск", "Саки", "Санкт-Петербург", "Севастополь",  "Симферополь", "Сочи", "Феодосия", "Ялта")
    private val listOfCityIds = arrayOf("584243", "713513", "709334", "688105", "467978", "706524", "524894", "1497543", "2323390", "498817", "694423",  "693805", "491422", "709161", "688532")
    private var position : Int = 10

    private val sharedPrefs by lazy {  getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setVisibleContent()

        position = getSavedPosition()

        val citySelector = findViewById<Button>(R.id.citySelection)
        citySelector.text = listOfCities[position]
        val reloadBtn = findViewById<Button>(R.id.reload)

        reloadBtn.setOnClickListener {
            setContent(position)
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

    private fun setContent(
        position: Int = 10
    ){
        val citySelector = findViewById<Button>(R.id.citySelection)
        citySelector.text = listOfCities[position]
        setVisibleContent(0, 1, 0)

        val requestUrl =
            "https://api.openweathermap.org/data/2.5/weather?id=" + listOfCityIds[position] + "&appid=ddd069d11b1e504d8268d4ee774ddd64&lang=ru&units=metric"

        val request = Request()

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
                            val description = findViewById<TextView>(R.id.description)
                            val feels = findViewById<TextView>(R.id.feels_like)
                            val wind = findViewById<TextView>(R.id.wind_info)
                            val pressure = findViewById<TextView>(R.id.pressure)
                            val humidity = findViewById<TextView>(R.id.humidity)
                            val weatherIcon = findViewById<ImageView>(R.id.weather_icon)

                            runOnUiThread {
                                currentTemperature.text = getTemperature(weatherObject.main.temp)
                                description.text = weatherObject.weather[0].description.toString()
                                feels.text = "Ощущается как " + getTemperature(weatherObject.main.feels_like)
                                wind.text = getWindInfo(weatherObject.wind.speed, weatherObject.wind.deg)
                                pressure.text = getPressure(weatherObject.main.pressure)
                                humidity.text = "Влажность: " + weatherObject.main.humidity.toString().toFloat().roundToInt() + "%"
                                val uri: Uri = Uri.parse("android.resource://nav_com.ru.myweather/drawable/w_" + weatherObject.weather[0].icon )
                                weatherIcon.setImageURI(null)
                                weatherIcon.setImageURI(uri)

                                setVisibleContent()
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
    }

    private fun getTemperature(temp: Any) : String {
        val intTemp = temp.toString().toFloat().roundToInt()
        return if (intTemp > 0)
            "+$intTemp°C"
        else
            "$intTemp°C"
    }

    private fun getPressure(pressure: Any) : String {
        return "Давление: " + (pressure.toString().toFloat() * 0.750064).roundToInt().toString() + " мм. рт. ст."
    }

    private fun getWindInfo(speed: Any, degrees: Any) : String {
        val result = "Ветер: " + speed.toString().toFloat().roundToInt() + " м/с, "

        val deg = degrees.toString().toFloat().roundToInt()
        var direction = ""
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

        return result + direction
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

        when (main) {
            0 -> mainContent.visibility = View.GONE
            1 -> mainContent.visibility = View.VISIBLE
        }

        when (loading) {
            0 -> {
                loadingContent.visibility = View.GONE
                citySelector.isEnabled = true
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
}