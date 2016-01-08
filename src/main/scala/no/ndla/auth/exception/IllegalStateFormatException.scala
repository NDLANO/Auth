package no.ndla.auth.exception

case class IllegalStateFormatException(message: String = null, cause: Throwable = null) extends RuntimeException(message, cause)
