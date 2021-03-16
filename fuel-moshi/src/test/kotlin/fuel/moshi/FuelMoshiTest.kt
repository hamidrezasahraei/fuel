package fuel.moshi

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Types
import fuel.Fuel
import fuel.get
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test

internal class FuelMoshiTest {

    @JsonClass(generateAdapter = true)
    data class HttpBinUserAgentModel(var userAgent: String = "")

    @JsonClass(generateAdapter = true)
    data class Card(val rank: Char, val suit: String)

    @Test
    fun testMoshiResponse() {
        val mockWebServer = startMockServerWithBody("{\"userAgent\": \"Fuel\"}")

        val response = Fuel.get(mockWebServer.url("user-agent")).execute()
        val moshi = response.toMoshi(HttpBinUserAgentModel::class.java)!!
        assertEquals("Fuel", moshi.userAgent)

        mockWebServer.shutdown()
    }

    @Test
    fun testReifiedTypeMoshiResponse() {
        val mockWebServer = startMockServerWithBody("{\"userAgent\": \"Fuel\"}")

        val response = Fuel.get(mockWebServer.url("user-agent")).execute()
        val moshi = response.toMoshi<HttpBinUserAgentModel>()!!
        assertEquals("Fuel", moshi.userAgent)

        mockWebServer.shutdown()
    }

    @Test
    fun testMoshiGenericList() {
        val mockWebServer = startMockServerWithBody(
            "[{" +
                "    \"rank\": \"4\"," +
                "    \"suit\": \"CLUBS\"" +
                "  }, {" +
                "    \"rank\": \"A\"," +
                "    \"suit\": \"HEARTS\"" +
                "  }, {" +
                "    \"rank\": \"J\"," +
                "    \"suit\": \"SPADES\"" +
                "  }" +
                "]"
        )

        val response = Fuel.get(mockWebServer.url("user-agent")).execute()
        val listOfCardsType = Types.newParameterizedType(List::class.java, Card::class.java)
        val cards = response.toMoshi<List<Card>>(listOfCardsType)!!
        assertEquals(3, cards.size)
        assertEquals("CLUBS", cards[0].suit)

        mockWebServer.shutdown()
    }

    @Test
    fun customMoshiAdapterWithGenericList() {
        val mockWebServer = startMockServerWithBody(
            "[{" +
                "    \"rank\": \"1\"," +
                "    \"suit\": \"CLUBS\"" +
                "  }, {" +
                "    \"rank\": \"J\"," +
                "    \"suit\": \"HEARTS\"" +
                "  }, {" +
                "    \"rank\": \"K\"," +
                "    \"suit\": \"SPADES\"" +
                "  }" +
                "]"
        )

        val userAgentResponse = Fuel.get(mockWebServer.url("user-agent")).execute()
        val listOfCardsType = Types.newParameterizedType(List::class.java, Card::class.java)
        val adapter = defaultMoshi.build().adapter<List<Card>>(listOfCardsType)
        val cards = userAgentResponse.toMoshi<List<Card>>(adapter)!!
        assertEquals(3, cards.size)
        assertEquals("CLUBS", cards[0].suit)

        mockWebServer.shutdown()
    }

    private fun startMockServerWithBody(body: String): MockWebServer = MockWebServer().apply {
        enqueue(MockResponse().setBody(body))
        start()
    }
}
