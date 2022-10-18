package nav_com.ru.myweather

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import nav_com.ru.myweather.models.ForecastItem
import java.util.*
import kotlin.math.roundToInt

class ForecastAdapter (
    context: Context?,
    resource: Int,
    jsonObjects: List<Array<ForecastItem>>
) : ArrayAdapter<Array<ForecastItem>?>(context!!, resource, jsonObjects) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val layout: Int = resource
    private val jsonObject: List<Array<ForecastItem>> = jsonObjects

    override fun getView (
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val view = inflater.inflate(layout, parent, false)

        val dayObject = jsonObject[position]

        val morningCard = view.findViewById<CardView>(R.id.morning_card)
        val dayCard = view.findViewById<CardView>(R.id.day_card)
        val eveningCard = view.findViewById<CardView>(R.id.evening_card)
        val nightCard = view.findViewById<CardView>(R.id.night_card)

        val morning_icon = view.findViewById<ImageView>(R.id.morning_icon)
        val day_icon = view.findViewById<ImageView>(R.id.day_icon)
        val evening_icon = view.findViewById<ImageView>(R.id.evening_icon)
        val night_icon = view.findViewById<ImageView>(R.id.night_icon)

        val morning_temperature = view.findViewById<TextView>(R.id.temperature_morning)
        val day_temperature = view.findViewById<TextView>(R.id.temperature_day)
        val evening_temperature = view.findViewById<TextView>(R.id.temperature_evening)
        val night_temperature = view.findViewById<TextView>(R.id.temperature_night)

        val morning_wind = view.findViewById<TextView>(R.id.wind_morning)
        val day_wind = view.findViewById<TextView>(R.id.wind_day)
        val evening_wind = view.findViewById<TextView>(R.id.wind_evening)
        val night_wind = view.findViewById<TextView>(R.id.wind_night)

        val date_tw = view.findViewById<TextView>(R.id.date_tw)

        val temperSys = getSavedTemperature()
        val windPowerSys = getSavedWindPower()
        val windDirectSys = getSavedWindDirection()

        if (dayObject.size == 1){
            dayCard.visibility = View.GONE
            eveningCard.visibility = View.GONE
            nightCard.visibility = View.GONE
        }

        if (dayObject.size == 2){
            eveningCard.visibility = View.GONE
            nightCard.visibility = View.GONE
        }

        if (dayObject.size == 3){
            nightCard.visibility = View.GONE
        }

        if (dayObject.size >= 1) {
            morning_temperature.text = getTemperature(dayObject[0].main.temp, temperSys)
            morning_wind.text = getWindInfo(dayObject[0].wind.speed, dayObject[0].wind.deg, temperSys, windPowerSys, windDirectSys)
            val uri: Uri =
                Uri.parse("android.resource://nav_com.ru.myweather/drawable/w_" + dayObject[0].weather[0].icon)
            morning_icon.setImageURI(null)
            morning_icon.setImageURI(uri)
        }

        if (dayObject.size >= 2) {
            day_temperature.text = getTemperature(dayObject[1].main.temp, temperSys)
            day_wind.text = getWindInfo(dayObject[1].wind.speed, dayObject[1].wind.deg, temperSys, windPowerSys, windDirectSys)
            val uri1: Uri =
                Uri.parse("android.resource://nav_com.ru.myweather/drawable/w_" + dayObject[1].weather[0].icon)
            day_icon.setImageURI(null)
            day_icon.setImageURI(uri1)
        }

        if (dayObject.size >= 3) {
            evening_temperature.text = getTemperature(dayObject[2].main.temp, temperSys)
            evening_wind.text = getWindInfo(dayObject[2].wind.speed, dayObject[2].wind.deg, temperSys, windPowerSys, windDirectSys)
            val uri2: Uri =
                Uri.parse("android.resource://nav_com.ru.myweather/drawable/w_" + dayObject[2].weather[0].icon)
            evening_icon.setImageURI(null)
            evening_icon.setImageURI(uri2)
        }

        if (dayObject.size >= 4) {
            night_temperature.text = getTemperature(dayObject[3].main.temp, temperSys)
            night_wind.text = getWindInfo(dayObject[3].wind.speed, dayObject[3].wind.deg, temperSys, windPowerSys, windDirectSys)
            val uri3: Uri =
                Uri.parse("android.resource://nav_com.ru.myweather/drawable/w_" + dayObject[3].weather[0].icon)
            night_icon.setImageURI(null)
            night_icon.setImageURI(uri3)
        }

        if (dayObject.isNotEmpty())
            date_tw.text = getTrueDate(dayObject[0].dt_txt)

        return view
    }

    private fun getTemperature(temp: Any, end: Int) : String {
        val intTemp = temp.toString().toFloat().roundToInt()
        val ending = if (end == 0) "C" else "F"
        return if (intTemp > 0)
            "+$intTemp°$ending"
        else
            "$intTemp°$ending"
    }

    private fun getTrueDate(date: Any) : String {
        val result : String

        val dayPart = date.toString().split(" ")[0]

        val day = dayPart.split("-")[2]
        val month = dayPart.split("-")[1]

        val monthText = when (month.toInt()) {
            1 -> "января"
            2 -> "февраля"
            3 -> "марта"
            4 -> "апреля"
            5 -> "мая"
            6 -> "июня"
            7 -> "июля"
            8 -> "августа"
            9 -> "сентября"
            10 -> "октября"
            11 -> "ноября"
            12 -> "декабря"
            else -> ""
        }

        val c = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())

        val nowMonth = c.get(Calendar.MONTH) + 1
        val nowDay = c.get(Calendar.DAY_OF_MONTH)

        if (nowMonth == month.toInt()) {
            if (nowDay == day.toInt()){
                return "Сегодня"
            }
            else if (nowDay + 1 == day.toInt()){
                return "Завтра"
            }
        }

        result = "" + day.toInt() + " " + monthText

        return result
    }

    private fun getWindInfo(speed: Any, degrees: Any, tempMode: Int, speedMode: Int, directionMode: Int) : String {
        var power = ""
        if (tempMode == 0) {
            if (speedMode == 0)
                power += "" + speed.toString().toFloat().roundToInt() + " м/с, "
            else
                power += "" + String.format(
                    "%.1f",
                    speed.toString().toFloat().roundToInt() * 2.236936
                ) + " м/ч \n"
        } else {
            if (speedMode == 0)
                power += "" + (speed.toString().toFloat()
                    .roundToInt() * 0.44704).roundToInt() + " м/с, "
            else
                power += "" + String.format("%.1f", speed.toString().toFloat()) + " м/ч \n"
        }

        val deg = degrees.toString().toFloat().roundToInt()

        var direction = ""
        if (directionMode == 0){
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

    private fun getSavedTemperature() = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_TEMPERATURE, 0)

    private fun getSavedWindPower() = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_WIND_POWER, 0)

    private fun getSavedWindDirection() = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_WIND_DIRECTION, 0)
}