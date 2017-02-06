/*
 * Part of NDLA auth.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.auth

import no.ndla.auth.controller.{AuthController, HealthController, TokenController}
import no.ndla.auth.integration.providers.{FacebookAuthServiceComponent, GoogleAuthServiceComponent, TwitterAuthServiceComponent}
import no.ndla.auth.integration.{DataSourceComponent, KongServiceComponent}
import no.ndla.auth.repository.{StateRepositoryComponent, UsersRepositoryComponent}
import no.ndla.auth.service.{Clock, TokenService}
import org.h2.jdbcx.JdbcConnectionPool
import org.mockito.Mockito
import org.scalatest.mock.MockitoSugar

trait TestEnvironment
  extends DataSourceComponent
    with UsersRepositoryComponent
    with StateRepositoryComponent
    with FacebookAuthServiceComponent
    with GoogleAuthServiceComponent
    with TwitterAuthServiceComponent
    with KongServiceComponent
    with AuthController
    with HealthController
    with TokenController
    with TokenService
    with Clock
    with MockitoSugar {
  val dataSource = JdbcConnectionPool.create("jdbc:h2:mem:test", "sa", "sa")
  DBMigrator.migrate(dataSource)

  val usersRepository = mock[UsersRepository]
  val stateRepository = mock[StateRepository]
  val facebookAuthService = mock[FacebookAuthService]
  val googleAuthService = mock[GoogleAuthService]
  val twitterAuthService = mock[TwitterAuthService]
  val kongService = mock[KongService]
  val authController = mock[AuthController]
  val healthController = mock[HealthController]
  val tokenController = mock[TokenController]
  val tokenService = mock[TokenService]
  val clock = mock[SystemClock]

  def resetMocks(): Unit = {
    Mockito.reset(usersRepository, stateRepository, facebookAuthService,
      googleAuthService, twitterAuthService, kongService, authController,
      healthController, tokenController, tokenService, clock)
  }
}
