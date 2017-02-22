/*
 * Part of NDLA auth.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.auth.exception

case class AccessTokenVerificationException(message: String) extends RuntimeException(message)
case class HeaderMissingException(message: String) extends RuntimeException(message)
case class ParameterMissingException(message: String) extends RuntimeException(message)
