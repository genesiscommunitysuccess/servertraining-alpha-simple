/**
 * System              : Genesis Business Library
 * Sub-System          : multi-pro-code-test Configuration
 * Version             : 1.0
 * Copyright           : (c) Genesis
 * Date                : 2022-03-18
 * Function : Provide event handler config for multi-pro-code-test.
 *
 * Modification History
 */
import genesis.global.message.event.*
import global.genesis.jackson.core.GenesisJacksonMapper
import java.io.File
import java.time.LocalDate
import global.genesis.commons.standards.GenesisPaths


eventHandler {

    eventHandler<Trade>(name = "TRADE_INSERT") {
        schemaValidation = false

        onValidate { event ->
            val message = event.details
            verify {
                entityDb hasEntry Counterparty.byId(message.counterpartyId.toString())
                entityDb hasEntry Instrument.byId(message.instrumentId.toString())
            }
            ack()
        }

        onCommit { event ->
            val trade = event.details

            if (trade.quantity!! > 0) {
                trade.enteredBy = event.userName
                entityDb.insert(trade)
                ack()
            }
            else {
                nack("Quantity must be positive")
            }
        }
    }

    eventHandler<Trade>(name = "TRADE_MODIFY", transactional = true) {
        onValidate { event ->
            val message = event.details
            verify {
                entityDb hasEntry Counterparty.ById(message.counterpartyId.toString())
                entityDb hasEntry Instrument.byId(message.instrumentId)
            }
            ack()
        }
        onCommit { event ->
            val trade = event.details
            entityDb.modify(trade)
            ack()
        }
    }

    eventHandler<Trade>(name = "TRADE_DELETE", transactional = true) {
        onCommit { event ->
            val trade = event.details
            entityDb.delete(trade)
            ack()
        }
    }

    eventHandler<Counterparty>(name = "COUNTERPARTY_INSERT") {
        schemaValidation = false
        onCommit { event ->
            entityDb.insert(event.details)
            ack()
        }
    }

    eventHandler<Counterparty>(name = "COUNTERPARTY_MODIFY") {
        onCommit { event ->
            entityDb.modify(event.details)
            ack()
        }
    }

    eventHandler<Counterparty>(name = "COUNTERPARTY_DELETE") {
        onCommit { event ->
            entityDb.delete(event.details)
            ack()
        }
    }

    eventHandler<Instrument>(name = "INSTRUMENT_INSERT") {
        schemaValidation = false
        onCommit { event ->
            entityDb.insert(event.details)
            ack()
        }
    }

    eventHandler<Instrument>(name = "INSTRUMENT_MODIFY") {
        onCommit { event ->
            entityDb.modify(event.details)
            ack()
        }
    }

    eventHandler<Instrument>(name = "INSTRUMENT_DELETE") {
        onCommit { event ->
            entityDb.delete(event.details)
            ack()
        }
    }

    eventHandler<PositionReport>(name = "POSITION_REPORT") {
        onCommit {
            val mapper = GenesisJacksonMapper.csvWriter<TradeView>()
            val today = LocalDate.now().toString()
            val positionReportFolder = File(GenesisPaths.runtime()).resolve("position-minute-report")
            if (!positionReportFolder.exists()) positionReportFolder.mkdirs()

            entityDb.getBulk(TRADE_VIEW)
                .toList()
                .groupBy { it.counterpartyName }
                .forEach { (counterParty, trades) ->
                    val file = positionReportFolder.resolve("${counterParty}_$today.csv")
                    if (file.exists()) file.delete()
                    mapper.writeValues(file).use { it.writeAll(trades) }
                }

            ack()
        }
    }


}