package genesis.global.eventhandler.commit


import global.genesis.db.rx.entity.multi.AsyncMultiEntityReadWriteGenericSupport
import global.genesis.gen.dao.Instrument
import global.genesis.message.core.event.Event

class CommitInstrument {
    companion object{
        suspend fun upsert(
            event: Event<Instrument>,
            entityDb: AsyncMultiEntityReadWriteGenericSupport
        )
        {
            val instrumentId = event.details.instrumentId

            val instrument = entityDb.get(Instrument.byId(instrumentId))

            // if instrument null then insert in database else it will modify
            if (instrument == null) {
                entityDb.insert(event.details)
            } else {
                entityDb.modify(event.details)
            }
        }

    }
}