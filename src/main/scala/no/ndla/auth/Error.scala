package no.ndla.auth

import java.text.SimpleDateFormat
import java.util.Date

object Error {
    val GENERIC = "1"
    val AUTHENTICATION = "2"

    val GenericError = Error(GENERIC, s"Ooops. Something we didn't anticipate occurred. We have logged the error, and will look into it.")
}

case class Error(code: String, description: String, occurredAt: String = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()))
