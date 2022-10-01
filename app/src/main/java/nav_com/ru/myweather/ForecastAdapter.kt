package nav_com.ru.myweather

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import nav_com.ru.myweather.models.ForecastItem
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

        val morning_icon = view.findViewById<ImageView>(R.id.morning_icon)
        val day_icon = view.findViewById<ImageView>(R.id.day_icon)
        val evening_icon = view.findViewById<ImageView>(R.id.evening_icon)
        val night_icon = view.findViewById<ImageView>(R.id.night_icon)

        val morning_temperature = view.findViewById<TextView>(R.id.temperature_morning)
        val day_temperature = view.findViewById<TextView>(R.id.temperature_day)
        val evening_temperature = view.findViewById<TextView>(R.id.temperature_evening)
        val night_temperature = view.findViewById<TextView>(R.id.temperature_night)

        val date_tw = view.findViewById<TextView>(R.id.date_tw)

        val uri: Uri = Uri.parse("android.resource://nav_com.ru.myweather/drawable/w_" + dayObject[0].weather[0].icon )
        morning_icon.setImageURI(null)
        morning_icon.setImageURI(uri)

        val uri1: Uri = Uri.parse("android.resource://nav_com.ru.myweather/drawable/w_" + dayObject[1].weather[0].icon )
        day_icon.setImageURI(null)
        day_icon.setImageURI(uri1)

        val uri2: Uri = Uri.parse("android.resource://nav_com.ru.myweather/drawable/w_" + dayObject[2].weather[0].icon )
        evening_icon.setImageURI(null)
        evening_icon.setImageURI(uri2)

        val uri3: Uri = Uri.parse("android.resource://nav_com.ru.myweather/drawable/w_" + dayObject[3].weather[0].icon )
        night_icon.setImageURI(null)
        night_icon.setImageURI(uri3)

        morning_temperature.text = getTemperature(dayObject[0].main.temp)
        day_temperature.text = getTemperature(dayObject[1].main.temp)
        evening_temperature.text = getTemperature(dayObject[2].main.temp)
        night_temperature.text = getTemperature(dayObject[3].main.temp)

        date_tw.text = getTrueDate(dayObject[0].dt_txt)

        return view
    }

    private fun getTemperature(temp: Any) : String {
        val intTemp = temp.toString().toFloat().roundToInt()
        return if (intTemp > 0)
            "+$intTemp°"
        else
            "$intTemp°"
    }

    private fun getTrueDate(date: Any) : String {
        var result = ""

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

        result = "" + day.toInt() + " " + monthText

        return result
    }
}