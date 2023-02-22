package nav_com.ru.myweather

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import nav_com.ru.myweather.models.SearchCity

class SelectCity : AppCompatActivity() {

    private val sharedPrefs by lazy {  getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_city)

        val listView: ListView = findViewById(R.id.fav_cities)
        val buttonBack: ImageButton = findViewById(R.id.backButtonToMain)
        val buttonAddCity: ImageButton = findViewById(R.id.addNewCity)
        val buttonAddCityCTA: ImageButton = findViewById(R.id.addNewCityCTA)

        val builder = GsonBuilder()
        val gson: Gson = builder.create()
        val favoritesMap: MutableMap<String, String> = gson.fromJson(getSavedFavorites(), object : TypeToken<MutableMap<String, String>>() {}.type)

        if (intent.hasExtra("name")){
            val selName = intent.getStringExtra("name").toString()
            val selLL = intent.getStringExtra("ll").toString()
            favoritesMap[selName] = selLL
            saveFavorites(gson.toJson(favoritesMap))
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
            val list : List<String> = fillList(favoritesMap)
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