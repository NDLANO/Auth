package no.ndla.auth

object WhiteListedUrls {

  val successUrls = AuthProperties.WhiteListedSuccessUrls.split(",").map(_ split "->") collect { case Array(k, v) => (k.trim, v.trim) } toMap
  val failureUrls = AuthProperties.WhiteListedFailureUrls.split(",").map(_ split "->") collect { case Array(k, v) => (k.trim, v.trim) } toMap

  val defaultSuccessUrl = successUrls.head._2
  val defaultFailureUrl = failureUrls.head._2

  def getFailureUrl(failureUrl: Option[String]): String = {
    failureUrl match {
      case Some(url) => failureUrls.getOrElse(url, defaultFailureUrl)
      case None => defaultFailureUrl
    }
  }

  def getSuccessUrl(successUrl: Option[String]):String = {
    successUrl match {
      case Some(url) => successUrls.getOrElse(url, defaultSuccessUrl)
      case None => defaultSuccessUrl
    }
  }
}
