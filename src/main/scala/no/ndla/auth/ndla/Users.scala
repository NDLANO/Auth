package no.ndla.auth.ndla

import java.util.UUID

import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.utils.UUIDs
import com.datastax.driver.core._
import no.ndla.auth.providers.google.GoogleUser
import no.ndla.auth.providers.twitter.TwitterUser
import no.ndla.auth.{NoSuchUserException, ExternalUser}
import no.ndla.auth.providers.facebook.FacebookUser
import org.joda.time.DateTime
import no.ndla.auth.database.Cassandra.session

object Users {

  def getOrCreateNdlaUser(user: ExternalUser): NdlaUser = {
    val ndlaUserId: Option[String] = user.userType match {
      case no.ndla.auth.UserType.FACEBOOK => findOrCreateUser(user.asInstanceOf[FacebookUser])
      case no.ndla.auth.UserType.GOOGLE => findOrCreateUser(user.asInstanceOf[GoogleUser])
      case no.ndla.auth.UserType.TWITTER => findOrCreateUser(user.asInstanceOf[TwitterUser])
      case _ => throw new RuntimeException("Not implemented for given user type")
    }

    if (ndlaUserId.isDefined) {
      getNdlaUser(ndlaUserId.get)
    } else {
      val ndlaUserId: String = createNdlaUser(user)
      getNdlaUser(ndlaUserId).copy(newUser = true)
    }
  }

  private def createNdlaUser(externalUser: ExternalUser): String = {
    val ndla_user_id: String = UUID.randomUUID().toString

    val insertNdlaUser: PreparedStatement = session.prepare(s"INSERT INTO ndla_users(id, first_name, middle_name, last_name, email, ${externalUser.userType}_id, created) VALUES (?, ?, ?, ?, ?, ?, ?)")
    val updateExternalUserWithNdlaUserId: Statement = QueryBuilder.update(externalUser.userType + "_users").`with`(QueryBuilder.set("ndla_id", ndla_user_id)).where(QueryBuilder.eq("id", externalUser.id))
    val batchStatement: BatchStatement = new BatchStatement()
    batchStatement.add(insertNdlaUser.bind(ndla_user_id, externalUser.first_name.orNull, null, externalUser.last_name.orNull, externalUser.email.orNull, externalUser.id, UUIDs.timeBased()))
    batchStatement.add(updateExternalUserWithNdlaUserId)
    session.execute(batchStatement)
    ndla_user_id
  }

  def getNdlaUser(ndlaUserId: String): NdlaUser = {
    val resultSet: ResultSet = session.execute(s"select * from ndla_users where id = '$ndlaUserId';")
    Option(resultSet.one()) match {
      case Some(row) => NdlaUser(ndlaUserId, Option(row.getString("first_name")), Option(row.getString("middle_name")), Option(row.getString("last_name")), Option(row.getString("email")), new DateTime(UUIDs.unixTimestamp(row.getUUID("created"))))
      case None => throw new NoSuchUserException(s"No user with id $ndlaUserId found")
    }
  }

  private def findOrCreateUser(facebookUser: FacebookUser): Option[String] = {
    val insertUserIntoFacebookTable: PreparedStatement = session.prepare(s"INSERT INTO facebook_users (id, first_name, middle_name, last_name, created, email) " +
      s"VALUES (?, ?, ?, ?, ?, ?) IF NOT EXISTS  ;")
    val boundStatement: BoundStatement = insertUserIntoFacebookTable.bind(facebookUser.id, facebookUser.first_name.orNull, facebookUser.middle_name.orNull, facebookUser.last_name.orNull, UUIDs.timeBased(), facebookUser.email.orNull)
    val result = session.execute(boundStatement)

    if (result.wasApplied()) {
      // New user
      None
    } else {
      Option(result.one().getString("ndla_id"))
    }
  }

  private def findOrCreateUser(twitterUser: TwitterUser): Option[String] = {
    val insertUserIntoFacebookTable: PreparedStatement = session.prepare(s"INSERT INTO twitter_users (id, name, first_name, middle_name, last_name, created, email) " +
      s"VALUES (?, ?, ?, ?, ?, ?, ?) IF NOT EXISTS  ;")
    val boundStatement: BoundStatement = insertUserIntoFacebookTable.bind(twitterUser.id, twitterUser.name.orNull, twitterUser.first_name.orNull, twitterUser.middle_name.orNull, twitterUser.last_name.orNull, UUIDs.timeBased(), twitterUser.email.orNull)
    val result = session.execute(boundStatement)

    if (result.wasApplied()) {
      // New user
      None
    } else {
      Option(result.one().getString("ndla_id"))
    }
  }

  private def findOrCreateUser(googleUser: GoogleUser): Option[String] = {
    val insertUserIntoTable: PreparedStatement = session.prepare(s"INSERT INTO google_users (id, first_name, middle_name, last_name, display_name, etag, object_type, verified, email, created) VALUES ( ?, ?, ?, ?, ?, ?,?, ?, ?, ?) IF NOT EXISTS  ;")
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
    val result = session.execute(boundStatement)

    if (result.wasApplied()) {
      // New user
      None
    } else {
      Option(result.one().getString("ndla_id"))
    }
  }
}

case class NdlaUser(id: String,
                    first_name: Option[String],
                    middle_name: Option[String],
                    last_name: Option[String],
                    email: Option[String],
                    created: DateTime,
                    newUser: Boolean = false
                     )