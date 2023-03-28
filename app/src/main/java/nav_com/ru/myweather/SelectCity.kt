package nav_com.ru.myweather

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class SelectCity : AppCompatActivity() {

    private val sharedPrefs by lazy {  getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_city)

        loadContent()

    }

    private fun loadContent(){
        val buttonBack: ImageButton = findViewById(R.id.backButtonToMain)
        val buttonAddCity: ImageButton = findViewById(R.id.addNewCity)
        val buttonAddCityCTA: ImageButton = findViewById(R.id.addNewCityCTA)
        val title: TextView = findViewById(R.id.textViewTitle)
        val currentPlaceBtn: ConstraintLayout = findViewById(R.id.currentPlace)

        if (getSavedFavorites() == "{}") {
            buttonBack.visibility = View.GONE
            buttonAddCity.visibility = View.GONE
            title.visibility = View.GONE
        }

        buttonBack.setOnClickListener {
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        buttonAddCity.setOnClickListener {
            intent = Intent(this, SearchCityActivity::class.java)
            startActivity(intent)
        }

        buttonAddCityCTA.setOnClickListener {
            intent = Intent(this, SearchCityActivity::class.java)
            startActivity(intent)
        }

        currentPlaceBtn.setOnClickListener {
            saveCurrentCity("current")
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        val builder = GsonBuilder()
        val gson: Gson = builder.create()
        val favoritesMap: MutableMap<String, String> = gson.fromJson(getSavedFavorites(), object : TypeToken<MutableMap<String, String>>() {}.type)

        val listView: ListView = findViewById(R.id.fav_cities)


        if (favoritesMap.isEmpty()){
            val CTAContent = findViewById<ConstraintLayout>(R.id.noCities)
            val listContent = findViewById<ConstraintLayout>(R.id.cityList)
            CTAContent.visibility = View.VISIBLE
            listContent.visibility = View.GONE
        } else {
            val CTAContent = findViewById<ConstraintLayout>(R.id.noCities)
            val listContent = findViewById<ConstraintLayout>(R.id.cityList)
            CTAContent.visibility = View.GONE
            listContent.visibility = View.VISIBLE

            val list: List<String> = fillList(favoritesMap)
            listView.adapter = FavoriteListAdapter(
                this,
                R.layout.favorite_cities_element,
                list
            )

            listView.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    run {
                        saveCurrentCity(list[position].split("=")[0])
                        intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }

            listView.onItemLongClickListener =
                AdapterView.OnItemLongClickListener { _, _, position, _ ->
                    val message: String = list[position].split("=")[0]
                    AlertDialog.Builder(this)
                        .setTitle(getString(R.string.delete_place))
                        .setMessage(message)
                        .setPositiveButton(getString(R.string.yes)) { _, _ ->
                            favoritesMap.remove(list[position].split("=")[0])
                            saveFavorites(gson.toJson(favoritesMap))
                            if (getCurrentCity() == list[position].split("=")[0]) {
                                if (favoritesMap.isNotEmpty()) {
                                    if (position > 0)
                                        saveCurrentCity(list[position - 1].split("=")[0])
                                    else
                                        saveCurrentCity(list[position + 1].split("=")[0])
                                } else
                                    saveCurrentCity("")
                            }
                            loadContent()
                        }
                        .setCancelable(true)
                        .setNeutralButton(getString(R.string.cancel)) { _, _ ->

                        }
                        .show()
                    true
                }
        }
    }

    private fun fillList(
        map: MutableMap<String, String>
    ): List<String> {
        val data = mutableListOf<String>()
        (map).forEach { i -> data.add("$i") }
        return data
    }

    private fun saveFavorites(fav: String) = sharedPrefs.edit().putString(KEY_FAVORITE_LIST, fav).apply()

    private fun getSavedFavorites() = sharedPrefs.getString(KEY_FAVORITE_LIST, "{}")

    private fun saveCurrentCity (city: String) = sharedPrefs.edit().putString(KEY_CURRENT_CITY, city).apply()

    private fun getCurrentCity() = sharedPrefs.getString(KEY_CURRENT_CITY, "")
}