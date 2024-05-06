import java.io.File
import global.genesis.commons.standards.GenesisPaths
import org.apache.camel.support.processor.idempotent.FileIdempotentRepository

camel {
    val pathFromSftp = "${GenesisPaths.runtime()}inbound/"
    LOG.info("#### About to create or make sure the dir is there: '$pathFromSftp' ####")
    try {
        File(pathFromSftp).mkdirs()
        LOG.info("#### Dir is created as defined: '$pathFromSftp' ####")
    }
    catch (e: Exception) {
        LOG.error("Error on create folder - ${e.message}", e)
    }

    routeHandler {
        val userName = systemDefinition.getItem("SFTP_USERNAME")
        val password = systemDefinition.getItem("SFTP_PASSWORD")
        val pathStrInbound = "${GenesisPaths.runtime()}\\inbound"
        val consumerRepo = "${pathStrInbound}\\IDEMPOTENT_CONSUMER.DATA"

        from("sftp:localhost:22/training?username=${userName}&password=${password}" +
                "&delay=1000&sortBy=file:modified&delete=false&bridgeErrorHandler=true" +
                "&useUserKnownHostsFile=false&throwExceptionOnConnectFailed=true&stepwise=false")
            .idempotentConsumer(header("CamelFileName"),
                FileIdempotentRepository.fileIdempotentRepository(File(consumerRepo), 300000, 15000000))
            .log("ALPHA file transfer: \${headers.CamelFileName}")
            .to("file:${pathStrInbound}")
    }
}