package genesis.global.message.event

import global.genesis.message.core.Outbound

sealed class CustomCounterpartyEventReply: Outbound() {

    class ValidationCounterpartyAck() : CustomCounterpartyEventReply()

    data class CounterpartyAck(val AckMessage : String) : CustomCounterpartyEventReply()
    data class CounterpartyNack(val NackMessage : String) : CustomCounterpartyEventReply()

}