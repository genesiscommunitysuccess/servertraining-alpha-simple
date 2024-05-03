package genesis.global.eventhandler.validate

import global.genesis.db.rx.entity.multi.AsyncMultiEntityReadWriteGenericSupport
import global.genesis.gen.dao.Instrument
import global.genesis.gen.dao.Trade
import global.genesis.message.core.event.Event
import kotlinx.coroutines.flow.toList

class ValidateInstrument {
    companion object {
        suspend fun validateDelete(
            event: Event<Instrument>,
            entityDb: AsyncMultiEntityReadWriteGenericSupport
        ){
            val instrumentId = event.details.instrumentId

            val trade = entityDb.getRange(Trade.byInstrumentId(instrumentId)).toList().firstOrNull()
            val instrument = entityDb.get(Instrument.byId(instrumentId))

            require (instrument != null) { "Instrument not found" }
            require(trade == null) { "Trades are using this Instrument. Unable to delete" }

        }

        suspend fun validateCascadeDelete(
            event: Event<Instrument>,
            entityDb: AsyncMultiEntityReadWriteGenericSupport
        ): List<Trade>{

            val instrumentId = event.details.instrumentId

            return entityDb.getRange(Trade.byInstrumentId(instrumentId)).toList()

        }
    }
}