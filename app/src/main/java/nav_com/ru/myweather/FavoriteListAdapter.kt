package nav_com.ru.myweather

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class FavoriteListAdapter (
    context: Context?,
    resource: Int,
    jsonObjects: List<String>
) : ArrayAdapter<String>(context!!, resource, jsonObjects) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val layout: Int = resource
    private val jsonObject: List<String> = jsonObjects

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val view = inflater.inflate(layout, parent, false)

        val city : TextView = view.findViewById(R.id.textViewLarge)
        val coord : TextView = view.findViewById(R.id.textViewSmall)
        val coordImage : ImageView = view.findViewById(R.id.imageView14)

        val currentStr = jsonObject[position]

        val arr : List<String> = currentStr.split("=")

        city.text = arr[0]
        coord.text = arr[1]

        if (getCurrentCity() == arr[0])
            coordImage.visibility = View.VISIBLE
        else
            coordImage.visibility = View.INVISIBLE

        return view
    }

    private fun getCurrentCity() = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_CURRENT_CITY, "")
}