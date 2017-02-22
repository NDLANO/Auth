/*
 * Part of NDLA auth.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.auth

import com.typesafe.scalalogging.LazyLogging
import no.ndla.network.secrets.Secrets._

import scala.util.Properties._
import scala.util.{Failure, Success}

object AuthProperties extends LazyLogging {
  val ApplicationPort = 80
  val ContactEmail = "christergundersen@ndla.no"
  val HealthControllerPath = "/health"
  val CorrelationIdKey = "correlationID"
  val CorrelationIdHeader = "X-Correlation-ID"

  val Auth0ClientIdKey = "AUTH0_CLIENT_ID"
  val Auth0DomainKey = "AUTH0_DOMAIN"
  val Auth0ScopeKey = "AUTH0_SCOPE"


  val SecretsFile = "auth.secrets"
  val ApiSecretKeys = Set(Auth0ClientIdKey, Auth0DomainKey, Auth0ScopeKey)

  val Environment: String = propOrElse("NDLA_ENVIRONMENT", "local")
  val Auth0ClientId: String = prop(Auth0ClientIdKey)
  val Auth0Domain: String = prop(Auth0DomainKey)
  val Auth0Scope: String = prop(Auth0ScopeKey)

  val TokenValidityInSeconds:Long = 60 * 60

  lazy private val secrets = readSecrets(SecretsFile, ApiSecretKeys) match {
     case Success(values) => values
     case Failure(exception) => throw new RuntimeException(s"Unable to load remote secrets from $SecretsFile", exception)
   }

  def prop(key: String): String = {
    propOrElse(key, throw new RuntimeException(s"Unable to load property $key"))
  }

  def propOrElse(key: String, default: => String): String = {
    secrets.get(key).flatten match {
      case Some(secret) => secret
      case None =>
        envOrNone(key) match {
          case Some(env) => env
          case None => default
        }
    }
  }
}
