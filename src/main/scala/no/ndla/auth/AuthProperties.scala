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

  val ExtraRolesToGrantKey = "EXTRA_ROLES_TO_GRANT"

  val SecretsFile = "auth.secrets"

  val Environment: String = propOrElse("NDLA_ENVIRONMENT", "local")

  val TokenValidityInSeconds:Long = 60 * 60

  val ExtraRolesToGrant = new RoleParser(propOrElse(ExtraRolesToGrantKey, "")).fromJson()

  lazy private val secrets = readSecrets(SecretsFile, Set(ExtraRolesToGrantKey)) match {
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

  case class RoleDef(clientId: String, roles: List[String])
  class RoleParser(jsonAsString: String) {
    import org.json4s.native.Serialization._
    implicit val formats = org.json4s.DefaultFormats

    def fromJson(): Map[String, List[String]] = read[List[RoleDef]](jsonAsString).map(t => t.clientId -> t.roles).toMap
  }
}
