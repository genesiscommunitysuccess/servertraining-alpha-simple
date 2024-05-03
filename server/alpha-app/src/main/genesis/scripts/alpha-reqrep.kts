
data class InputClass(val field1: String, val field2: Int?)
data class OutputClass(val message: String, val details: String)

requestReplies{

    requestReply<InputClass, OutputClass>("CUSTOM_REQUEST"){
        replySingle{ input ->
            OutputClass(input.field1, "This is a number: ${input.field2}")
        }
    }


    requestReply("TRAINING_REQUEST",TRAINING_VIEW)

    requestReply("TRADE_DETAILS",TRADE){
        request{
            COUNTERPARTY_ID withTransformation { type, _ ->
                if (type == "2"){
                    "UNKOWN"
                }else {
                    type
                }
            }
        }
        reply{
            TRADE_ID
            COUNTERPARTY_ID
            INSTRUMENT_ID
            QUANTITY
            PRICE
        }
    }
    requestReply("TRAINING_REQUEST",TRAINING_VIEW)
}

