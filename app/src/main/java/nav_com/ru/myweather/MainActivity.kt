package nav_com.ru.myweather

import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.my.target.ads.MyTargetView
import com.my.target.ads.MyTargetView.MyTargetViewListener
import com.my.target.common.MyTargetConfig
import com.my.target.common.MyTargetManager
import nav_com.ru.myweather.models.ForecastItem
import nav_com.ru.myweather.models.ForecastResponse
import nav_com.ru.myweather.models.Weather
import nav_com.ru.myweather.models.WeatherResponse
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.time.ZoneOffset
import java.util.*
import kotlin.math.roundToInt

const val SLOT_ID = 1333756
const val IS_TEST = false
const val PREFS_NAME = "myWeather_prefs"
const val KEY_POSITION = "prefs.position"
const val KEY_TEMPERATURE = "prefs.temp"
const val KEY_WIND_POWER = "prefs.wind_power"
const val KEY_WIND_DIRECTION = "prefs.wind_direction"
const val KEY_PRESSURE = "prefs.pressure"
const val KEY_FAVORITE_LIST = "prefs.favorite_list"
const val KEY_CURRENT_CITY = "prefs.current_city"
const val KEY_CHIPS = "prefs.chips"
const val KEY_THEME = "prefs.theme"
class MainActivity : AppCompatActivity() {

    private val listOfCities = arrayOf("Адлер", "Алушта", "Джанкой", "Евпатория", "Елец", "Керчь", "Москва", "Нижневартовск", "Саки", "Санкт-Петербург", "Севастополь",  "Симферополь", "Сочи", "Феодосия", "Чайковский", "Ялта")
    private val listOfCitylls = arrayOf("43.4253834, 39.9237036", "44.677112, 34.4095393", "45.7093755, 34.3899131", "45.1907635, 33.3679049", "52.6219865, 38.5003298", "45.3534002, 36.4538645", "55.750446, 37.617493", "60.9339411, 76.5814274", "45.1319466, 33.6001281", "59.938732, 30.316229", "44.6054434, 33.5220842",  "44.9521459, 34.1024858", "43.5854823, 39.723109", "45.033669, 35.3753628", "56.7787468, 54.1500704", "44.49707, 34.158688")
    private var position : Int = 0

    private val usedHours: Array<Int> = arrayOf(6, 12, 18, 21)

    private lateinit var adView: MyTargetView

    private val sharedPrefs by lazy {  getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_MyWeather)
        actionBar?.hide()
        window.statusBarColor = resources.getColor(R.color.dark_mv)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            when (getSavedTheme()){
                0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }

