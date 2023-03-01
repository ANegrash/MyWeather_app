package nav_com.ru.myweather

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import nav_com.ru.myweather.models.SearchCity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class SearchCityActivity : AppCompatActivity() {
    private val sharedPrefs by lazy {  getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_city)

        val cityEditText : EditText = findViewById(R.id.searchEditText)
        val doSearchBtn : ImageButton = findViewById(R.id.doSearchBtn)
        val backBtn : ImageButton = findViewById(R.id.backButtonToMain2)

        backBtn.setOnClickListener {
            intent = Intent(this, SelectCity::class.java)
            startActivity(intent)
            finish()
        }

        doSearchBtn.setOnClickListener {
            val q : String = cityEditText.text.toString()
            if (q.length >= 2)
                search(q)
        }
    }

    private fun search(
        query: String
    ) {
        val requestUrl =
            "https://api.openweathermap.org/geo/1.0/direct?" +
                    "q=$query" +
                    "&limit=5" +
                    "&appid=ddd069d11b1e504d8268d4ee774ddd64"

        val request = Request()

        request.run(
            requestUrl,
            object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(
                            this@SearchCityActivity,
                            "Произошла непредвиденная ошибка",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    if (response.body != null) {
                        val stringResponse = response.body!!.string()
                        val builder = GsonBuilder()
                        val gson: Gson = builder.create()
                        val listView = findViewById<ListView>(R.id.listOfSearch)

                        val searchObject: List<SearchCity> =
                            gson.fromJson(stringResponse, Array<SearchCity>::class.java).toList()

                        runOnUiThread {
                            val scAdapter = SearchCityAdapter(
                                this@SearchCityActivity,
                                R.layout.search_city_item,
                                searchObject
                            )
                            listView.adapter = scAdapter

                            listView.onItemClickListener =
                                AdapterView.OnItemClickListener { _, _, position, _ ->
                                    run {
                                        val obj: SearchCity = searchObject[position]
                                        val array : Map<String, String> = obj.local_names

                                        val cityName = if (array != null)
                                            if (array.containsKey("ru")) array["ru"].toString() else obj.name
                                        else
                                            obj.name

                                        val latlon = "" + obj.lat + ", " + obj.lon

                                        val builder = GsonBuilder()
                                        val gson: Gson = builder.create()
                                        val favoritesMap: MutableMap<String, String> = gson.fromJson(getSavedFavorites(), object : TypeToken<MutableMap<String, String>>() {}.type)
                                        favoritesMap[cityName] = latlon
                                        saveFavorites(gson.toJson(favoritesMap))
                                        saveCurrentCity(cityName)

                                        val intent = Intent(applicationContext, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                }
                        }
                    }
                }
            })
    }

    private fun saveCurrentCity (city: String) = sharedPrefs.edit().putString(KEY_CURRENT_CITY, city).apply()

    private fun saveFavorites(fav: String) = sharedPrefs.edit().putString(KEY_FAVORITE_LIST, fav).apply()

    private fun getSavedFavorites() = sharedPrefs.getString(KEY_FAVORITE_LIST, "{}")

}