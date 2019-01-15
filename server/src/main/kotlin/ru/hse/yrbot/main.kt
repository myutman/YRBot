package ru.hse.yrbot

import com.sun.net.httpserver.HttpServer
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.util.concurrent.Executors

fun main(args: Array<String>) {
    val host = "postgres"
    val port = 5432

    val executor = Executors.newSingleThreadExecutor()

    val helper = SQLHelper(host, port, "mickjagger")
    helper.index(executor)

    var out = PrintWriter(File("out1.log"))
    out.println("Hello")
    out.close()

    val server = HttpServer.create(InetSocketAddress(8000), 0)
    server.createContext("/get") {
        val request = IOUtils.toString(it.getRequestBody(), "UTF-8")
        val response = helper.query(request)

        out = PrintWriter(File("out1.log"))
        out.println("Request: $request")
        out.close()

        it.sendResponseHeaders(200, response.length.toLong())
        IOUtils.write(response, it.getResponseBody(), "UTF-8")
    }
    server.executor = null
    server.start()

    out = PrintWriter(File("out1.log"))
    out.println("Started")
    out.close()
}