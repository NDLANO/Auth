package no.ndla.auth

import no.ndla.auth.integration.{DataSourceComponent, KongServiceComponent}
import no.ndla.auth.repository.{StateRepositoryComponent, UsersRepositoryComponent}
import no.ndla.auth.integration.providers.{TwitterAuthServiceComponent, GoogleAuthServiceComponent, FacebookAuthServiceComponent}
import org.postgresql.ds.PGPoolingDataSource
import org.scalatest.mock.MockitoSugar

trait TestEnvironment
    extends DataSourceComponent
    with UsersRepositoryComponent
    with StateRepositoryComponent
    with FacebookAuthServiceComponent
    with GoogleAuthServiceComponent
    with TwitterAuthServiceComponent
    with KongServiceComponent
    with MockitoSugar
{
  val dataSource = mock[PGPoolingDataSource]
  val usersRepository = mock[UsersRepository]
  val stateRepository = mock[StateRepository]
  val facebookAuthService = mock[FacebookAuthService]
  val googleAuthService = mock[GoogleAuthService]
  val twitterAuthService = mock[TwitterAuthService]
  val kongService = mock[KongService]
}