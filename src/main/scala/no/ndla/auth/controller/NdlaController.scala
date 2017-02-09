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
import no.ndla.auth.AuthProperties.{CorrelationIdHeader, CorrelationIdKey}
import no.ndla.auth.exception.{HeaderMissingException, ParameterMissingException}
import no.ndla.auth.model.{Error, ValidationException}
import no.ndla.network.{ApplicationUrl, CorrelationID}
import org.apache.logging.log4j.ThreadContext
import org.json4s.native.Serialization.read
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.ScalatraServlet
import org.scalatra.json.NativeJsonSupport
import org.scalatra.swagger.{ResponseMessage, SwaggerSupport}

abstract class NdlaController extends ScalatraServlet with NativeJsonSupport with LazyLogging with SwaggerSupport {
  protected implicit override val jsonFormats: Formats = DefaultFormats

  val response400 = ResponseMessage(400, "Validation Error", Some("ValidationError"))
  val response500 = ResponseMessage(500, "Unknown error", Some("Error"))

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
    case p: ParameterMissingException => halt(status = 400, body = Error(Error.PARAMETER_MISSING, p.getMessage))
    case h: HeaderMissingException => halt(status = 400, body = Error(Error.HEADER_MISSING, h.getMessage))
    case v: ValidationException => halt(status = 400, body = Error(Error.VALIDATION, v.getMessage))
    case t: Throwable =>
      logger.error(Error.GenericError.toString, t)
      halt(status = 500, body = Error.GenericError)
  }

  def requireHeader(headerName: String)(implicit request: HttpServletRequest): String = {
    request.header(headerName) match {
      case Some(value) => value
      case None =>
        logger.warn(s"Request made to ${request.getRequestURI} without required header $headerName.")
        throw HeaderMissingException(s"The required header $headerName is missing.")
    }
  }

  def requireParam(paramName: String)(implicit request: HttpServletRequest): String = {
    params.get(paramName) match {
      case Some(value) => value
      case None =>
        logger.warn(s"Request made to ${request.getRequestURI} without required parameter $paramName")
        throw ParameterMissingException(s"The required parameter $paramName is missing.")
    }
  }

  def extract[T](json: String)(implicit mf: scala.reflect.Manifest[T]): T = {
    try {
      read[T](json)
    } catch {
      case e: Exception =>
        logger.error(e.getMessage, e)
        throw new ValidationException(message = e.getMessage)
    }
  }

}
