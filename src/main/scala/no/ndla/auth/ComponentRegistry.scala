package no.ndla.auth

import com.datastax.driver.core.{Cluster, Session}
import no.ndla.auth.database.Cassandra
import no.ndla.auth.integration.KongServiceComponent
import no.ndla.auth.repository.StateRepositoryComponent
import no.ndla.auth.repository.UsersRepositoryComponent
import no.ndla.auth.integration.providers.{TwitterAuthServiceComponent, GoogleAuthServiceComponent, FacebookAuthServiceComponent}

object ComponentRegistry
  extends UsersRepositoryComponent
  with StateRepositoryComponent
  with FacebookAuthServiceComponent
  with GoogleAuthServiceComponent
  with TwitterAuthServiceComponent
  with KongServiceComponent
  with Cassandra
{
  lazy val cassandraCluster = Cluster.builder().addContactPoint("cassandra").build()
  lazy val cassandraSession = cassandraCluster.connect("accounts")

  lazy val usersRepository = new UsersRepository
  lazy val stateRepository = new StateRepository
  lazy val facebookAuthService = new FacebookAuthService
  lazy val googleAuthService = new GoogleAuthService
  lazy val twitterAuthService = new TwitterAuthService
  lazy val kongService = new KongService
}