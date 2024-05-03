package scripts

/**
 *
 *   System              : alpha
 *   Sub-System          : alpha Configuration
 *   Version             : 1.0
 *   Copyright           : (c) GENESIS
 *   Date                : 2021-09-07
 *
 *   Function : Provide Data Server Configuration for alpha.
 *
 *   Modification History
 *
 */

dataServer {
    query("ALL_TRADES", TRADE_VIEW) {
    }
    query("ALL_PRICES", TRADE){
        fields{
            PRICE
            SYMBOL
        }
        where { trade -> trade.price > 0.0 }
    }
    query("ALL_INSTRUMENTS", INSTRUMENT)
    query("ALL_COUNTERPARTIES", COUNTERPARTY){
        enrich(USER_COUNTERPARTY_HIDE_LEI){
            join{ userName, row ->
                UserCounterpartyHideLei.byUserNameCounterpartyCounterpartyId(userName, row.counterpartyId)
            }

            hideFields{ counterparty, row, userData ->
                if(userData?.hideLei == true){
                    listOf(COUNTERPARTY_LEI)
                }else{
                    emptyList()
                }
            }

            fields{
                USER_COUNTERPARTY_HIDE_LEI.HIDE_LEI
                derivedField("IS_HIDDEN", BOOLEAN){ row, userData ->
                    row.counterpartyId == userData?.counterpartyId
                }
            }
        }
    }
    query("ALL_POSITIONS", POSITION)
    query("ALL_TRADES_AUDIT",TRADE_AUDIT)
    query("ALL_FAVOURITE_TRADES", TRADE){
        enrich(FAVOURITE_TRADE){
            join{ userName, row ->
                FavouriteTrade.ByUserTradeId(userName, row.tradeId)
            }
        }
    }
}