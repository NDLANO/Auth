package no.ndla.auth

import org.postgresql.ds.PGPoolingDataSource
import no.ndla.auth.integration.{DataSourceComponent, KongServiceComponent}
import no.ndla.auth.repository.StateRepositoryComponent
import no.ndla.auth.repository.UsersRepositoryComponent
import no.ndla.auth.integration.providers.{TwitterAuthServiceComponent, GoogleAuthServiceComponent, FacebookAuthServiceComponent}

object ComponentRegistry
  extends DataSourceComponent
  with UsersRepositoryComponent
  with StateRepositoryComponent
  with FacebookAuthServiceComponent
  with GoogleAuthServiceComponent
  with TwitterAuthServiceComponent
  with KongServiceComponent
{
  val dataSource = new PGPoolingDataSource()
  dataSource.setUser(AuthProperties.MetaUserName)
  dataSource.setPassword(AuthProperties.MetaPassword)
  dataSource.setDatabaseName(AuthProperties.MetaResource)
  dataSource.setServerName(AuthProperties.MetaServer)
  dataSource.setPortNumber(AuthProperties.MetaPort)
  dataSource.setInitialConnections(AuthProperties.MetaInitialConnections)
  dataSource.setMaxConnections(AuthProperties.MetaMaxConnections)
  dataSource.setCurrentSchema(AuthProperties.MetaSchema)

  lazy val usersRepository = new UsersRepository
  lazy val stateRepository = new StateRepository
  lazy val facebookAuthService = new FacebookAuthService
  lazy val googleAuthService = new GoogleAuthService
  lazy val twitterAuthService = new TwitterAuthService
  lazy val kongService = new KongService
}