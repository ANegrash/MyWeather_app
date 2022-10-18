package nav_com.ru.myweather

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class Settings : AppCompatActivity() {
    private val sharedPrefs by lazy {  getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val back = findViewById<Button>(R.id.back)

        back.setOnClickListener {
            finish()
        }

        val temperatureC = findViewById<Button>(R.id.temperature_c)
        val temperatureF = findViewById<Button>(R.id.temperature_f)
        val windMS = findViewById<Button>(R.id.wind_meters)
        val windMH = findViewById<Button>(R.id.wind_miles)
        val windStoroni = findViewById<Button>(R.id.wind_geo)
        val windDegrees = findViewById<Button>(R.id.wind_deg)
        val pressMRS = findViewById<Button>(R.id.press_mm)
        val pressHPA = findViewById<Button>(R.id.press_hpa)

        if (getSavedTemperature() == 0){
            temperatureC.setBackgroundColor(resources.getColor(R.color.light_green))
            temperatureF.setBackgroundColor(resources.getColor(R.color.ic_launcher_background))
        } else {
            temperatureC.setBackgroundColor(resources.getColor(R.color.ic_launcher_background))
            temperatureF.setBackgroundColor(resources.getColor(R.color.light_green))
        }

        if (getSavedWindPower() == 0){
            windMS.setBackgroundColor(resources.getColor(R.color.light_green))
            windMH.setBackgroundColor(resources.getColor(R.color.ic_launcher_background))
        } else {
            windMS.setBackgroundColor(resources.getColor(R.color.ic_launcher_background))
            windMH.setBackgroundColor(resources.getColor(R.color.light_green))
        }

        if (getSavedWindDirection() == 0){
            windStoroni.setBackgroundColor(resources.getColor(R.color.light_green))
            windDegrees.setBackgroundColor(resources.getColor(R.color.ic_launcher_background))
        } else {
            windStoroni.setBackgroundColor(resources.getColor(R.color.ic_launcher_background))
            windDegrees.setBackgroundColor(resources.getColor(R.color.light_green))
        }

        if (getSavedPressure() == 0){
            pressMRS.setBackgroundColor(resources.getColor(R.color.light_green))
            pressHPA.setBackgroundColor(resources.getColor(R.color.ic_launcher_background))
        } else {
            pressMRS.setBackgroundColor(resources.getColor(R.color.ic_launcher_background))
            pressHPA.setBackgroundColor(resources.getColor(R.color.light_green))
        }

        temperatureC.setOnClickListener {
            saveTemperature(0)
            temperatureC.setBackgroundColor(resources.getColor(R.color.light_green))
            temperatureF.setBackgroundColor(resources.getColor(R.color.ic_launcher_background))
        }

        temperatureF.setOnClickListener {
            saveTemperature(1)
            temperatureC.setBackgroundColor(resources.getColor(R.color.ic_launcher_background))
            temperatureF.setBackgroundColor(resources.getColor(R.color.light_green))
        }

        windMS.setOnClickListener {
            saveWindPower(0)
            windMS.setBackgroundColor(resources.getColor(R.color.light_green))
            windMH.setBackgroundColor(resources.getColor(R.color.ic_launcher_background))
        }

        windMH.setOnClickListener {
            saveWindPower(1)
            windMS.setBackgroundColor(resources.getColor(R.color.ic_launcher_background))
            windMH.setBackgroundColor(resources.getColor(R.color.light_green))
        }

        windStoroni.setOnClickListener {
            saveWindDirection(0)
            windStoroni.setBackgroundColor(resources.getColor(R.color.light_green))
            windDegrees.setBackgroundColor(resources.getColor(R.color.ic_launcher_background))
        }

        windDegrees.setOnClickListener {
            saveWindDirection(1)
            windStoroni.setBackgroundColor(resources.getColor(R.color.ic_launcher_background))
            windDegrees.setBackgroundColor(resources.getColor(R.color.light_green))
        }

        pressMRS.setOnClickListener {
            savePressure(0)
            pressMRS.setBackgroundColor(resources.getColor(R.color.light_green))
            pressHPA.setBackgroundColor(resources.getColor(R.color.ic_launcher_background))
        }

        pressHPA.setOnClickListener {
            savePressure(1)
            pressMRS.setBackgroundColor(resources.getColor(R.color.ic_launcher_background))
            pressHPA.setBackgroundColor(resources.getColor(R.color.light_green))
        }
    }

    private fun saveTemperature (t: Int) = sharedPrefs.edit().putInt(KEY_TEMPERATURE, t).apply()

    private fun getSavedTemperature() = sharedPrefs.getInt(KEY_TEMPERATURE, 0)

    private fun saveWindPower (wp: Int) = sharedPrefs.edit().putInt(KEY_WIND_POWER, wp).apply()

    private fun getSavedWindPower() = sharedPrefs.getInt(KEY_WIND_POWER, 0)

    private fun saveWindDirection (wd: Int) = sharedPrefs.edit().putInt(KEY_WIND_DIRECTION, wd).apply()

    private fun getSavedWindDirection() = sharedPrefs.getInt(KEY_WIND_DIRECTION, 0)

    private fun savePressure (p: Int) = sharedPrefs.edit().putInt(KEY_PRESSURE, p).apply()

    private fun getSavedPressure() = sharedPrefs.getInt(KEY_PRESSURE, 0)
}