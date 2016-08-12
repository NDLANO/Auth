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


object AuthProperties extends LazyLogging {
  var AuthApiProps: mutable.Map[String, Option[String]] = mutable.HashMap()

  val ApplicationPort = 80
  lazy val ContactEmail = get("CONTACT_EMAIL")

  lazy val WhiteListedSuccessUrls = get("WHITELISTED_SUCCESSURLS")
  lazy val WhiteListedFailureUrls = get("WHITELISTED_FAILUREURLS")

  lazy val KongAdminPort = get("KONG_ADMIN_PORT")
  val KongHostName = "api-gateway"
  val KongUsernamePrefix = "ndla-"

  lazy val MetaUserName = get("DB_USER")
  lazy val MetaPassword = get("DB_PASSWORD")
  lazy val MetaResource = get("DB_RESOURCE")
  lazy val MetaServer = get("DB_SERVER")
  lazy val MetaSchema = get("DB_SCHEMA")
  val MetaPort = 5432
  val MetaMaxConnections = 20
  val MetaInitialConnections = 3

  def setProperties(properties: Map[String, Option[String]]) = {
    properties.foreach(prop => AuthApiProps.put(prop._1, prop._2))
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

  def getWithPrefix(prefix: String): Map[String, Option[String]] = {
    AuthApiProps.filterKeys(_.startsWith(prefix)).map {
      case (key, value) => key.replaceFirst(prefix, "") -> value
    }.toMap
  }

  private def getInt(envKey: String): Integer = {
    get(envKey).toInt
  }
}

object PropertiesLoader {
  val EnvironmentFile = "/auth.env"

  private def readPropertyFile(): Map[String, Option[String]] = {
    val keys = Source.fromInputStream(getClass.getResourceAsStream(EnvironmentFile)).getLines().withFilter(line => line.matches("^\\w+$"))
    keys.map(key => key -> scala.util.Properties.envOrNone(key)).toMap
  }

  def load() = {
    AuthProperties.setProperties(readPropertyFile())
    AuthProperties.verify()
  }
}
