/*
 * Part of NDLA auth.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.auth.controller

import javax.servlet.http.HttpServletRequest

import com.typesafe.scalalogging.LazyLogging
import org.apache.logging.log4j.ThreadContext
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.ScalatraServlet
import org.scalatra.json.NativeJsonSupport
import no.ndla.network.{ApplicationUrl, CorrelationID}
import no.ndla.auth.AuthProperties.{CorrelationIdHeader, CorrelationIdKey, WhiteListedSuccessUrls, WhiteListedFailureUrls}
import no.ndla.auth.model.{Error, ValidationException}
import org.scalatra.swagger.SwaggerSupport

abstract class NdlaController extends ScalatraServlet with NativeJsonSupport with SwaggerSupport with LazyLogging {
  protected implicit override val jsonFormats: Formats = DefaultFormats

  before() {
    contentType = formats("json")
    CorrelationID.set(Option(request.getHeader(CorrelationIdHeader)))
    ThreadContext.put(CorrelationIdKey, CorrelationID.get.getOrElse(""))
    ApplicationUrl.set(request)
    logger.info("{} {}{}", request.getMethod, request.getRequestURI, Option(request.getQueryString).map(s => s"?$s").getOrElse(""))
  }

  after() {
    CorrelationID.clear()
    ThreadContext.remove(CorrelationIdKey)
    ApplicationUrl.clear
  }

  error {
    case v: ValidationException => halt(status = 400, body = Error(Error.VALIDATION, v.getMessage))
    case t: Throwable => {
      logger.error(Error.GenericError.toString, t)
      halt(status = 500, body = Error.GenericError)
    }
  }

  def long(paramName: String)(implicit request: HttpServletRequest): Long = {
    val paramValue = params(paramName)
    paramValue.forall(_.isDigit) match {
      case true => paramValue.toLong
      case false => throw new ValidationException(s"Invalid value for $paramName. Only digits are allowed.")
    }
  }

  def getLoginFailureUrl(implicit request: HttpServletRequest): String = {
    val defaultFailureUrl = WhiteListedFailureUrls.head._2
    params.get("failureUrl") match {
      case Some(url) => WhiteListedFailureUrls.getOrElse(url, defaultFailureUrl)
      case None => defaultFailureUrl
    }
  }

  def getLoginSuccessUrl(implicit request: HttpServletRequest): String = {
    val defaultSuccessUrl = WhiteListedSuccessUrls.head._2
    params.get("successUrl") match {
      case Some(url) => WhiteListedSuccessUrls.getOrElse(url, defaultSuccessUrl)
      case None => defaultSuccessUrl
    }
  }

}
