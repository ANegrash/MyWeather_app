package nav_com.ru.myweather

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import nav_com.ru.myweather.models.WeatherResponse
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.roundToInt

/**
 * Implementation of App Widget functionality.
 */
class NewAppWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val views = RemoteViews(context.packageName, R.layout.new_app_widget)

    val uri: Uri = Uri.parse("android.resource://nav_com.ru.myweather/drawable/shot5")
    views.setImageViewUri(R.id.widget_1_icon, uri)
    views.setTextViewText(R.id.widget_1_text, "+?°")
    views.setTextViewText(R.id.widget_1_time, context.getString(R.string.loading))
    appWidgetManager.updateAppWidget(appWidgetId, views)

    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val city = prefs.getString(KEY_CURRENT_CITY, "").toString()
    val fav = prefs.getString(KEY_FAVORITE_LIST, "{}")

    val builder = GsonBuilder()
    val gson: Gson = builder.create()
    val favoritesMap: MutableMap<String, String> = gson.fromJson(fav, object : TypeToken<MutableMap<String, String>>() {}.type)
    val ll = "lat=" + favoritesMap[city].toString().split(", ")[0] + "&lon=" + favoritesMap[city].toString().split(", ")[1]

    val savedTemperature = prefs.getInt(KEY_TEMPERATURE, 0)

    val units = if (savedTemperature == 0)
        "metric"
    else
        "imperial"

    val requestUrl =
        "https://api.openweathermap.org/data/2.5/weather?$ll" +
                "&appid=ddd069d11b1e504d8268d4ee774ddd64" +
                "&units=" + units

    val request = Request()

    request.run(
        requestUrl,
        object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                val uri: Uri = Uri.parse("android.resource://nav_com.ru.myweather/drawable/no_wifi")
                views.setImageViewUri(R.id.widget_1_icon, uri)
                views.setTextViewText(R.id.widget_1_text, "...")
                val sdf = SimpleDateFormat("hh:mm")
                val currentTime = sdf.format(Date()).toString()

                views.setTextViewText(R.id.widget_1_time, currentTime)
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.body != null) {
                    val stringResponse = response.body!!.string()
                    val builder = GsonBuilder()
                    val gson: Gson = builder.create()

                    val weatherObject: WeatherResponse = gson.fromJson(stringResponse, WeatherResponse::class.java)

                    if (weatherObject.cod == 200) {
                        val currentTemperature = getTemperature(weatherObject.main.temp)
                        val uri: Uri = Uri.parse("android.resource://nav_com.ru.myweather/drawable/w_" + weatherObject.weather[0].icon)
                        views.setImageViewUri(R.id.widget_1_icon, uri)
                        views.setTextViewText(R.id.widget_1_text, currentTemperature)

                        val sdf = SimpleDateFormat("hh:mm")
                        val currentTime = sdf.format(Date()).toString()

                        views.setTextViewText(R.id.widget_1_time, currentTime)

                        val updateIntent = Intent(context, NewAppWidget::class.java)
                        updateIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        updateIntent.putExtra(
                            AppWidgetManager.EXTRA_APPWIDGET_IDS,
                            intArrayOf(appWidgetId)
                        )
                        val pIntent = PendingIntent.getBroadcast(context, appWidgetId, updateIntent, PendingIntent.FLAG_IMMUTABLE)
                        views.setOnClickPendingIntent(R.id.widget_1_reload, pIntent)

                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                }
            }
        }
    )
}

private fun getTemperature(temp: Any) : String {
    val intTemp = temp.toString().toFloat().roundToInt()
    return if (intTemp > 0)
        "+$intTemp°"
    else
        "$intTemp°"
}