package nav_com.ru.myweather

import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request

class Request {

    fun run(
        url: String,
        callback: Callback
    ) {
        val okHttpClient = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()
        okHttpClient.newCall(request).enqueue(callback)
    }
}