        position = getSavedPosition()
        if (position > -1){
            saveCurrentCity(listOfCities[position])
            saveFavorites("{\"" + listOfCities[position] + "\":\"" + listOfCitylls[position] + "\"}")
            savePosition(-1)
        }
    }

    override fun onBackPressed() {
        finishAffinity()
    }

    override fun onResume() {
        setAll()
        super.onResume()
    }

    override fun onDestroy() {
        adView.destroy()
        super.onDestroy()
    }

    private fun setAll() {
        val builder = GsonBuilder()
        val gson: Gson = builder.create()
        val favoritesMap: MutableMap<String, String> = gson.fromJson(getSavedFavorites(), object : TypeToken<MutableMap<String, String>>() {}.type)

        if (favoritesMap.isEmpty()) {
            intent = Intent(this, SelectCity::class.java)
            startActivity(intent)
        }
        else {
            setVisibleContent()

            val citySelector = findViewById<Button>(R.id.citySelection)
            val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
            val settings = findViewById<ImageButton>(R.id.settings)

            swipeRefresh.setOnRefreshListener {
                setContent()
            }

            settings.setOnClickListener {
                intent = Intent(this, Settings::class.java)
                startActivity(intent)
            }

            val reloadErr = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayoutErr)

            reloadErr.setOnRefreshListener {
                setContent()
            }

            citySelector.setOnClickListener {
                intent = Intent(this, SelectCity::class.java)
                startActivity(intent)
            }
            setContent()
        }
    }

    private fun setContent() {
        val currentCity = getCurrentCity().toString()
        val citySelector = findViewById<Button>(R.id.citySelection)
        citySelector.text = currentCity
        setVisibleContent(0, 1, 0)

        val cl = findViewById<ConstraintLayout>(R.id.constraintLayoutBanner)

        val myTargetConfig = MyTargetConfig.Builder()
            //.withTestDevices("5e52c264-be03-444f-8f37-d828c0c53279")
            .build()
        MyTargetManager.setSdkConfig(myTargetConfig)

        MyTargetManager.setDebugMode(IS_TEST)
        adView = MyTargetView(this)
        adView.setSlotId(SLOT_ID)

        adView.listener = object : MyTargetViewListener {
            override fun onLoad(myTargetView: MyTargetView) {
                cl.addView(adView)
            }
            override fun onNoAd(reason: String, myTargetView: MyTargetView) {}
            override fun onShow(myTargetView: MyTargetView) {}
            override fun onClick(myTargetView: MyTargetView) {}
        }
        adView.load()

        val ll = getCurrentLL(currentCity)

        val lang : String = if (Locale.getDefault().language == "en") "en" else "ru"

        val units = if (getSavedTemperature() == 0)
            "metric"
        else
            "imperial"

        val requestUrl =
            "https://api.openweathermap.org/data/2.5/weather?$ll" +
                    "&appid=ddd069d11b1e504d8268d4ee774ddd64" +
                    "&lang=$lang" +
                    "&units=" + units

        val forecastUrl =
            "https://api.openweathermap.org/data/2.5/forecast?$ll" +
                    "&appid=ddd069d11b1e504d8268d4ee774ddd64" +
                    "&lang=$lang" +
                    "&units=" + units

        val request = Request()
        val forecast = Request()

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

                            var savedChips = getSavedChips().orEmpty()
                            if (savedChips.isEmpty()) {
                                savedChips = "11110000"
                                saveChips(savedChips)
                            }

                            val currentTemperature = findViewById<TextView>(R.id.current_temperature)
                            val wind = findViewById<Chip>(R.id.wind_chip)
                            val pressure = findViewById<Chip>(R.id.press_chip)
                            val humidity = findViewById<Chip>(R.id.humidity_chip)
                            val feels = findViewById<Chip>(R.id.feeling_chip)
                            val cloudiness = findViewById<Chip>(R.id.cloudiness_chip)
                            val sunrise = findViewById<Chip>(R.id.sunrise_chip)
                            val sunset = findViewById<Chip>(R.id.sunset_chip)
                            val visibility = findViewById<Chip>(R.id.visibility_chip)
                            val currentWeather = findViewById<ConstraintLayout>(R.id.constraintLayout2)

                            wind.visibility = if (checkChip(savedChips, 0)) View.VISIBLE else View.GONE
                            pressure.visibility = if (checkChip(savedChips, 1)) View.VISIBLE else View.GONE
                            humidity.visibility = if (checkChip(savedChips, 2)) View.VISIBLE else View.GONE
                            feels.visibility = if (checkChip(savedChips, 3)) View.VISIBLE else View.GONE
                            cloudiness.visibility = if (checkChip(savedChips, 4)) View.VISIBLE else View.GONE
                            sunrise.visibility = if (checkChip(savedChips, 5)) View.VISIBLE else View.GONE
                            sunset.visibility = if (checkChip(savedChips, 6)) View.VISIBLE else View.GONE
                            visibility.visibility = if (checkChip(savedChips, 7)) View.VISIBLE else View.GONE

                            if (savedChips == "00000000")
                                currentWeather.visibility = View.GONE
                            else
                                currentWeather.visibility = View.VISIBLE

                            val weatherIcon = findViewById<ImageView>(R.id.weather_icon)

                            runOnUiThread {
                                currentTemperature.text = getTemperature(weatherObject.main.temp)
                                feels.text = getTemperature(weatherObject.main.feels_like)
                                wind.text = getWindInfo(weatherObject.wind.speed, weatherObject.wind.deg)
                                pressure.text = getPressure(weatherObject.main.pressure)
                                val humidityText = weatherObject.main.humidity.toString().toFloat().roundToInt().toString() + "%"
                                humidity.text = humidityText
                                val cloudinessText = weatherObject.clouds.all.toInt().toString() + "%"
                                cloudiness.text = cloudinessText
                                visibility.text = getVisibility(weatherObject.visibility)
                                sunrise.text = getSunTime(weatherObject.sys.sunrise, weatherObject.timezone)
                                sunset.text = getSunTime(weatherObject.sys.sunset, weatherObject.timezone)
                                val uri: Uri = Uri.parse("android.resource://nav_com.ru.myweather/drawable/w_" + weatherObject.weather[0].icon )
                                weatherIcon.setImageURI(null)
                                weatherIcon.setImageURI(uri)

                                setVisibleContent()

                                weatherIcon.setOnClickListener {
                                    Toast.makeText(this@MainActivity, weatherObject.weather[0].description.toString(), Toast.LENGTH_SHORT).show()
                                }
                                wind.setOnClickListener {
                                    Toast.makeText(this@MainActivity, getString(R.string.toast_wind), Toast.LENGTH_SHORT).show()
                                }
                                pressure.setOnClickListener {
                                    Toast.makeText(this@MainActivity, getString(R.string.settings_pressure_title), Toast.LENGTH_SHORT).show()
                                }
                                humidity.setOnClickListener {
                                    Toast.makeText(this@MainActivity, getString(R.string.humidity), Toast.LENGTH_SHORT).show()
                                }
                                feels.setOnClickListener {
                                    Toast.makeText(this@MainActivity, getString(R.string.toast_feels), Toast.LENGTH_SHORT).show()
                                }
                                cloudiness.setOnClickListener {
                                    Toast.makeText(this@MainActivity, getString(R.string.toast_cloudiness), Toast.LENGTH_SHORT).show()
                                }
                                visibility.setOnClickListener {
                                    Toast.makeText(this@MainActivity, getString(R.string.toast_visibility), Toast.LENGTH_SHORT).show()
                                }
                                sunrise.setOnClickListener {
                                    Toast.makeText(this@MainActivity, getString(R.string.toast_sunrise), Toast.LENGTH_SHORT).show()
                                }
                                sunset.setOnClickListener {
                                    Toast.makeText(this@MainActivity, getString(R.string.toast_sunset), Toast.LENGTH_SHORT).show()
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
                        val stringResponse = response.body.string()
                        val builder = GsonBuilder()
                        val gson: Gson = builder.create()

                        val forecastObject: ForecastResponse = gson.fromJson(stringResponse, ForecastResponse::class.java)

                        if (forecastObject.cod == "200") {
                            runOnUiThread {
                                val listView = findViewById<ListView>(R.id.forecast_list)
                                val listOfWeathers = forecastObject.list
                                val forecastList = mutableListOf<ForecastItem>()

                                val firstWeatherItemHour = getItemHour(listOfWeathers[0])
                                if (firstWeatherItemHour > 6)
                                    forecastList += ForecastItem(null, null, listOf(Weather(0, "", "", "")), null, null, null, null, null, null, null, listOfWeathers[0].dt_txt)
                                if (firstWeatherItemHour > 12)
                                    forecastList += ForecastItem(null, null, listOf(Weather(0, "", "", "")), null, null, null, null, null, null, null, listOfWeathers[0].dt_txt)
                                if (firstWeatherItemHour > 18)
                                    forecastList += ForecastItem(null, null, listOf(Weather(0, "", "", "")), null, null, null, null, null, null, null, listOfWeathers[0].dt_txt)

                                for (weatherItem in listOfWeathers)
                                    if (getItemHour(weatherItem) in usedHours)
                                        forecastList += weatherItem

                                val forecastSize = forecastList.size % 4
                                if (forecastList.size % 4 > 0)
                                    for (i in 1..forecastSize)
                                        forecastList += ForecastItem(null, null, listOf(Weather(0, "", "", "")), null, null, null, null, null, null, null, "")

                                val finalForecastList = mutableListOf<Array<ForecastItem>>()

                                val counter = (forecastList.size / 4) - 1
                                for (i in 0..counter) {
                                    finalForecastList += arrayOf(
                                        forecastList[i * 4], forecastList[i * 4 + 1],forecastList[i * 4 + 2],forecastList[i * 4 + 3]
                                    )
                                }

                                val forecastAdapter = ForecastAdapter(
                                    this@MainActivity,
                                    R.layout.forecast_item,
                                    finalForecastList
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

    private fun checkChip(chips: String, find: Int) : Boolean {
        return chips[find] == '1'
    }

    private fun getTemperature(temp: Any) : String {
        val intTemp = temp.toString().toFloat().roundToInt()
        return if (intTemp > 0)
            "+$intTemp°"
        else
            "$intTemp°"
    }

    private fun getPressure(pressure: Any) : String {
        return if (getSavedPressure() == 0)
            "" + (pressure.toString().toFloat() * 0.750064).roundToInt().toString() + " " + getString(R.string.pressure_mmrtst)
        else
            "" + pressure.toString().toFloat().roundToInt() + " " + getString(R.string.pressure_hpa)
    }

    private fun getWindInfo(speed: Any, degrees: Any) : String {
        val resultSystem = getSavedTemperature()
        val power = if (resultSystem == 0){
            if (getSavedWindPower() == 0)
                "" + speed.toString().toFloat().roundToInt() + " " + getString(R.string.windPower_ms) + ", "
            else
                "" + String.format("%.1f", speed.toString().toFloat().roundToInt() * 2.236936) + " " + getString(R.string.windPower_milesh) + ", "
        } else {
            if (getSavedWindPower() == 0)
                "" + (speed.toString().toFloat().roundToInt() * 0.44704).roundToInt() + " " + getString(R.string.windPower_ms) + ", "
            else
                "" + String.format("%.1f", speed.toString().toFloat()) + " " + getString(R.string.windPower_milesh) + ", "
        }

        val deg = degrees.toString().toFloat().roundToInt()

        var direction = ""
        if (getSavedWindDirection() == 0){
            when (deg) {
                in 0..23 -> direction = getString(R.string.direction_N)
                in 24..66 -> direction = getString(R.string.direction_N) + getString(R.string.direction_E)
                in 67..113 -> direction = getString(R.string.direction_E)
                in 114..158 -> direction = getString(R.string.direction_S) + getString(R.string.direction_E)
                in 159..203 -> direction = getString(R.string.direction_S)
                in 204..248 -> direction = getString(R.string.direction_S) + getString(R.string.direction_W)
                in 249..293 -> direction = getString(R.string.direction_W)
                in 294..338 -> direction = getString(R.string.direction_N) + getString(R.string.direction_W)
                in 339..360 -> direction = getString(R.string.direction_N)
            }
        } else
            direction = "$deg°"

        return power + direction
    }

    private fun getVisibility (distance: Float) : String {
        val km = distance.toInt() / 1000
        val meters = ((distance - km * 1000) / 100).roundToInt()
        return if (km > 0)
            if (meters > 0)
                "$km,$meters км"
            else
                "$km км"
        else
            "" + (distance - km * 1000).toInt() + " м"
    }

    private fun getSunTime (unix : Long, zone : Int) : String {
        val instant = java.time.Instant.ofEpochSecond(unix)
        val offset = ZoneOffset.ofTotalSeconds(zone)
        val withOffset = instant.atOffset(offset)
        val sunTimeArray = java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(withOffset).toString().split("T")[1].split(":")
        return sunTimeArray[0] + ":" + sunTimeArray[1]
    }

    private fun setVisibleContent (
        main: Int = 1,
        loading: Int = 0,
        error: Int = 0
    ) {
        val mainContent = findViewById<ConstraintLayout>(R.id.mainContent)
        val loadingContent = findViewById<ConstraintLayout>(R.id.loadingContent)
        val errorContent = findViewById<ConstraintLayout>(R.id.errorContent)
        val swipeRefresh = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        val swipeRefreshErr = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayoutErr)
        val imageView = findViewById<ImageView>(R.id.loadingImage)
        imageView.setBackgroundResource(R.drawable.loading)

        val animationLoading : AnimationDrawable = imageView.background as AnimationDrawable

        swipeRefreshErr.isRefreshing = false
        when (main) {
            0 -> mainContent.visibility = View.GONE
            1 -> mainContent.visibility = View.VISIBLE
        }

        when (loading) {
            0 -> {
                animationLoading.stop()
                loadingContent.visibility = View.GONE
                swipeRefresh.isRefreshing = false
            }
            1 -> {
                animationLoading.start()
                loadingContent.visibility = View.VISIBLE
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
                0 -> getString(R.string.error_no_connection)
                1 -> getString(R.string.error_server) + cod
                else -> getString(R.string.error_unknown)
            }
            setVisibleContent(0, 0, 1)
        }
    }

    private fun getCurrentLL(city: String) : String {
        val builder = GsonBuilder()
        val gson: Gson = builder.create()
        val favoritesMap: MutableMap<String, String> = gson.fromJson(getSavedFavorites(), object : TypeToken<MutableMap<String, String>>() {}.type)
        return "lat=" + favoritesMap[city].toString().split(", ")[0] + "&lon=" + favoritesMap[city].toString().split(", ")[1]
    }

    private fun saveFavorites(fav: String) = sharedPrefs.edit().putString(KEY_FAVORITE_LIST, fav).apply()

    private fun getSavedFavorites() = sharedPrefs.getString(KEY_FAVORITE_LIST, "{}")

    private fun getSavedTheme() = sharedPrefs.getInt(KEY_THEME, 0)

    private fun saveCurrentCity (city: String) = sharedPrefs.edit().putString(KEY_CURRENT_CITY, city).apply()

    private fun getCurrentCity() = sharedPrefs.getString(KEY_CURRENT_CITY, "")

    private fun savePosition (position: Int) = sharedPrefs.edit().putInt(KEY_POSITION, position).apply()

    private fun getSavedPosition() = sharedPrefs.getInt(KEY_POSITION, -2)

    private fun getSavedTemperature() = sharedPrefs.getInt(KEY_TEMPERATURE, 0)

    private fun getSavedWindPower() = sharedPrefs.getInt(KEY_WIND_POWER, 0)

    private fun getSavedWindDirection() = sharedPrefs.getInt(KEY_WIND_DIRECTION, 0)

    private fun getSavedPressure() = sharedPrefs.getInt(KEY_PRESSURE, 0)

    private fun getItemHour (item : ForecastItem) = item.dt_txt.split(" ")[1].split(":")[0].toInt()

    private fun saveChips (chips: String) = sharedPrefs.edit().putString(KEY_CHIPS, chips).apply()

    private fun getSavedChips() = sharedPrefs.getString(KEY_CHIPS, "")
}