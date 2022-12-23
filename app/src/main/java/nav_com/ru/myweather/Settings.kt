package nav_com.ru.myweather

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.widget.SwitchCompat

class Settings : AppCompatActivity() {
    private val sharedPrefs by lazy {  getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val back = findViewById<Button>(R.id.back)

        back.setOnClickListener {
            finish()
        }

        val switchTemperature = findViewById<SwitchCompat>(R.id.switchTemperature)
        val switchWind = findViewById<SwitchCompat>(R.id.switchWind)
        val switchWindDirection = findViewById<SwitchCompat>(R.id.switchWindDir)
        val switchPress = findViewById<SwitchCompat>(R.id.switchPress)

        switchTemperature.isChecked = getSavedTemperature() != 0
        switchWind.isChecked = getSavedWindPower() != 0
        switchWindDirection.isChecked = getSavedWindDirection() != 0
        switchPress.isChecked = getSavedPressure() != 0

        switchTemperature.setOnCheckedChangeListener { _, isChecked ->
            saveTemperature(if (isChecked) 1 else 0)
        }

        switchWind.setOnCheckedChangeListener { _, isChecked ->
            saveWindPower(if (isChecked) 1 else 0)
        }

        switchWindDirection.setOnCheckedChangeListener { _, isChecked ->
            saveWindDirection(if (isChecked) 1 else 0)
        }

        switchPress.setOnCheckedChangeListener { _, isChecked ->
            savePressure(if (isChecked) 1 else 0)
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