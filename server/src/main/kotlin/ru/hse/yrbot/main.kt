package ru.hse.yrbot

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.apache.commons.io.IOUtils
import org.json.JSONObject
import java.net.InetSocketAddress
import java.sql.DriverManager
import java.sql.Statement

fun main(args: Array<String>) {
    println("It's alive")
    val host = "postgres"
    val port = 5432
    val connection = DriverManager.getConnection("jdbc:postgresql://$host:$port/yrealty", "yrealty", "mickjagger")
    val statement = connection.createStatement()
    var query = "DROP TABLE IF EXISTS realty3_offers CASCADE;"
    statement.executeUpdate(query)
    query = "CREATE TABLE realty3_offers (offer_id BIGINT PRIMARY KEY, unified_address TEXT, area FLOAT, total_price_in_rubbles INT, rooms INT, partner_id BIGINT, partner_name TEXT, agent_fee FLOAT, latitude FLOAT, longitude FLOAT, metro_geo_id INT, time_to_metro INT, floor INT, floors_total INT, price INT, kitchen_area FLOAT, ceiling_height FLOAT, has_kitchen_furniture INT, has_internet INT, has_no_furniture INT, has_phone INT, has_refrigerator INT, has_room_furniture INT, has_television INT, has_washing_machine INT, built_year INT, has_parking INT, has_lift INT, has_rubbish_chute INT, is_guarded INT, balcony INT, bathroom_unit INT, creation_date DATE, renovation INT, floor_covering INT, window_view INT, studio INT, open_plan INT);"
    statement.executeUpdate(query)
    query = "COPY realty3_offers FROM '/spb.db.sell.rent.tsv' (DELIMITER E'\\t', NULL \"NULL\");"
    statement.executeUpdate(query)

    val server = HttpServer.create(InetSocketAddress(8000), 0)
    server.createContext("/get") {
        handle(it, statement)
    }
    server.executor = null // creates a default executor
    server.start()
}

fun handle(t: HttpExchange, statement: Statement) {
    val request = IOUtils.toString(t.getRequestBody(), "UTF-8")
    val json = JSONObject(request)

    val minCost = json.optInt("mincost", 0)
    val maxCost = json.optInt("maxcost", 999999999);
    val lift = if (json.optBoolean("lift", false)) "AND lift == 1" else ""
    val count = json.optInt("count", 10);
    //val area = json.getDouble("area")
    //val credit = json.getInt("credit")
    //val metro = json.getInt("metro")
    //val typeDeal = json.getInt("typeDeal")
    //val reasonForPurchase = json.getInt("reasonForPurchase")

    val query = "SELECT offer_id FROM realty3_offers WHERE price >= $minCost AND price <= $maxCost$lift"
    val set = statement.executeQuery(query)


    val builder = StringBuilder()
    builder.append("[")
    for (i in 0..(count - 1)) {
        if (!set.next()) break;
        val url = "https://realty.yandex.ru/offer/${set.getLong("offer_id")}/"
        builder.append(url)
                .append(",")
    }
    builder.append("]")

    val response = builder.toString()
    //val response = request
    t.sendResponseHeaders(200, response.length.toLong())
    IOUtils.write(response, t.getResponseBody(), "UTF-8")
}