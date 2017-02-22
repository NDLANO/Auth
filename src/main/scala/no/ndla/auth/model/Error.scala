/*
 * Part of NDLA auth.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.auth.model

import java.text.SimpleDateFormat
import java.util.Date

object Error {
  val GENERIC = "GENERIC"
  val AUTHENTICATION = "AUTHENTICATION"
  val NOT_FOUND = "NOT_FOUND"
  val HEADER_MISSING = "HEADER_MISSING"
  val PARAMETER_MISSING = "PARAMETER_MISSING"
  val VALIDATION = "VALIDATION"

  val GenericError = Error(GENERIC, s"Ooops. Something we didn't anticipate occurred. We have logged the error, and will look into it.")
}

case class Error(code: String, description: String, occurredAt: String = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()))
class ValidationException(message: String) extends RuntimeException(message)
