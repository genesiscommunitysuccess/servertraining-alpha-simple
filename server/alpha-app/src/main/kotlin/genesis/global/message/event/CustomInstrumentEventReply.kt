package genesis.global.message.event

import global.genesis.message.core.Outbound

sealed class CustomInstrumentEventReply: Outbound() {
    class ValidationInstrumentAck() : CustomInstrumentEventReply()

    data class InstrumentAck(val AckMessage : String) : CustomInstrumentEventReply()
    data class InstrumentNack(val NackMessage : String) : CustomInstrumentEventReply()
    data class InstrumentInsertError(val InstrumentID: String) : CustomInstrumentEventReply()
}