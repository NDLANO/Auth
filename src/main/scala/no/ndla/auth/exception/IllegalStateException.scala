package no.ndla.auth.exception

case class IllegalStateException(message: String = null, cause: Throwable = null) extends RuntimeException(message, cause)
