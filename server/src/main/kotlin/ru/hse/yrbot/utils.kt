package ru.hse.yrbot

import java.net.URI
import java.net.URLDecoder
import java.util.LinkedHashMap

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