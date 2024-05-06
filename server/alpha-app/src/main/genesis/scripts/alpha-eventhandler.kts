/**
 * System              : Genesis Business Library
 * Sub-System          : multi-pro-code-test Configuration
 * Version             : 1.0
 * Copyright           : (c) Genesis
 * Date                : 2022-03-18
 * Function : Provide event handler config for multi-pro-code-test.
 *
 * Modification History
 */

import global.genesis.jackson.core.GenesisJacksonMapper
import java.io.File
import java.time.LocalDate
import global.genesis.commons.standards.GenesisPaths
import genesis.global.message.event.*
import genesis.global.eventhandler.validate.*
import genesis.global.eventhandler.commit.*
import global.genesis.message.core.event.ApprovalType

eventHandler {

    eventHandler<Trade, CustomTradeEventReply>(name = "TRADE_INSERT") {

        onException{ event, throwable ->
            CustomTradeEventReply.TradeNck("ERROR: ${throwable.message}")
        }

        onValidate { event ->
            ValidateTrade.validateInsert(event,entityDb)
            CustomTradeEventReply.ValidationTradeAck()
        }
        onCommit { event ->
            CommitTrade.insert(event, entityDb)
            CustomTradeEventReply.TradeAck("TRADE_INSERT_ACK")

        }
    }

    eventHandler<Trade>(name="TRADE_UPSERT"){
        schemaValidation = false
        onValidate {
            ValidateTrade.validateUpsert(it,entityDb)
            ack()
        }
        onCommit {
            CommitTrade.upsert(it,entityDb)
            ack()
        }
    }

    eventHandler<Trade>(name = "TRADE_MODIFY", transactional = true) {
        onValidate { event ->
            val message = event.details
            verify {
                entityDb hasEntry Counterparty.ById(message.counterpartyId.toString())
                entityDb hasEntry Instrument.byId(message.instrumentId)
            }
            ack()
        }
        onCommit { event ->
            val trade = event.details
            entityDb.modify(trade)
            ack()
        }
    }

    eventHandler<Trade>(name = "TRADE_DELETE", transactional = true) {
        onCommit { event ->
            val trade = event.details
            entityDb.delete(trade)
            ack()
        }
    }

    eventHandler<Counterparty, CustomCounterpartyEventReply>(name="COUNTERPARTY_INSERT", transactional = true){
        schemaValidation = false

        onException{ _ , throwable ->
            CustomCounterpartyEventReply.CounterpartyNack("ERROR: ${throwable.message}")
        }
        onValidate {
            require(!it.details.counterpartyId.contains("1234")){
                "COUNTERPARTY_ID cannot contain 1234"
            }
            CustomCounterpartyEventReply.ValidationCounterpartyAck()
        }
        onCommit {
            entityDb.insert(it.details)
            CustomCounterpartyEventReply.CounterpartyAck("Counterparty successfully inserted: ${it.details.counterpartyId}")
        }
    }

    eventHandler<Counterparty>(name = "COUNTERPARTY_MODIFY") {
        onCommit { event ->
            entityDb.modify(event.details)
            ack()
        }
    }

    eventHandler<Counterparty>(name = "COUNTERPARTY_DELETE") {
        onValidate{
            ValidateCounterparty.validateDelete(it, entityDb)
            ack()
        }
        onCommit { event ->
            entityDb.delete(event.details)
            ack()
        }
    }

    eventHandler<Counterparty>(name="COUNTERPARTY_UPSERT"){
        schemaValidation = false

        onCommit {
            CommitCounterparty.upsert(it,entityDb)
            ack()
        }
    }

    eventHandler<Instrument>(name="INSTRUMENT_UPSERT"){
        schemaValidation = false
        onCommit {
            CommitInstrument.upsert(it,entityDb)
            ack()
        }
    }

    eventHandler<Instrument, CustomInstrumentEventReply>(name="INSTRUMENT_INSERT", transactional = true){
        schemaValidation = false

        onException{ _ , throwable ->
            CustomInstrumentEventReply.InstrumentNack("ERROR: ${throwable.message}")
        }

        onValidate {
            require(!it.details.instrumentId.contains("1234")){
                "INSTRUMENT_ID cannot contain 1234"
            }
            CustomInstrumentEventReply.ValidationInstrumentAck()
        }
        onCommit {
            entityDb.insert(it.details)
            CustomInstrumentEventReply.InstrumentAck("Instrument successfully inserted: ${it.details.instrumentId}")
        }
    }

    eventHandler<Instrument>(name = "INSTRUMENT_MODIFY") {
        onCommit { event ->
            entityDb.modify(event.details)
            ack()
        }
    }

    eventHandler<Instrument>(name = "INSTRUMENT_DELETE") {
        onValidate{
            ValidateInstrument.validateDelete(it, entityDb)
            ack()
        }
        onCommit { event ->
            entityDb.delete(event.details)
            ack()
        }
    }

    eventHandler<Operations>(name = "OPERATIONS_INSERT") {
        schemaValidation = false
        onCommit {

            //get operation

            //val result = entityDb.get(Trade.byId("123"))

            //getAllAsList
            //val result = entityDb.getAllAsList(Trade.byId("123"),Trade.ById("1234"))

            //getRange
            val result = entityDb.getRange(Trade.byCurrencyId(1), Trade.byCurrencyId(10)).toList()
            LOG.info("***************************** RESULT: ${result} ********************************")
            ack()
        }
    }

    eventHandler<TrainingClass>(name = "TEST"){
        schemaValidation = false
        onCommit{
            ack()
        }
    }

    eventHandler<TradeStandardization>(transactional = true) {
        onCommit {
            val tradesNegativePrices = entityDb.getBulk(TRADE).toList()
                .filter { it.price < 0 }

            tradesNegativePrices.forEach {
                it.price = 0.0
            }

            entityDb.modifyAll(*tradesNegativePrices.toList().toTypedArray())
            ack()
        }
    }

    eventHandler<PositionReport>(name = "POSITION_REPORT") {
        onCommit {
            val mapper = GenesisJacksonMapper.csvWriter<TradeView>()
            val today = LocalDate.now().toString()
            val positionReportFolder = File(GenesisPaths.runtime()).resolve("position-minute-report")
            if (!positionReportFolder.exists()) positionReportFolder.mkdirs()

            entityDb.getBulk(TRADE_VIEW)
                .toList()
                .groupBy { it.counterpartyName }
                .forEach { (counterParty, trades) ->
                    val file = positionReportFolder.resolve("${counterParty}_$today.csv")
                    if (file.exists()) file.delete()
                    mapper.writeValues(file).use { it.writeAll(trades) }
                }

            ack()
        }
    }

    contextEventHandler<Counterparty, List<Trade>>(name = "COUNTERPARTY_DELETE_CASCADE", transactional = true){
        onException{ _ , throwable ->
            nack("ERROR: ${throwable.message}")
        }

        onValidate{ event ->
            val trades = ValidateCounterparty.validateCascadeDelete(event, entityDb)
            validationResult(ack(),trades)
        }

        onCommit{ event, trades ->
            if(trades != null){
                for (trade in trades){
                    LOG.info("Deleting trade ${trade.tradeId}")
                    entityDb.delete(Trade.byId(trade.tradeId))
                }
            }
            entityDb.delete(event.details)
            ack()
        }
    }

    eventHandler<Company> (name = "COMPANY_INSERT", transactional = true){
        onException{ _ , throwable ->
            nack("ERROR: ${throwable.message}")
        }

        requiresPendingApproval { event ->
            event.approvalMessage = "My custom approval message"
            event.userName != "system.user"
        }

        onValidate {
            val company = it.details

            val pendingApprovals = entityDb.getBulk(APPROVAL)
                .filter { it.approvalStatus == ApprovalStatus.PENDING }.toList()


            pendingApprovals.forEach {
                val companyId = it.eventMessage
                    .split("\",\"")
                    .find{ it.contains("COMPANY_ID")}
                    ?.split(":")
                    ?.get(2)
                    ?.removePrefix("\"")
                LOG.info(companyId)
                if (companyId == company.companyId){
                    throw NoSuchElementException ("Company insert for ${company.companyId} has already been sent for approval, choose another company ID")
                }
            }


            approvableAck(
                entityDetails = listOf(
                    ApprovalEntityDetails(
                        entityTable = "COMPANY",
                        entityId = company.companyId,
                        approvalType = ApprovalType.NEW,
                    )
                ),
                approvalMessage = "Company insert for ${company.companyId} has been sent for approval",
                approvalType = ApprovalType.NEW,
                additionalDetails = "Custom additional details"
            )
        }

        onCommit {

            val company = it.details
            entityDb.insert(company)
            LOG.info("Company insert for ${company.companyId} successfully")
            ack()
        }
    }

    eventHandler<TradeAudit>(name = "TRADE_AUDIT_STREAM"){
        schemaValidation = false
        onCommit{ event ->
            val message = event.details
            if(message.beenAudited == false || message.beenAudited == null){
               message.beenAudited = true
                entityDb.modify(message)
                LOG.info("Trade_ID: ${message.tradeId} validated")
            }else {
                LOG.info("Trade_ID: ${message.tradeId} already validated")
            }
            ack()
        }
    }

}