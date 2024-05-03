package genesis.global.eventhandler.commit

import global.genesis.db.rx.entity.multi.AsyncMultiEntityReadWriteGenericSupport
import global.genesis.gen.dao.Trade
import global.genesis.message.core.event.Event

class CommitTrade {
    companion object{
        suspend fun upsert(
            event: Event<Trade>,
            entityDb: AsyncMultiEntityReadWriteGenericSupport
        )
        {
            val tradeId = event.details.tradeId

            val trade = entityDb.get(Trade.byId(tradeId))

            // if trade null then insert in database else it will modify
            if (trade == null) {
                entityDb.insert(event.details)
            } else {
                entityDb.modify(event.details)
            }
        }

        suspend fun insert(
            event: Event<Trade>,
            entityDb: AsyncMultiEntityReadWriteGenericSupport
        ){
            entityDb.insert(event.details)
        }

    }
}