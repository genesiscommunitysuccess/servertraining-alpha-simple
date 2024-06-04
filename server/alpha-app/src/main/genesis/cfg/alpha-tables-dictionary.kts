/**
 * System              : Genesis Business Library
 * Sub-System          : multi-pro-code-test Configuration
 * Version             : 1.0
 * Copyright           : (c) Genesis
 * Date                : 2022-03-18
 * Function : Provide table definition config for multi-pro-code-test.
 *
 * Modification History
 */

tables {

    table (name = "TRADE", id = 2000,audit = details(id = 2100, sequence = "TR", tsKey = true)) {

        field("TRADE_ID", type = STRING).sequence("TR").primaryKey()
        field("QUANTITY", type = INT)
        field("PRICE", type = DOUBLE).notNull()
        field("SYMBOL", type = STRING).uniqueIndex()
        field("DIRECTION", type = ENUM("BUY", "SELL")).default("BUY")
        field("TRADE_DATE", type = DATE)
        field("ENTERED_BY", type = STRING)
        field("TRADE_STATUS", type = ENUM("NEW", "ALLOCATED", "CANCELLED")).default("NEW")
        field("BEEN_AUDITED", type= BOOLEAN)
        field("CURRENCY_ID", type= INT).nonUniqueIndex()
        field("INSTRUMENT_ID", type = STRING).notNull().nonUniqueIndex()
        field("COUNTERPARTY_ID", type = STRING).notNull().nonUniqueIndex()

    }

    table (name = "COUNTERPARTY", id = 2001) {
        field("COUNTERPARTY_ID", type = STRING).sequence("CP").primaryKey()
        field("COUNTERPARTY_NAME", type = STRING)
        field("ENABLED", type = BOOLEAN)
        field("COUNTERPARTY_LEI", type = STRING)

    }

    table (name = "INSTRUMENT", id = 2002) {
        field("INSTRUMENT_ID", type = STRING).sequence("IN").primaryKey()
        field("INSTRUMENT_NAME", type = STRING)
        field("MARKET_ID", type = STRING)
        field("COUNTRY_CODE", type = STRING)
        field("ASSET_CLASS", type = STRING)
        field("CURRENCY_ID", type= INT)
    }

    table(name = "POSITION", id = 2003) {
        field("POSITION_ID", type = STRING).sequence("PO").primaryKey()
        field("NOTIONAL", type = DOUBLE)
        field("LAST_PRICE", type = DOUBLE)
        field("VALUE", type = DOUBLE)
        field("PNL", type = DOUBLE)
        field("INSTRUMENT_ID", type = STRING).uniqueIndex()
        field("QUANTITY", type = INT)
    }

    table(name = "INSTRUMENT_PRICE", id = 2004) {
        field("INSTRUMENT_ID", type = STRING).primaryKey()
        field("LAST_PRICE", type = DOUBLE)
    }

    // create a company table
    table(name = "COMPANY", id = 2005) {
        field("COMPANY_ID", type = STRING).sequence("CO").primaryKey()
        field("COMPANY_NAME", type = STRING)
        field("COMPANY_LEI", type = STRING)
        field("COMPANY_CEO", type = STRING)
        field(name = "COMPANY_STATUS", type = ENUM("NEW", "OPENED", "CLOSED")).default("NEW")
    }

    table (name = "USER_COUNTERPARTY_HIDE_LEI", id = 2010){
        field("USER_COUNTERPARTY_HIDE_LEI_ID", type= STRING).sequence("UC").primaryKey()
        field("USER_NAME_COUNTERPARTY", type= STRING)
        field("HIDE_LEI", type= BOOLEAN)
        field("COUNTERPARTY_ID", type = STRING)
        indices {
            unique("USER_NAME_COUNTERPARTY","COUNTERPARTY_ID")
        }
    }

    table (name = "FAVOURITE_TRADE", id = 2011){
        field("FAVOURITE_TRADE_ID", type= STRING).sequence("FT").primaryKey()
        field("TRADE_ID", type= STRING)
        field("IS_FAVOURITE", type = BOOLEAN)
        field("USER", type = STRING)
        indices{
            unique("USER","TRADE_ID")
        }
    }
}