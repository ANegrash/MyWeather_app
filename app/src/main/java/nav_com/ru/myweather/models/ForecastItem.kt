package nav_com.ru.myweather.models

data class ForecastItem(
    var dt: Any,
    var main: Temperature,
    var weather: List<Weather>,
    var clouds: Clouds,
    var wind: Wind,
    var visibility: Any,
    var pop: Float,
    var rain: Any,
    var snow: Any,
    var sys: Any,
    var dt_txt: String
)
