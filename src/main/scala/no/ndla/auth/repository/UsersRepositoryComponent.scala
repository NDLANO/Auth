package no.ndla.auth.repository

import java.util.UUID

import com.datastax.driver.core._
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.utils.UUIDs
import no.ndla.auth.database.Cassandra
import no.ndla.auth.exception.NoSuchUserException
import no.ndla.auth.model._
import no.ndla.auth.model
import org.joda.time.DateTime


trait UsersRepositoryComponent {
  this: Cassandra =>
  val usersRepository: UsersRepository

  class UsersRepository {
    val FindNdlaUser = cassandraSession.prepare("select * from ndla_users where id = ?")

    def getOrCreateNdlaUser(user: ExternalUser): NdlaUser = {
      val ndlaUserId: Option[String] = user.userType match {
        case model.UserType.FACEBOOK => findOrCreateUser(user.asInstanceOf[FacebookUser])
        case model.UserType.GOOGLE => findOrCreateUser(user.asInstanceOf[GoogleUser])
        case model.UserType.TWITTER => findOrCreateUser(user.asInstanceOf[TwitterUser])
        case _ => throw new RuntimeException("Not implemented for given user type")
      }

      ndlaUserId match {
        case Some(userId) => getNdlaUser(userId)
        case None => getNdlaUser(createNdlaUser(user)).copy(newUser = true)
      }
    }

    private def createNdlaUser(externalUser: ExternalUser): String = {
      val ndla_user_id: String = UUID.randomUUID().toString

      val insertNdlaUser: PreparedStatement = cassandraSession.prepare(s"INSERT INTO ndla_users(id, first_name, middle_name, last_name, email, ${externalUser.userType}_id, created) VALUES (?, ?, ?, ?, ?, ?, ?)")
      val updateExternalUserWithNdlaUserId: Statement = QueryBuilder.update(externalUser.userType + "_users").`with`(QueryBuilder.set("ndla_id", ndla_user_id)).where(QueryBuilder.eq("id", externalUser.id))
      val batchStatement: BatchStatement = new BatchStatement()
      batchStatement.add(insertNdlaUser.bind(ndla_user_id, externalUser.first_name.orNull, null, externalUser.last_name.orNull, externalUser.email.orNull, externalUser.id, UUIDs.timeBased()))
      batchStatement.add(updateExternalUserWithNdlaUserId)
      cassandraSession.execute(batchStatement)
      ndla_user_id
    }

    def getNdlaUser(ndlaUserId: String): NdlaUser = {
      val resultSet: ResultSet = cassandraSession.execute(FindNdlaUser.bind(ndlaUserId))
      Option(resultSet.one()) match {
        case Some(row) => NdlaUser(ndlaUserId, Option(row.getString("first_name")), Option(row.getString("middle_name")), Option(row.getString("last_name")), Option(row.getString("email")), new DateTime(UUIDs.unixTimestamp(row.getUUID("created"))))
        case None => throw new NoSuchUserException(s"No user with id $ndlaUserId found")
      }
    }

    def getNdlaUserName(ndlaUserId: String): NdlaUserName = {
      val ndlaUser = getNdlaUser(ndlaUserId)
      NdlaUserName(ndlaUser.first_name, ndlaUser.middle_name, ndlaUser.last_name)
    }

    private def findOrCreateUser(facebookUser: FacebookUser): Option[String] = {
      val insertUserIntoFacebookTable: PreparedStatement = cassandraSession.prepare(s"INSERT INTO facebook_users (id, first_name, middle_name, last_name, created, email) " +
        s"VALUES (?, ?, ?, ?, ?, ?) IF NOT EXISTS  ;")
      val boundStatement: BoundStatement = insertUserIntoFacebookTable.bind(facebookUser.id, facebookUser.first_name.orNull, facebookUser.middle_name.orNull, facebookUser.last_name.orNull, UUIDs.timeBased(), facebookUser.email.orNull)
      val result = cassandraSession.execute(boundStatement)

      result.wasApplied() match {
        case true => None // New user
        case false => Option(result.one().getString("ndla_id"))
      }
    }

    private def findOrCreateUser(twitterUser: TwitterUser): Option[String] = {
      val insertUserIntoFacebookTable: PreparedStatement = cassandraSession.prepare(s"INSERT INTO twitter_users (id, name, first_name, middle_name, last_name, created, email) " +
        s"VALUES (?, ?, ?, ?, ?, ?, ?) IF NOT EXISTS  ;")
      val boundStatement: BoundStatement = insertUserIntoFacebookTable.bind(twitterUser.id, twitterUser.name.orNull, twitterUser.first_name.orNull, twitterUser.middle_name.orNull, twitterUser.last_name.orNull, UUIDs.timeBased(), twitterUser.email.orNull)
      val result = cassandraSession.execute(boundStatement)

      result.wasApplied() match {
        case true => None // New user
        case false => Option(result.one().getString("ndla_id"))
      }
    }

    private def findOrCreateUser(googleUser: GoogleUser): Option[String] = {
      val insertUserIntoTable: PreparedStatement = cassandraSession.prepare(s"INSERT INTO google_users (id, first_name, middle_name, last_name, display_name, etag, object_type, verified, email, created) VALUES ( ?, ?, ?, ?, ?, ?,?, ?, ?, ?) IF NOT EXISTS  ;")
      val boundStatement: BoundStatement = insertUserIntoTable.bind(
        googleUser.id,
        googleUser.name.flatMap(_.givenName).orNull,
        googleUser.name.flatMap(_.middleName).orNull,
        googleUser.name.flatMap(_.familyName).orNull,
        googleUser.displayName.orNull,
        googleUser.etag.orNull,
        googleUser.objectType.orNull,
        Option(true).map(_.asInstanceOf[java.lang.Boolean]).orNull,
        googleUser.email.orNull,
        UUIDs.timeBased())
      val result = cassandraSession.execute(boundStatement)

      result.wasApplied() match {
        case true => None // New user
        case false => Option(result.one().getString("ndla_id"))
      }
    }
  }
}


