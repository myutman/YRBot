import org.json.JSONObject
import org.junit.Test

import java.io.UnsupportedEncodingException
import java.net.URI
import java.net.URISyntaxException

import org.junit.Assert.assertEquals
import ru.hse.yrbot.splitQuery

class ServerTest {
    @Test
    fun testJson() {
        val b = JSONObject("{\n" +
                "   \"a\":\"b\"\n" +
                "}").getString("a")
        assertEquals("b", b)
    }

    @Test
    @Throws(URISyntaxException::class, UnsupportedEncodingException::class)
    fun testParseArgs() {
        val b = splitQuery(URI("http://lol.kek.co/get?a=b"))["a"]
        assertEquals("b", b)
    }
}
