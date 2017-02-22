/*
 * Part of NDLA auth.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.auth

import no.ndla.auth.controller.{HealthController, TokenController}
import no.ndla.auth.service.{Clock, TokenService}
import org.mockito.Mockito
import org.scalatest.mockito.MockitoSugar

trait TestEnvironment
  extends HealthController
    with TokenController
    with TokenService
    with Clock
    with MockitoSugar {

  val healthController: HealthController = mock[HealthController]
  val tokenController: TokenController = mock[TokenController]
  val tokenService: TokenService = mock[TokenService]
  val clock: SystemClock = mock[SystemClock]

  def resetMocks(): Unit = {
    Mockito.reset(healthController, tokenController, tokenService, clock)
  }
}
