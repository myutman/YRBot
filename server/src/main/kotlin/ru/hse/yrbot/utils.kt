package ru.hse.yrbot

import org.json.JSONObject
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLDecoder
import java.util.*
import java.util.stream.Collectors

val auth = {
    val properties = Properties()
    val propertiesStream = FileInputStream("auth.properties")
    properties.load(propertiesStream)
    properties["auth"] as String
}()

val districts = {
    val reader = BufferedReader(FileReader("regionInfo.json"))
    val response = reader
            .lines()
            .collect(Collectors
                    .joining("\n"))
    JSONObject(response).getJSONObject("response").getJSONArray("sublocalities")
}()

fun checkLocation(location: JSONObject): Boolean {
    if (location.optInt("rgid") != 417899) {
        for (district in districts) {
            if (location.optInt("rgid").toString() == (district as JSONObject).getString("id"))
                return true
        }
        return false
    }
    return true
}

fun getRegionIdByName(name: String): String {
    for (district in districts) {
        if (name == (district as JSONObject).getString("name")) return district.getString("id")
    }
    return "417899"
}

fun getInfoJSONById(id: Long): JSONObject {
    val url = URL("https://api.realty.yandex.net/1.0/cardWithViews.json?currency=RUR&id=$id&showOnMobile=YES")
    var conn = url.openConnection() as HttpURLConnection
    conn.requestMethod = "GET"

    conn.addRequestProperty("X-Authorization", auth)

    while (conn.responseCode != 200 && conn.responseCode != 404) {
        println("code = ${conn.responseCode} ${conn.responseMessage}")
        conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.addRequestProperty("X-Authorization", auth)
    }

    if (conn.responseCode == 404) {
        return JSONObject("{}")
    }

    val reader = BufferedReader(InputStreamReader(conn.inputStream))

    val response = reader
            .lines()
            .collect(Collectors
                    .joining("\n"))
    conn.disconnect()
    return JSONObject(response).optJSONObject("response") ?: JSONObject("{}")
}

fun splitQuery(uri: URI): Map<String, String> {
    val query_pairs = LinkedHashMap<String, String>()
    val query = uri.getQuery()
    val pairs = query.split("&".toRegex())
            .dropLastWhile({ it.isEmpty() })
            .toTypedArray()
    for (pair in pairs) {
        val idx = pair.indexOf("=")
        val key = URLDecoder.decode(pair.substring(0, idx), "UTF-8")
        val value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
        query_pairs[key] = value
    }
    return query_pairs
}