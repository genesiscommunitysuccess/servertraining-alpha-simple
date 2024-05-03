package genesis.global.eventhandler.validate

import global.genesis.db.rx.entity.multi.AsyncMultiEntityReadWriteGenericSupport
import global.genesis.gen.dao.Counterparty
import global.genesis.gen.dao.Trade
import global.genesis.message.core.event.Event
import kotlinx.coroutines.flow.toList

class ValidateCounterparty {
    companion object {
        suspend fun validateDelete(
            event: Event<Counterparty>,
            entityDb: AsyncMultiEntityReadWriteGenericSupport
        ) {
            val counterpartyId = event.details.counterpartyId

            val trade = entityDb.getRange(Trade.byCounterpartyId(counterpartyId)).toList()
            val counterparty = entityDb.get(Counterparty.byId(counterpartyId))

            require(counterparty != null) { "Counterparty not found" }
            require(trade.firstOrNull() == null) { "Trades are using this Counterparty. Unable to delete" }


        }
        suspend fun validateCascadeDelete(
            event: Event<Counterparty>,
            entityDb: AsyncMultiEntityReadWriteGenericSupport
        ) : List<Trade>{
            val counterpartyId = event.details.counterpartyId

            val trades = entityDb.getRange(Trade.byCounterpartyId(counterpartyId)).toList()

            require (trades.count() <= 10){
                "exceed 10 trades to be deleted in cascade"
            }
            return trades
        }
    }
}