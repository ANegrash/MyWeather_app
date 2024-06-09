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
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
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

        val morningObject = jsonObject[position][0]
        val dayObject = jsonObject[position][1]
        val eveningObject = jsonObject[position][2]
        val nightObject = jsonObject[position][3]

        val morningCard = view.findViewById<CardView>(R.id.morning_card)
        val dayCard = view.findViewById<CardView>(R.id.day_card)
        val eveningCard = view.findViewById<CardView>(R.id.evening_card)
        val nightCard = view.findViewById<CardView>(R.id.night_card)

        val morningIcon = view.findViewById<ImageView>(R.id.morning_icon)
        val dayIcon = view.findViewById<ImageView>(R.id.day_icon)
        val eveningIcon = view.findViewById<ImageView>(R.id.evening_icon)
        val nightIcon = view.findViewById<ImageView>(R.id.night_icon)

        val morningTemperature = view.findViewById<TextView>(R.id.temperature_morning)
        val dayTemperature = view.findViewById<TextView>(R.id.temperature_day)
        val eveningTemperature = view.findViewById<TextView>(R.id.temperature_evening)
        val nightTemperature = view.findViewById<TextView>(R.id.temperature_night)

        val morningWind = view.findViewById<TextView>(R.id.wind_morning)
        val dayWind = view.findViewById<TextView>(R.id.wind_day)
        val eveningWind = view.findViewById<TextView>(R.id.wind_evening)
        val nightWind = view.findViewById<TextView>(R.id.wind_night)

        val dateTw = view.findViewById<TextView>(R.id.date_tw)
        val dateOw = view.findViewById<TextView>(R.id.date_ow)

        val temperSys = getSavedTemperature()
        val windPowerSys = getSavedWindPower()
        val windDirectSys = getSavedWindDirection()

        if (morningObject.wind == null)
            morningCard.visibility = View.GONE
        else {
            morningTemperature.text = getTemperature(morningObject.main!!.temp)
            morningWind.text = getWindInfo(
                morningObject.wind!!.speed,
                morningObject.wind!!.deg,
                temperSys,
                windPowerSys,
                windDirectSys
            )
        }
        val uri: Uri =
            Uri.parse(
                "android.resource://nav_com.ru.myweather/drawable/w_" +
                toDay(morningObject.weather[0].icon.toString())
            )
        morningIcon.setImageURI(null)
        morningIcon.setImageURI(uri)

        if (dayObject.wind == null)
            dayCard.visibility = View.GONE
        else {
            dayTemperature.text = getTemperature(dayObject.main!!.temp)
            dayWind.text = getWindInfo(
                dayObject.wind!!.speed,
                dayObject.wind!!.deg,
                temperSys,
                windPowerSys,
                windDirectSys
            )
        }
        val uri1: Uri =
            Uri.parse(
                "android.resource://nav_com.ru.myweather/drawable/w_" +
                toDay(dayObject.weather[0].icon.toString())
            )
        dayIcon.setImageURI(null)
        dayIcon.setImageURI(uri1)

        if (eveningObject.wind == null)
            eveningCard.visibility = View.GONE
        else {
            eveningTemperature.text = getTemperature(eveningObject.main!!.temp)
            eveningWind.text = getWindInfo(
                eveningObject.wind!!.speed,
                eveningObject.wind!!.deg,
                temperSys,
                windPowerSys,
                windDirectSys
            )
        }
        val uri2: Uri =
            Uri.parse(
                "android.resource://nav_com.ru.myweather/drawable/w_" +
                toNight(eveningObject.weather[0].icon.toString())
            )
        eveningIcon.setImageURI(null)
        eveningIcon.setImageURI(uri2)

        if (nightObject.wind == null)
            nightCard.visibility = View.GONE
        else {
            nightTemperature.text = getTemperature(nightObject.main!!.temp)
            nightWind.text = getWindInfo(
                nightObject.wind!!.speed,
                nightObject.wind!!.deg,
                temperSys,
                windPowerSys,
                windDirectSys
            )
        }
        val uri3: Uri =
            Uri.parse(
                "android.resource://nav_com.ru.myweather/drawable/w_" +
                toNight(nightObject.weather[0].icon.toString())
            )
        nightIcon.setImageURI(null)
        nightIcon.setImageURI(uri3)

        dateTw.text = getTrueDate(morningObject.dt_txt)

        val dayPart = morningObject.dt_txt.split(" ")[0]
        val day = dayPart.split("-")[2].toInt()
        val month = dayPart.split("-")[1].toInt()
        val c = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault())
        val nowMonth = c.get(Calendar.MONTH) + 1
        val nowDay = c.get(Calendar.DAY_OF_MONTH)

        val isAround = if (nowMonth == month)
            if (nowDay == day)
                context.getString(R.string.today)
            else if (nowDay + 1 == day)
                context.getString(R.string.tomorrow)
            else
                ""
        else
            ""

        if (isAround.isNotEmpty()) {
            dateOw.visibility = View.VISIBLE
            dateOw.text = isAround
        } else {
            dateOw.visibility = View.GONE
            dateOw.text = ""
        }

        return view
    }

    private fun getTemperature(temp: Any) : String {
        val intTemp = temp.toString().toFloat().roundToInt()
        return "$intTemp°"
    }

    private fun getTrueDate(date: Any) : String {
        val result : String

        val dayPart = date.toString().split(" ")[0]

        val day = dayPart.split("-")[2].toInt()
        val month = dayPart.split("-")[1].toInt()
        val year = dayPart.split("-")[0].toInt()

        val dayOfWeekText = when (LocalDate.of(year, month, day).dayOfWeek){
            DayOfWeek.MONDAY -> ", " + context.getString(R.string.mon)
            DayOfWeek.TUESDAY -> ", " + context.getString(R.string.tue)
            DayOfWeek.WEDNESDAY -> ", " + context.getString(R.string.wed)
            DayOfWeek.THURSDAY -> ", " + context.getString(R.string.thu)
            DayOfWeek.FRIDAY -> ", " + context.getString(R.string.fri)
            DayOfWeek.SATURDAY -> ", " + context.getString(R.string.sat)
            DayOfWeek.SUNDAY -> ", " + context.getString(R.string.sun)
            else -> ""
        }

        val monthText = when (month) {
            1 -> context.getString(R.string.month_jan)
            2 -> context.getString(R.string.month_feb)
            3 -> context.getString(R.string.month_mar)
            4 -> context.getString(R.string.month_apr)
            5 -> context.getString(R.string.month_may)
            6 -> context.getString(R.string.month_jun)
            7 -> context.getString(R.string.month_jul)
            8 -> context.getString(R.string.month_aug)
            9 -> context.getString(R.string.month_sep)
            10 -> context.getString(R.string.month_oct)
            11 -> context.getString(R.string.month_nov)
            12 -> context.getString(R.string.month_dec)
            else -> ""
        }

        result = if (Locale.getDefault().language == "en")
            "$monthText, $day"
        else
            "$day $monthText$dayOfWeekText"

        return result
    }

    private fun getWindInfo(speed: Any, degrees: Any, tempMode: Int, speedMode: Int, directionMode: Int) : String {
        val power = if (tempMode == 0) {
            if (speedMode == 0)
                "" + speed.toString().toFloat().roundToInt() + " " + context.getString(R.string.windPower_ms) + ", "
            else
                "" + String.format("%.1f", speed.toString().toFloat().roundToInt() * 2.236936) + " " + context.getString(R.string.windPower_milesh) + ", "
        } else {
            if (getSavedWindPower() == 0)
                "" + (speed.toString().toFloat().roundToInt() * 0.44704).roundToInt() + " " + context.getString(R.string.windPower_ms) + ", "
            else
                "" + String.format("%.1f", speed.toString().toFloat()) + " " + context.getString(R.string.windPower_milesh) + ", "
        }

        val deg = degrees.toString().toFloat().roundToInt()

        var direction = ""
        if (directionMode == 0) {
            when (deg) {
                in 0..23 -> direction = context.getString(R.string.direction_N)
                in 24..66 -> direction = context.getString(R.string.direction_N) + context.getString(R.string.direction_E)
                in 67..113 -> direction = context.getString(R.string.direction_E)
                in 114..158 -> direction = context.getString(R.string.direction_S) + context.getString(R.string.direction_E)
                in 159..203 -> direction = context.getString(R.string.direction_S)
                in 204..248 -> direction = context.getString(R.string.direction_S) + context.getString(R.string.direction_W)
                in 249..293 -> direction = context.getString(R.string.direction_W)
                in 294..338 -> direction = context.getString(R.string.direction_N) + context.getString(R.string.direction_W)
                in 339..360 -> direction = context.getString(R.string.direction_N)
            }
        } else
            direction = "$deg°"

        return power + direction
    }

    private fun toDay(src: String) : String {
        return src.dropLast(1) + "d"
    }

    private fun toNight(src: String) : String {
        return src.dropLast(1) + "n"
    }

    private fun getSavedTemperature() = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_TEMPERATURE, 0)

    private fun getSavedWindPower() = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_WIND_POWER, 0)

    private fun getSavedWindDirection() = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_WIND_DIRECTION, 0)
}