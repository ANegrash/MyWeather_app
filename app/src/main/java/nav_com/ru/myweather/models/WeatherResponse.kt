package nav_com.ru.myweather.models

data class WeatherResponse (
    var coord: Coordinates,
    var weather: List<Weather>,
    var base: Any,
    var main: Temperature,
    var visibility: Float,
    var wind: Wind,
    var clouds: Clouds,
    var dt: Any,
    var sys: System,
    var timezone: Int,
    var id: Any,
    var name: Any,
    var cod: Int
    )