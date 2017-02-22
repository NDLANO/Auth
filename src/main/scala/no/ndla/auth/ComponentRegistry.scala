/*
 * Part of NDLA auth.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.auth

import no.ndla.auth.controller.{HealthController, LoginController, TokenController}
import no.ndla.auth.service.{Clock, TokenService}

object ComponentRegistry
  extends HealthController
  with TokenController
  with TokenService
  with LoginController
  with Clock
{
  implicit val swagger = new AuthSwagger

  lazy val resourcesApp = new ResourcesApp
  lazy val healthController = new HealthController
  lazy val tokenController = new TokenController
  lazy val tokenService = new TokenService
  lazy val loginController = new LoginController
  lazy val clock = new SystemClock
}
