package genesis.global.eventhandler.commit


import global.genesis.db.rx.entity.multi.AsyncMultiEntityReadWriteGenericSupport
import global.genesis.gen.dao.Counterparty
import global.genesis.message.core.event.Event

class CommitCounterparty {
    companion object{
        suspend fun upsert(
            event: Event<Counterparty>,
            entityDb: AsyncMultiEntityReadWriteGenericSupport
        )
        {
            val counterpartyId = event.details.counterpartyId

            val counterparty = entityDb.get(Counterparty.byId(counterpartyId))

            // if counterparty null then insert in database else it will modify
            if (counterparty == null) {
                entityDb.insert(event.details)
            } else {
                entityDb.modify(event.details)
            }
        }

    }
}