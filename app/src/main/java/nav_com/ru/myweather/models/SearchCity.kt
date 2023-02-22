package nav_com.ru.myweather.models

data class SearchCity(
    val name: String,
    val local_names: Map<String, String>,
    val lat: Float,
    val lon: Float,
    val country: String,
    val state: Any
)
