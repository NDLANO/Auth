package no.ndla.auth

import no.ndla.auth.integration.providers.{FacebookAuthServiceComponent, GoogleAuthServiceComponent, TwitterAuthServiceComponent}
import no.ndla.auth.integration.{DataSourceComponent, KongServiceComponent}
import no.ndla.auth.repository.{StateRepositoryComponent, UsersRepositoryComponent}
import org.h2.jdbcx.JdbcConnectionPool
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
  val dataSource = JdbcConnectionPool.create("jdbc:h2:mem:test", "sa", "sa")
  DBMigrator.migrate(dataSource)

  val usersRepository = mock[UsersRepository]
  val stateRepository = mock[StateRepository]
  val facebookAuthService = mock[FacebookAuthService]
  val googleAuthService = mock[GoogleAuthService]
  val twitterAuthService = mock[TwitterAuthService]
  val kongService = mock[KongService]
}
