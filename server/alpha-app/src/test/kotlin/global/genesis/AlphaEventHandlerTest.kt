package global.genesis

import global.genesis.commons.model.GenesisSet
import global.genesis.db.DbRecord
import global.genesis.dictionary.GenesisDictionary
import global.genesis.gen.dao.Trade
import global.genesis.gen.dao.enums.Direction
import global.genesis.gen.dao.enums.TradeStatus
import global.genesis.message.core.event.Event
import global.genesis.message.core.event.EventReply
import global.genesis.testsupport.AbstractGenesisTestSupport
import global.genesis.testsupport.GenesisTestConfig
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.inject.Inject

class AlphaEventHandlerTest : AbstractGenesisTestSupport<GenesisSet>(

    GenesisTestConfig {
        packageName = "global.genesis.eventhandler.pal"
        genesisHome = "/genesisHome/"
        scriptFileName = "alpha-eventhandler.kts"
        parser = { it }
        initialDataFile = "data/TEST_DATA_EVENTHANDLER.csv"
        addAuthCacheOverride("ENTITY_VISIBILITY")
    }
) {
    override fun systemDefinition(): Map<String, Any> = mapOf("IS_SCRIPT" to "true")

    @BeforeEach
    fun setUp() {
        authorise("ENTITY_VISIBILITY", "1", "JaneDee")

        val trader = DbRecord.dbRecord("RIGHT_SUMMARY") {
            "USER_NAME" with "JaneDee"
            "RIGHT_CODE" with "INSERT_TRADE"
        }
        rxDb.insert(trader).blockingGet()
    }

    @Test
    fun `test insert trade`(): Unit = runBlocking {
        val message = Event(
            details = Trade {
                counterpartyId = "1"
                instrumentId = "2"
                direction = Direction.BUY
                price = 1.123
                quantity = 1000
                enteredBy = "JohnDoe"
                tradeDate = DateTime.now()
            },
            messageType = "EVENT_TRADE_INSERT",
            userName = "JaneDee"
        )

        val result: EventReply? = messageClient.suspendRequest(message)

        result.assertedCast<EventReply.EventAck>()
        val trades = entityDb.getBulk<Trade>().toList()
        val trade = trades[5]
        assertNotNull(trade)
        assertEquals(6, trades.size)
        assertEquals("1", trade.counterpartyId)
        assertEquals("2", trade.instrumentId)
        assertEquals(TradeStatus.NEW, trade.tradeStatus)
        assertEquals(Direction.BUY, trade.direction)
        assertEquals(1.123, trade.price)
        assertEquals(1000, trade.quantity)
    }
}