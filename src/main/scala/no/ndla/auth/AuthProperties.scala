/*
 * Part of NDLA auth.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.auth

import com.typesafe.scalalogging.LazyLogging
import no.ndla.network.Domains
import no.ndla.network.secrets.PropertyKeys._
import no.ndla.network.secrets.Secrets._

import scala.util.{Failure, Success}
import scala.util.Properties._

object AuthProperties extends LazyLogging {
  val ApplicationPort = 80
  val ContactEmail = "christergundersen@ndla.no"
  val HealthControllerPath = "/health"

  val GoogleClientSecretKey = "GOOGLE_CLIENT_SECRET"
  val GoogleClientIdKey = "GOOGLE_CLIENT_ID"
  val FacebookClientSecretKey = "FACEBOOK_CLIENT_SECRET"
  val FacebookClientIdKey = "FACEBOOK_CLIENT_ID"
  val TwitterApiKeyKey = "TWITTER_API_KEY"
  val TwitterClientSecretKey = "TWITTER_CLIENT_SECRET"

  val SecretsFile = "auth.secrets"
  val ApiSecretKeys = Set(GoogleClientSecretKey, GoogleClientIdKey, FacebookClientSecretKey, FacebookClientIdKey, TwitterApiKeyKey, TwitterClientSecretKey)

  val Environment = propOrElse("NDLA_ENVIRONMENT", "local")

  val Domain = Map(
    "local" -> "http://local.ndla.no"
  ).getOrElse(Environment, Domains.get(Environment))


  val LearningpathFrontendDomain = Map(
    "local" -> "http://localhost:30007",
    "prod" -> "https://learningpath-frontend.api.ndla.no"
  ).getOrElse(Environment, s"https://learningpath-frontend.$Environment.api.ndla.no")

  val WhiteListedSuccessUrls = Map("/login/success/{appkey}" -> s"$LearningpathFrontendDomain/login/success/{appkey}")

  val WhiteListedFailureUrls = Map(
    "/login/failure" -> s"$LearningpathFrontendDomain/login/failure",
    "/" -> s"$LearningpathFrontendDomain/")

  val GoogleClientSecret = prop(GoogleClientSecretKey)
  val GoogleClientId = prop(GoogleClientIdKey)
  val FacebookClientSecret = prop(FacebookClientSecretKey)
  val FacebookClientId = prop(FacebookClientIdKey)
  val TwitterApiKey = prop(TwitterApiKeyKey)
  val TwitterClientSecret = prop(TwitterClientSecretKey)

  val KongAdminPort = 8001
  val KongHostName = "api-gateway.ndla-local"
  val KongUsernamePrefix = "ndla-"

  val CorrelationIdKey = "correlationID"
  val CorrelationIdHeader = "X-Correlation-ID"

  val MetaUserName = prop(MetaUserNameKey)
  val MetaPassword = prop(MetaPasswordKey)
  val MetaResource = prop(MetaResourceKey)
  val MetaServer = prop(MetaServerKey)
  val MetaPort = prop(MetaPortKey).toInt
  val MetaSchema = prop(MetaSchemaKey)
  val MetaInitialConnections = 3
  val MetaMaxConnections = 20

  lazy val secrets = readSecrets(SecretsFile, ApiSecretKeys) match {
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
