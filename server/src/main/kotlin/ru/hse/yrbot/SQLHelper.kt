package ru.hse.yrbot

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.PrintWriter
import java.sql.DriverManager
import java.sql.Statement
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class SQLHelper(host: String, port: Int, password: String) {
    private val statement: Statement

    init {
        val manager = DriverManager.getConnection("jdbc:postgresql://$host:$port/yrealty", "yrealty", password)
        statement = manager.createStatement()

        var query = "DROP TABLE IF EXISTS realty3_offers, realty3_offers_extended CASCADE;"
        statement.executeUpdate(query)
        query = "CREATE TABLE realty3_offers (offer_id BIGINT PRIMARY KEY, unified_address TEXT, area FLOAT, total_price_in_rubbles INT, rooms INT, partner_id BIGINT, partner_name TEXT, agent_fee FLOAT, latitude FLOAT, longitude FLOAT, metro_geo_id INT, time_to_metro INT, floor INT, floors_total INT, price INT, kitchen_area FLOAT, ceiling_height FLOAT, has_kitchen_furniture INT, has_internet INT, has_no_furniture INT, has_phone INT, has_refrigerator INT, has_room_furniture INT, has_television INT, has_washing_machine INT, built_year INT, has_parking INT, has_lift INT, has_rubbish_chute INT, is_guarded INT, balcony INT, bathroom_unit INT, creation_date DATE, renovation INT, floor_covering INT, window_view INT, studio INT, open_plan INT);"
        statement.executeUpdate(query)
        query = "COPY realty3_offers FROM '/spb.db.sell.rent.tsv' (DELIMITER E'\\t', NULL \"NULL\");"
        statement.executeUpdate(query)

        query = "CREATE TABLE realty3_offers_extended (offer_id BIGINT PRIMARY KEY, area FLOAT, rooms INT, floor INT, price INT, has_lift INT, rgid INT, metro TEXT);"
        statement.executeUpdate(query)
    }

    fun index(executor: Executor) {
        var query = "SELECT offer_id, area, rooms, floor, price, has_lift FROM realty3_offers;"

        var ct = 0
        val set = statement.executeQuery(query)

        var listOfEntries: List<Map<String, Any>> = emptyList()

        while (set.next()) {
            val id = set.getLong("offer_id")
            val area = set.getFloat("area")
            val rooms = set.getInt("rooms")
            val floor = set.getInt("floor")
            val price = set.getInt("price")
            val lift = set.getInt("has_lift")
            listOfEntries += mapOf(Pair("offer_id", id),
                    Pair("area", area),
                    Pair("rooms", rooms),
                    Pair("floor", floor),
                    Pair("price", price),
                    Pair("lift", lift))
        }

        for (entry in listOfEntries) {
            executor.execute {
                val id = entry.get("offer_id") as Long
                val area = entry.get("area") as Float?
                val rooms = entry.get("rooms") as Int?
                val floor = entry.get("floor") as Int?
                val price = entry.get("price") as Int?
                val lift = entry.get("has_lift") as Int?

                val info = getInfoJSONById(id)
                val location = info.getJSONObject("location")

                if (info.optString("offerType") == "SELL" && info.optString("offerCategory") == "APARTMENT"
                        && info.optBoolean("active", false) && checkLocation(location)) {


                    val rgid = location.getInt("rgid")
                    val station = location.optJSONObject("metro")?.getString("name")


                    query = "INSERT INTO realty3_offers_extended (offer_id, area, rooms, floor, price, has_lift, rgid, metro) VALUES ($id, $area, $rooms, $floor, $price, $lift, $rgid, '$station');"
                    print(query)
                    statement.executeUpdate(query)

                    val out = PrintWriter(File("out.log"))
                    out.println("$ct: INSERT $id")
                    out.close()
                    ct++
                }
            }
        }
    }

    fun query(request: String): String {
        val json = JSONObject(request)
        val minCost = json.optInt("minCost", 0)
        val maxCost = json.optInt("maxCost", 999999999);

        val minCountRooms = json.optInt("minCountRooms", 0)
        val maxCountRooms = json.optInt("maxCountRooms", 999999999);

        val lift = if (json.optBoolean("lift", false))
            "AND has_lift=1"
        else if (!json.optBoolean("lift", true))
            "AND has_lift=0"
        else
            ""

        var count = json.optInt("count", 10);
        var wantedFloor = json.optJSONArray("wantedFloor") ?. toList() ?: emptyList()
        if (wantedFloor.isEmpty())
            wantedFloor = (1..38).toList()
        val unwantedFloor = json.optJSONArray("unwantedFloor") ?. toList() ?: emptyList()
        wantedFloor.removeAll(unwantedFloor)

        val floors = buildString {
            append("AND (")
            append("floor = ${wantedFloor.first()}")
            for (level in wantedFloor.drop(1))
                append("OR floor = $level")
            append(")")
        }

        val districts = json.optJSONArray("district")
                ?. toList()
                ?. map { "rgid=${getRegionIdByName(it as String)}" }
                ?. joinToString(prefix = "AND (", separator = " OR ", postfix = ")")
                ?: ""

        val metros = json.optJSONArray("metro")
                ?. toList()
                ?. map { "metro=\'$it\'"}
                ?. joinToString(prefix = "AND (", separator = " OR ", postfix = ")")
                ?: ""

        //val area = json.getDouble("area")
        //val credit = json.getInt("credit")
        //val metro = json.getInt("metro")
        //val typeDeal = json.getInt("typeDeal")
        //val reasonForPurchase = json.getInt("reasonForPurchase")

        val query = "SELECT offer_id, price, has_lift, floor FROM realty3_offers_extended WHERE price >= $minCost AND price <= $maxCost AND rooms >= $minCountRooms AND rooms <= $maxCountRooms $lift $floors $districts $metros"
        val set = statement.executeQuery(query)

        var array = JSONArray()
        while (set.next()) {
            val offerId = set.getLong("offer_id")
            /*val info = getInfoJSONById(offerId)

            if (info.optString("offerType") != "SELL") continue

            val location = info.optJSONObject("location")

            val districtMatches = districts.contains(location ?. optInt("rgid") ?. toString() ?: "417899")
            val metroMatches = location ?. optJSONArray("metroList") ?. toList() ?. filter { metros.contains((it as JSONObject).getString("name")) } ?. isEmpty() ?: false

            if (!(metroMatches || districtMatches) && !(metros.isEmpty() && districts.isEmpty())) continue*/

            val obj = JSONObject()
                    .put("url", "https://realty.yandex.ru/offer/$offerId/")
                    .put("cost", set.getInt("price"))
                    .put("lift", set.getInt("has_lift"))
                    .put("floor", set.getInt("floor"))
            array = array.put(obj)
            if (--count == 0) break
        }

        return array.toString()
    }
}