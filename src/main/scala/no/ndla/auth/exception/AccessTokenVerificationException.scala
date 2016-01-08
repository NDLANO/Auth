package no.ndla.auth.exception

case class AccessTokenVerificationException(message: String) extends RuntimeException(message)
