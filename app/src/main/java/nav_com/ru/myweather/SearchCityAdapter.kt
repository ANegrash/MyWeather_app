package nav_com.ru.myweather

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import nav_com.ru.myweather.models.SearchCity
import java.util.*

class SearchCityAdapter (
    context: Context?,
    resource: Int,
    jsonObjects: List<SearchCity>
) : ArrayAdapter<SearchCity?>(context!!, resource, jsonObjects) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val layout: Int = resource
    private val jsonObject: List<SearchCity> = jsonObjects

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val view = inflater.inflate(layout, parent, false)

        val city : TextView = view.findViewById(R.id.cityNameInSearch)
        val ll : TextView = view.findViewById(R.id.latlonSearch)
        val obj : SearchCity = jsonObject[position]

        val array : Map<String, String> = obj.local_names

        val lang : String = if (Locale.getDefault().language == "en") "en" else "ru"

        val cityName = if (array != null)
            if (array.containsKey(lang)) array[lang].toString() else obj.name
        else
            obj.name

        val country = if ((obj.lon in 32.394843..36.647747) && (obj.lat in 44.363357..46.06472))
            "Crimea"
        else
            obj.country

        val latlon = "" + obj.lat + ", " + obj.lon

        city.text = cityName + ", " + country
        ll.text = latlon

        return view
    }
}