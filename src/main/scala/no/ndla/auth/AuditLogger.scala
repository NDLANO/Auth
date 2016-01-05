package no.ndla.auth

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

object AuditLogger {
    private val auditLogger = Logger(LoggerFactory.getLogger("AUDIT"))

    def logAudit(message: String): Unit = {
        auditLogger.error(message)
    }
}
