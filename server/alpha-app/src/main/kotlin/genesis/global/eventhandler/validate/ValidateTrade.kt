package genesis.global.eventhandler.validate

import global.genesis.db.rx.entity.multi.AsyncMultiEntityReadWriteGenericSupport
import global.genesis.gen.dao.Counterparty
import global.genesis.gen.dao.Instrument
import global.genesis.gen.dao.Trade
import global.genesis.message.core.event.Event

class ValidateTrade {

    companion object{
        suspend fun validateInsert(
            event: Event<Trade>,
            entityDb: AsyncMultiEntityReadWriteGenericSupport
        ){
            val counterpartyId = event.details.counterpartyId!!
            val instrumentId = event.details.instrumentId

            val counterparty = entityDb.get(Counterparty.byId(counterpartyId))
            val instrument = entityDb.get(Instrument.byId(instrumentId))

            require(instrument != null){"Instrument not found"}
            require(counterparty != null){"Counterparty not found"}

        }
        suspend fun validateUpsert(
            event: Event<Trade>,
            entityDb: AsyncMultiEntityReadWriteGenericSupport
        ) {
            val tradeId = event.details.tradeId
            val counterpartyId = event.details.counterpartyId!!
            val instrumentId = event.details.instrumentId

            val trade = entityDb.get(Trade.byId(tradeId))
            val instrument = entityDb.get(Instrument.byId(instrumentId))
            val counterparty = entityDb.get(Counterparty.byId(counterpartyId))

            if (trade == null){
                validateInsert(event, entityDb)
            } else {
                require(instrument != null) { "INSTRUMENT not found" }
                require(counterparty != null) { "COUNTERPARTY not found" }
            }
        }
    }
}