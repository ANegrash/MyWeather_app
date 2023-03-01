package nav_com.ru.myweather

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
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
        setError(3)

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
            else
                setError(2)
        }
    }

    private fun search(
        query: String
    ) {
        setContent(0, 1,0)
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
                        setError(0)
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
                            if (searchObject.isEmpty())
                                setError(1)
                            else {
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
                                            val array: Map<String, String> = obj.local_names

                                            val cityName = if (array != null)
                                                if (array.containsKey("ru")) array["ru"].toString() else obj.name
                                            else
                                                obj.name

                                            val latlon = "" + obj.lat + ", " + obj.lon

                                            val builder = GsonBuilder()
                                            val gson: Gson = builder.create()
                                            val favoritesMap: MutableMap<String, String> =
                                                gson.fromJson(
                                                    getSavedFavorites(),
                                                    object :
                                                        TypeToken<MutableMap<String, String>>() {}.type
                                                )
                                            favoritesMap[cityName] = latlon
                                            saveFavorites(gson.toJson(favoritesMap))
                                            saveCurrentCity(cityName)

                                            val intent =
                                                Intent(applicationContext, MainActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                    }

                                setContent()
                            }
                        }
                    }
                }
            })
    }

    private fun setContent(
        main: Int = 1,
        loading: Int = 0,
        error: Int = 0
    ){
        val mainContent = findViewById<ListView>(R.id.listOfSearch)
        val loadingContent = findViewById<ConstraintLayout>(R.id.loading_inSearch)
        val errorContent = findViewById<ConstraintLayout>(R.id.error_inSearch)

        when (main) {
            0 -> mainContent.visibility = View.GONE
            1 -> mainContent.visibility = View.VISIBLE
        }

        when (loading) {
            0 -> loadingContent.visibility = View.GONE
            1 -> loadingContent.visibility = View.VISIBLE
        }

        when (error) {
            0 -> errorContent.visibility = View.GONE
            1 -> errorContent.visibility = View.VISIBLE
        }
    }

    private fun setError(
        type: Int = 0
    ){
        val icon = findViewById<ImageView>(R.id.errorIcon_inSearch)
        val mainText = findViewById<TextView>(R.id.mainError_inSearch)
        val secondaryText = findViewById<TextView>(R.id.secondaryError_inSearch)

        when (type){
            0 -> {
                icon.setImageResource(R.drawable.no_internet)
                mainText.text = "Нет соединения с сервером"
                secondaryText.text = "Проверьте наличие интернета или попробуйте позже"
            }
            1 -> {
                icon.setImageResource(R.drawable.no_results)
                mainText.text = "Поиск не дал результатов"
                secondaryText.text = "Проверьте введённые данные на ошибки"
            }
            2 -> {
                icon.setImageResource(R.drawable.no_results)
                mainText.text = "Введено мало символов"
                secondaryText.text = "Для поиска введите минимум 2 символа"
            }
            3 -> {
                icon.setImageResource(R.drawable.lets_search)
                mainText.text = "Добавьте любой город на планете!"
                secondaryText.text = "1. Введите название в поле выше \n2. Нажмите кнопку поиска \n3. Выберите нужный вариант из списка"
            }
        }

        setContent(0, 0, 1)
    }

    private fun saveCurrentCity (city: String) = sharedPrefs.edit().putString(KEY_CURRENT_CITY, city).apply()

    private fun saveFavorites(fav: String) = sharedPrefs.edit().putString(KEY_FAVORITE_LIST, fav).apply()

    private fun getSavedFavorites() = sharedPrefs.getString(KEY_FAVORITE_LIST, "{}")

}