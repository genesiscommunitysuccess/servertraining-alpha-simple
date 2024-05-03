package genesis.global.message.event

import global.genesis.message.core.Outbound

sealed class CustomTradeEventReply : Outbound(){

    class ValidationTradeAck( ): CustomTradeEventReply()

    data class TradeAck(val AckMessage: String) : CustomTradeEventReply()
    data class TradeNck(val NackMessage: String) : CustomTradeEventReply()
}