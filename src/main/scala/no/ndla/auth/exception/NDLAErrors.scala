package no.ndla.auth.exception

case class AccessTokenVerificationException(message: String) extends RuntimeException(message)
case class IllegalStateException(message: String = null, cause: Throwable = null) extends RuntimeException(message, cause)
case class IllegalStateFormatException(message: String = null, cause: Throwable = null) extends RuntimeException(message, cause)
