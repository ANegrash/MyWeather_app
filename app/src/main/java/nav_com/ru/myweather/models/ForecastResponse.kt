package nav_com.ru.myweather.models

data class ForecastResponse(
    var cod: String,
    var message: Any,
    var cnt: Int,
    var list: List<ForecastItem>,
    var city: Any
)
