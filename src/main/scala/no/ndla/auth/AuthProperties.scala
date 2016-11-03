/*
 * Part of NDLA auth.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.auth

import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable
import scala.io.Source
import no.ndla.network.secrets.PropertyKeys._
import no.ndla.network.secrets.Secrets._

import scala.util.{Failure, Properties, Success, Try}

object AuthProperties extends LazyLogging {
  var AuthApiProps: mutable.Map[String, Option[String]] = mutable.HashMap()

  val ApplicationPort = 80
  lazy val ContactEmail = "christergundersen@ndla.no"

  lazy val Environment = get("NDLA_ENVIRONMENT")
  lazy val Domain = getDomain
  val HealthControllerPath = "/health"

  lazy val WhiteListedSuccessUrls = Map(
    "/login/success/{appkey}" -> s"$Domain:8080/login/success/{appkey}",
    "/images" -> s"$Domain/images"
  )

  lazy val WhiteListedFailureUrls = Map(
    "/login/failure" -> s"$Domain:8080/login/failure",
    "/" -> s"$Domain/"
  )

  lazy val GoogleClientSecret = get("GOOGLE_CLIENT_SECRET")
  lazy val GoogleClientId = get("GOOGLE_CLIENT_ID")

  lazy val FacebookClientSecret = get("FACEBOOK_CLIENT_SECRET")
  lazy val FacebookClientId = get("FACEBOOK_CLIENT_ID")

  lazy val TwitterApiKey = get("TWITTER_API_KEY")
  lazy val TwitterClientSecret = get("TWITTER_CLIENT_SECRET")

  lazy val KongAdminPort = 8001
  val KongHostName = "api-gateway.ndla-local"
  val KongUsernamePrefix = "ndla-"

  val CorrelationIdKey = "correlationID"
  val CorrelationIdHeader = "X-Correlation-ID"

  lazy val MetaUserName = get(MetaUserNameKey)
  lazy val MetaPassword = get(MetaPasswordKey)
  lazy val MetaResource = get(MetaResourceKey)
  lazy val MetaServer = get(MetaServerKey)
  lazy val MetaPort = getInt(MetaPortKey)
  lazy val MetaSchema = get(MetaSchemaKey)
  val MetaInitialConnections = 3
  val MetaMaxConnections = 20


  def setProperties(properties: Map[String, Option[String]]) = {
    val missingProperties = properties.filter(_._2.isEmpty).keys
    missingProperties.isEmpty match {
      case true => Success(properties.foreach(prop => AuthApiProps.put(prop._1, prop._2)))
      case false => Failure(new RuntimeException(s"Missing required properties: ${missingProperties.mkString(", ")}"))
    }
  }

  def verify() = {
    val missingProperties = AuthApiProps.filter(entry => entry._2.isEmpty).toList
    if (missingProperties.nonEmpty) {
      missingProperties.foreach(entry => logger.error("Missing required environment variable {}", entry._1))

      logger.error("Shutting down.")
      System.exit(1)
    }
  }

  private def get(envKey: String): String = {
    AuthApiProps.get(envKey) match {
      case Some(value) => value.get
      case None => throw new NoSuchFieldError(s"Missing environment variable $envKey")
    }
  }

  private def getDomain: String = {
    Map("local" -> "http://localhost",
        "prod" -> "http://api.ndla.no"
    ).getOrElse(Environment, s"http://api.$Environment.ndla.no")
  }

  def getWithPrefix(prefix: String): Map[String, Option[String]] = {
    AuthApiProps.filterKeys(_.startsWith(prefix)).map {
      case (key, value) => key.replaceFirst(prefix, "") -> value
    }.toMap
  }

  private def getInt(envKey: String): Integer = {
    get(envKey).toInt
  }
}

object PropertiesLoader extends LazyLogging {
  val EnvironmentFile = "/auth.env"

  def readPropertyFile() = {
    Try(Source.fromInputStream(getClass.getResourceAsStream(EnvironmentFile)).getLines().withFilter(line => line.matches("^\\w+$")).map(key => key -> Properties.envOrNone(key)).toMap)
  }

  def load() = {
    val ApiSecretKeys = Set("GOOGLE_CLIENT_SECRET", "GOOGLE_CLIENT_ID", "FACEBOOK_CLIENT_SECRET", "FACEBOOK_CLIENT_ID", "TWITTER_API_KEY", "TWITTER_CLIENT_SECRET")
    val verification = for {
      file <- readPropertyFile()
      secrets <- readSecrets("auth.secrets", ApiSecretKeys)
      didSetProperties <- AuthProperties.setProperties(file ++ secrets)
    } yield didSetProperties

    if(verification.isFailure){
      logger.error("Unable to load properties", verification.failed.get)
      System.exit(1)
    }
  }
}
