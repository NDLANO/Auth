package no.ndla.auth

import com.typesafe.scalalogging.LazyLogging


object AuthProperties extends LazyLogging {

  val EnvironmentFile = "/auth.env"
  val Properties = scala.io.Source.fromInputStream(getClass.getResourceAsStream(EnvironmentFile)).getLines().map(key => key -> scala.util.Properties.envOrNone(key)).toMap

  val WhiteListedSuccessUrls = get("WHITELISTED_SUCCESSURLS")
  val WhiteListedFailureUrls = get("WHITELISTED_FAILUREURLS")

  val KongAdminPort = get("KONG_ADMIN_PORT")
  val KongHostName = "kong"
  val KongUsernamePrefix = "ndla-"

  var MetaUserName = get("DB_USER_NAME")
  var MetaPassword = get("DB_PASSWORD")
  var MetaResource = get("DB_RESOURCE")
  var MetaServer = get("DB_SERVER")
  var MetaPort = getInt("DB_PORT")
  var MetaSchema = get("DB_SCHEMA")
  var MetaInitialConnections = 3
  var MetaMaxConnections = 20

  def verify() = {
    val missingProperties = Properties.filter(entry => entry._2.isEmpty).toList
    if (missingProperties.nonEmpty) {
      missingProperties.foreach(entry => logger.error("Missing required environment variable {}", entry._1))

      logger.error("Shutting down.")
      System.exit(1)
    }
  }

  private def get(envKey: String): String = {
    Properties.get(envKey) match {
      case Some(value) => value.get
      case None => throw new NoSuchFieldError(s"Missing environment variable $envKey")
    }
  }

  def getWithPrefix(prefix: String): Map[String, Option[String]] = {
    Properties.filterKeys(_.startsWith(prefix)).map {
      case (key, value) => (key.replaceFirst(prefix, ""), value)
    }
  }

  private def getInt(envKey: String): Integer = {
    get(envKey).toInt
  }
}
