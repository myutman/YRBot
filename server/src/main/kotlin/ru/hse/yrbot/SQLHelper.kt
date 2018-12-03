package ru.hse.yrbot

import org.json.JSONArray
import org.json.JSONObject
import java.sql.DriverManager
import java.sql.Statement

class SQLHelper(host: String, port: Int, password: String) {
    private val statement: Statement

    init {
        val manager = DriverManager.getConnection("jdbc:postgresql://$host:$port/yrealty", "yrealty", password)
        statement = manager.createStatement()

        var query = "DROP TABLE IF EXISTS realty3_offers CASCADE;"
        statement.executeUpdate(query)
        query = "CREATE TABLE realty3_offers (offer_id BIGINT PRIMARY KEY, unified_address TEXT, area FLOAT, total_price_in_rubbles INT, rooms INT, partner_id BIGINT, partner_name TEXT, agent_fee FLOAT, latitude FLOAT, longitude FLOAT, metro_geo_id INT, time_to_metro INT, floor INT, floors_total INT, price INT, kitchen_area FLOAT, ceiling_height FLOAT, has_kitchen_furniture INT, has_internet INT, has_no_furniture INT, has_phone INT, has_refrigerator INT, has_room_furniture INT, has_television INT, has_washing_machine INT, built_year INT, has_parking INT, has_lift INT, has_rubbish_chute INT, is_guarded INT, balcony INT, bathroom_unit INT, creation_date DATE, renovation INT, floor_covering INT, window_view INT, studio INT, open_plan INT);"
        statement.executeUpdate(query)
        query = "COPY realty3_offers FROM '/spb.db.sell.rent.tsv' (DELIMITER E'\\t', NULL \"NULL\");"
        statement.executeUpdate(query)
    }

    fun query(request: String): String {
        val json = JSONObject(request)
        val minCost = json.optInt("mincost", 0)
        val maxCost = json.optInt("maxcost", 999999999);
        val lift = if (json.optBoolean("lift", false)) "AND has_lift == 1" else ""
        val count = json.optInt("count", 10);
        //val area = json.getDouble("area")
        //val credit = json.getInt("credit")
        //val metro = json.getInt("metro")
        //val typeDeal = json.getInt("typeDeal")
        //val reasonForPurchase = json.getInt("reasonForPurchase")

        val query = "SELECT offer_id, price, has_lift, floor FROM realty3_offers WHERE price >= $minCost AND price <= $maxCost$lift"
        val set = statement.executeQuery(query)


        var array = JSONArray()
        for (i in 0..(count - 1)) {
            if (!set.next()) break
            val obj = JSONObject()
                    .put("url", "https://realty.yandex.ru/offer/${set.getLong("offer_id")}/")
                    .put("cost", set.getInt("price"))
                    .put("lift", set.getInt("has_lift"))
                    .put("floor", set.getInt("floor"))
            array = array.put(obj)
        }

        return array.toString()
    }
}