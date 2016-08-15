/*
 * Part of NDLA auth.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.auth.controller


import com.typesafe.scalalogging.LazyLogging
import no.ndla.network.CorrelationID
import org.apache.logging.log4j.ThreadContext
import org.scalatra.CoreDsl
import no.ndla.auth.AuthProperties.{CorrelationIdKey, CorrelationIdHeader}

trait CorrelationIdSupport extends CoreDsl with LazyLogging {

  before() {
    CorrelationID.set(Option(request.getHeader(CorrelationIdHeader)))
    ThreadContext.put(CorrelationIdKey, CorrelationID.get.getOrElse(""))
  }

  after() {
    CorrelationID.clear()
    ThreadContext.remove(CorrelationIdKey)
  }

}
