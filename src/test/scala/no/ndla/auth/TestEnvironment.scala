package no.ndla.auth

import no.ndla.auth.database.Cassandra
import com.datastax.driver.core.{PreparedStatement, Session, Cluster}
import no.ndla.auth.integration.KongServiceComponent
import no.ndla.auth.repository.{StateRepositoryComponent, UsersRepositoryComponent}
import no.ndla.auth.integration.providers.{TwitterAuthServiceComponent, GoogleAuthServiceComponent, FacebookAuthServiceComponent}
import org.scalatest.mock.MockitoSugar
import org.mockito.Matchers._
import org.mockito.Mockito._

trait TestEnvironment
    extends UsersRepositoryComponent
    with StateRepositoryComponent
    with FacebookAuthServiceComponent
    with GoogleAuthServiceComponent
    with TwitterAuthServiceComponent
    with KongServiceComponent
    with Cassandra
    with MockitoSugar
{
  val cassandraCluster = mock[Cluster]
  val cassandraSession = mock[Session]

  val usersRepository = mock[UsersRepository]
  val stateRepository = mock[StateRepository]
  val facebookAuthService = mock[FacebookAuthService]
  val googleAuthService = mock[GoogleAuthService]
  val twitterAuthService = mock[TwitterAuthService]
  val kongService = mock[KongService]
}