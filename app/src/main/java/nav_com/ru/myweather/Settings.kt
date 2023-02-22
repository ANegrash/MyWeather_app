package nav_com.ru.myweather

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout

class Settings : AppCompatActivity() {
    private val sharedPrefs by lazy {  getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val back = findViewById<ImageButton>(R.id.back)

        back.setOnClickListener {
            finish()
        }

        val switchTemperature = findViewById<SwitchCompat>(R.id.switchTemperature)
        val switchWind = findViewById<SwitchCompat>(R.id.switchWind)
        val switchWindDirection = findViewById<SwitchCompat>(R.id.switchWindDir)
        val switchPress = findViewById<SwitchCompat>(R.id.switchPress)

        val rate = findViewById<ConstraintLayout>(R.id.rate_layout)
        val privacy = findViewById<ConstraintLayout>(R.id.priacy)

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

        rate.setOnClickListener {
            val uri: Uri = Uri.parse("market://details?id=$packageName")
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            goToMarket.addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            try {
                startActivity(goToMarket)
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=$packageName"))
                )
            }
        }

        privacy.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW);
            intent.data = Uri.parse("https://nav-com.ru/weather")
            startActivity(intent)
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