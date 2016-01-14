package no.ndla.auth

import com.typesafe.scalalogging.LazyLogging


object AuthProperties extends LazyLogging {

  val environmentFile = "/auth.env"
  val properties = scala.io.Source.fromInputStream(getClass.getResourceAsStream(environmentFile)).getLines().map(key => key -> scala.util.Properties.envOrNone(key)).toMap

  val whiteListedSuccessUrls = get("WHITELISTED_SUCCESSURLS")
  val whiteListedFailureUrls = get("WHITELISTED_FAILUREURLS")

  val KONG_USERNAME_PREFIX = "ndla-"

  def verify() = {
    val missingProperties = properties.filter(entry => entry._2.isEmpty).toList
    if (missingProperties.nonEmpty) {
      missingProperties.foreach(entry => logger.error("Missing required environment variable {}", entry._1))

      logger.error("Shutting down.")
      System.exit(1)
    }
  }

  def get(envKey: String): String = {
    properties.get(envKey) match {
      case Some(value) => value.get
      case None => throw new NoSuchFieldError(s"Missing environment variable $envKey")
    }
  }

  def getWithPrefix(prefix: String): Map[String, Option[String]] = {
    properties.filterKeys(_.startsWith(prefix)).map {
      case (key, value) => (key.replaceFirst(prefix, ""), value)
    }
  }

  def getInt(envKey: String): Integer = {
    get(envKey).toInt
  }

}
