import global.genesis.commons.standards.GenesisPaths

pipelines{
    csvSource("cp-pipeline"){
        location = "file:${GenesisPaths.runtime()}/fileIngress?fileName=cp-pipeline.csv"

        map("mapper-name",COUNTERPARTY){
            COUNTERPARTY{
                COUNTERPARTY_ID{
                    property = "ID"
                }
                COUNTERPARTY_LEI{
                    property = "Counterparty_LEI"
                }
                COUNTERPARTY_NAME{
                    property = "Counterparty_Name"
                }
            }
        }

        onCompletion {
            LOG.info("CP pipeline completed")
        }
    }
}