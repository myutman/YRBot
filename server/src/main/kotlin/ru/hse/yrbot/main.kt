package ru.hse.yrbot

import com.sun.net.httpserver.HttpServer
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.PrintWriter
import java.net.InetSocketAddress

fun main(args: Array<String>) {
    println("It's alive")
    val host = "postgres"
    val port = 5432

    val helper = SQLHelper(host, port, "mickjagger")
    val out = PrintWriter(File("out.log"))

    val server = HttpServer.create(InetSocketAddress(8000), 0)
    server.createContext("/get") {
        val request = IOUtils.toString(it.getRequestBody(), "UTF-8")

        out.println(request)
        val response = helper.query(request)

        it.sendResponseHeaders(200, response.length.toLong())
        IOUtils.write(response, it.getResponseBody(), "UTF-8")
    }
    server.executor = null // creates a default executor
    server.start()
}