import global.genesis.commons.standards.GenesisPaths
import global.genesis.message.core.HttpStatusCode
import java.nio.file.Paths
import java.io.File


webHandlers("BASE-PATH"){
    multipartEndpoint("FILE_UPLOAD"){
        val fileUploadFolder = "training/files"
        config {
            multiPart {
                maxFileSize = 10_000_000
                useDisk = true
                baseDir = fileUploadFolder
            }
        }

        val savedFolder = Paths.get(GenesisPaths.runtime() + "/" + fileUploadFolder)

        handleRequest {
            body.fileUploads.forEach {
                val file = File("$savedFolder/${it.fileName}")
                require(!file.exists()) { "File already exists" }
                it.copyTo(savedFolder.resolve(it.fileName))
            }
        }
        exceptionHandler<IllegalArgumentException>(HttpStatusCode.Forbidden){
            exception.message ?: "Error"
        }
    }

    endpoint(GET, "ALL-TRADES"){
        val searchTradeId by header("SEARCH_TRADE_ID")
        handleRequest {
            require(searchTradeId != "1") { "searchTradeId cannot be 1"}
            db.get(Trade.byId(searchTradeId))
        }
        exceptionHandler<IllegalArgumentException>{
            exception.message ?: "Error performing the search"
        }
    }

    endpoint(POST,"DELETE_FILE"){
        val fileUploadFolder = "training/files"
        val savedFolder = Paths.get(GenesisPaths.runtime() + "/" + fileUploadFolder)
        val fileName by queryParameter("name")

        handleRequest {
            val file = File("$savedFolder/${fileName}")
            require(file.exists()) { "File: $fileName not found" }
            file.delete()
        }

        exceptionHandler<IllegalArgumentException>(HttpStatusCode.NotFound){
            exception.message ?: "Error"
        }

    }

}