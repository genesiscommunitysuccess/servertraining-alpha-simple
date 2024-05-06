streams {
    stream("TRADE_AUDIT_STREAM", TRADE_AUDIT.BY_TIMESTAMP){
        fields{
            TRADE.TRADE_ID
        }
    }
}