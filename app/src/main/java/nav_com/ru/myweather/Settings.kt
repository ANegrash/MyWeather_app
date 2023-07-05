package nav_com.ru.myweather

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.chip.Chip

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
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        val radioSystem = findViewById<RadioButton>(R.id.radioTheme_system)
        val radioLight = findViewById<RadioButton>(R.id.radioTheme_light)
        val radioDark = findViewById<RadioButton>(R.id.radioTheme_dark)
        val radioTxt = findViewById<TextView>(R.id.textView27)

        val rate = findViewById<ConstraintLayout>(R.id.rate_layout)
        val privacy = findViewById<ConstraintLayout>(R.id.priacy)

        switchTemperature.isChecked = getSavedTemperature() != 0
        switchWind.isChecked = getSavedWindPower() != 0
        switchWindDirection.isChecked = getSavedWindDirection() != 0
        switchPress.isChecked = getSavedPressure() != 0

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            when (getSavedTheme()){
                0 -> radioSystem.isChecked = true
                1 -> radioLight.isChecked = true
                2 -> radioDark.isChecked = true
            }

            radioGroup.setOnCheckedChangeListener { _, id ->
                when (id) {
                    R.id.radioTheme_system -> setTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, 0)
                    R.id.radioTheme_light -> setTheme(AppCompatDelegate.MODE_NIGHT_NO, 1)
                    R.id.radioTheme_dark -> setTheme(AppCompatDelegate.MODE_NIGHT_YES, 2)
                }

            }
        } else {
            radioGroup.visibility = View.GONE
            radioTxt.visibility = View.GONE
        }

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

        var savedChips = getSavedChips().orEmpty()
        if (savedChips.isEmpty()) {
            savedChips = "11110000"
            saveChips(savedChips)
        }

        val wind = findViewById<Chip>(R.id.chip_settings_wind)
        val pressure = findViewById<Chip>(R.id.chip_settings_pressure)
        val humidity = findViewById<Chip>(R.id.chip_settings_humidity)
        val feels = findViewById<Chip>(R.id.chip_settings_temperature)
        val cloudiness = findViewById<Chip>(R.id.chip_settings_cloudiness)
        val sunrise = findViewById<Chip>(R.id.chip_settings_sunrise)
        val sunset = findViewById<Chip>(R.id.chip_settings_sunset)
        val visibility = findViewById<Chip>(R.id.chip_settings_visibility)
        val helpBtn = findViewById<ImageButton>(R.id.help)

        wind.isChecked = checkChip(savedChips, 0)
        pressure.isChecked = checkChip(savedChips, 1)
        humidity.isChecked = checkChip(savedChips, 2)
        feels.isChecked = checkChip(savedChips, 3)
        cloudiness.isChecked = checkChip(savedChips, 4)
        sunrise.isChecked = checkChip(savedChips, 5)
        sunset.isChecked = checkChip(savedChips, 6)
        visibility.isChecked = checkChip(savedChips, 7)

        wind.setOnClickListener {
            saveChips(replaceChip(getSavedChips().toString(), 0))
        }

        pressure.setOnClickListener {
            saveChips(replaceChip(getSavedChips().toString(), 1))
        }

        humidity.setOnClickListener {
            saveChips(replaceChip(getSavedChips().toString(), 2))
        }

        feels.setOnClickListener {
            saveChips(replaceChip(getSavedChips().toString(), 3))
        }

        cloudiness.setOnClickListener {
            saveChips(replaceChip(getSavedChips().toString(), 4))
        }

        sunrise.setOnClickListener {
            saveChips(replaceChip(getSavedChips().toString(), 5))
        }

        sunset.setOnClickListener {
            saveChips(replaceChip(getSavedChips().toString(), 6))
        }

        visibility.setOnClickListener {
            saveChips(replaceChip(getSavedChips().toString(), 7))
        }

        helpBtn.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.attention))
                .setMessage(getString(R.string.disclaimer_parameters))
                .setPositiveButton("OK") {
                    d, _ -> d.cancel()
                }
                .show()
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

    private fun checkChip (chips: String, find: Int) : Boolean {
        return chips[find] == '1'
    }

    private fun replaceChip (chips: String, find: Int) : String {
        var result = ""
        for (counter in 0..7) {
            result += if (counter == find)
                if (chips[find] == '1') "0" else "1"
            else
                chips[counter]
        }
        return result
    }

    private fun saveTheme (theme: Int) = sharedPrefs.edit().putInt(KEY_THEME, theme).apply()

    private fun getSavedTheme() = sharedPrefs.getInt(KEY_THEME, 0)

    private fun saveTemperature (t: Int) = sharedPrefs.edit().putInt(KEY_TEMPERATURE, t).apply()

    private fun getSavedTemperature() = sharedPrefs.getInt(KEY_TEMPERATURE, 0)

    private fun saveWindPower (wp: Int) = sharedPrefs.edit().putInt(KEY_WIND_POWER, wp).apply()

    private fun getSavedWindPower() = sharedPrefs.getInt(KEY_WIND_POWER, 0)

    private fun saveWindDirection (wd: Int) = sharedPrefs.edit().putInt(KEY_WIND_DIRECTION, wd).apply()

    private fun getSavedWindDirection() = sharedPrefs.getInt(KEY_WIND_DIRECTION, 0)

    private fun savePressure (p: Int) = sharedPrefs.edit().putInt(KEY_PRESSURE, p).apply()

    private fun getSavedPressure() = sharedPrefs.getInt(KEY_PRESSURE, 0)

    private fun setTheme (themeMode: Int, prefsMode: Int) {
        AppCompatDelegate.setDefaultNightMode(themeMode)
        saveTheme(prefsMode)
    }

    private fun saveChips (chips: String) = sharedPrefs.edit().putString(KEY_CHIPS, chips).apply()

    private fun getSavedChips() = sharedPrefs.getString(KEY_CHIPS, "")
}