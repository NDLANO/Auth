package no.ndla.auth.repository

import java.util.{Calendar, UUID}
import java.sql.Timestamp
import no.ndla.auth.exception.NoSuchUserException
import no.ndla.auth.integration.DataSourceComponent
import no.ndla.auth.model._
import no.ndla.auth.model
import org.joda.time.DateTime
import scalikejdbc._


trait UsersRepositoryComponent {
  this: DataSourceComponent =>
  val usersRepository: UsersRepository

  class UsersRepository {
    ConnectionPool.singleton(new DataSourceConnectionPool(dataSource))

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
      val ndla_user_id: UUID = UUID.randomUUID()

      using(ConnectionPool.borrow()) { conn: java.sql.Connection =>
        val pst = conn.prepareStatement(s"INSERT INTO ndla_users(id, first_name, middle_name, last_name, email, ${externalUser.userType}_id, created) VALUES (?, ?, ?, ?, ?, ?, ?)")
        pst.setObject(1, ndla_user_id)
        pst.setString(2, externalUser.first_name.orNull)
        pst.setString(3, externalUser.middle_name.orNull)
        pst.setString(4, externalUser.last_name.orNull)
        pst.setString(5, externalUser.email.orNull)
        pst.setString(6, externalUser.id)
        pst.setTimestamp(7, new Timestamp(Calendar.getInstance().getTime().getTime()))
        pst.executeUpdate()
      }
      ndla_user_id.toString
    }

    def getNdlaUser(ndlaUserId: String): NdlaUser = {
      val resultSet = using(ConnectionPool.borrow()) { conn: java.sql.Connection =>
        val pst = conn.prepareStatement("SELECT * FROM ndla_users WHERE id = ?")
        pst.setObject(1, UUID.fromString(ndlaUserId))
        pst.executeQuery()
      }
      resultSet.next() match {
        case false => throw new NoSuchUserException(s"No user with id $ndlaUserId found")
        case true =>NdlaUser(ndlaUserId, Option(resultSet.getString("first_name")), Option(resultSet.getString("middle_name")), Option(resultSet.getString("last_name")), Option(resultSet.getString("email")), new DateTime(resultSet.getTimestamp("created")))
      }
    }

    def getNdlaUserName(ndlaUserId: String): NdlaUserName = {
      val ndlaUser = getNdlaUser(ndlaUserId)
      NdlaUserName(ndlaUser.first_name, ndlaUser.middle_name, ndlaUser.last_name)
    }

    private def findOrCreateUser(facebookUser: FacebookUser): Option[String] = {
      val resultSet = using(ConnectionPool.borrow()) { conn: java.sql.Connection =>
        // Denne spørringen opretter en ny row dersom det ikke finnes en rad med id=facebookUser.id, og returnerer ndla_id kolonnen for denne nye raden (null).
        // Hvis en rad med id = facebookUser.id allerede finnes returneres ndla_id for denne raden (uten å manipulere raden, eller lagge til en ny rad)
        val query = "WITH s AS ( " +
          "SELECT * FROM facebook_users WHERE id = ? " +
          "), i AS ( " +
          "INSERT INTO facebook_users (id, first_name, middle_name, last_name, created, email) " +
          "SELECT ?, ?, ?, ?, ?, ? " +
          "WHERE NOT EXISTS (SELECT * FROM facebook_users WHERE id = ?) " +
          "RETURNING * " +
          ") " +
          "SELECT ndla_id FROM i " +
          "UNION ALL " +
          "SELECT ndla_id FROM s ";

        val pst = conn.prepareStatement(query)
        pst.setString(1, facebookUser.id)
        pst.setString(2, facebookUser.id)
        pst.setString(3, facebookUser.first_name.orNull)
        pst.setString(4, facebookUser.middle_name.orNull)
        pst.setString(5, facebookUser.last_name.orNull)
        pst.setTimestamp(6, new Timestamp(Calendar.getInstance().getTime().getTime()))
        pst.setString(7, facebookUser.email.orNull)
        pst.setString(8, facebookUser.id)

        pst.executeQuery()
      }
      resultSet.next()

      resultSet.getString("ndla_id") match {
        case null => None // New user
        case value => Option(value)
      }
    }

    private def findOrCreateUser(twitterUser: TwitterUser): Option[String] = {
      val resultSet = using(ConnectionPool.borrow()) { conn: java.sql.Connection =>
        // Denne spørringen opretter en ny row dersom det ikke finnes en rad med id=facebookUser.id, og returnerer ndla_id kolonnen for denne nye raden (null).
        // Hvis en rad med id = facebookUser.id allerede finnes returneres ndla_id for denne raden (uten å manipulere raden, eller lagge til en ny rad)
        val query = "WITH s AS ( " +
          "SELECT * FROM twitter_users WHERE id = ? " +
          "), i AS ( " +
          "INSERT INTO twitter_users (id, first_name, middle_name, last_name, created, email) " +
          "SELECT ?, ?, ?, ?, ?, ? " +
          "WHERE NOT EXISTS (SELECT * FROM twitter_users WHERE id = ?) " +
          "RETURNING * " +
          ") " +
          "SELECT ndla_id FROM i " +
          "UNION ALL " +
          "SELECT ndla_id FROM s ";

        val pst = conn.prepareStatement(query)
        pst.setString(1, twitterUser.id)
        pst.setString(2, twitterUser.id)
        pst.setString(3, twitterUser.first_name.orNull)
        pst.setString(4, twitterUser.middle_name.orNull)
        pst.setString(5, twitterUser.last_name.orNull)
        pst.setTimestamp(6, new Timestamp(Calendar.getInstance().getTime().getTime()))
        pst.setString(7, twitterUser.email.orNull)
        pst.setString(8, twitterUser.id)

        pst.executeQuery()
      }
      resultSet.next()

      resultSet.getString("ndla_id") match {
        case null => None // New user
        case value => Option(value)
      }
    }

    private def findOrCreateUser(googleUser: GoogleUser): Option[String] = {
      val resultSet = using(ConnectionPool.borrow()) { conn: java.sql.Connection =>
        // Denne spørringen opretter en ny row dersom det ikke finnes en rad med id=facebookUser.id, og returnerer ndla_id kolonnen for denne nye raden (null).
        // Hvis en rad med id = facebookUser.id allerede finnes returneres ndla_id for denne raden (uten å manipulere raden, eller lagge til en ny rad)
        val query = "WITH s AS ( " +
          "SELECT * FROM google_users WHERE id = ? " +
          "), i AS ( " +
          "INSERT INTO google_users (id, first_name, middle_name, last_name, display_name, etag, object_type, verified, email, created) " +
          "SELECT ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
          "WHERE NOT EXISTS (SELECT * FROM google_users WHERE id = ?) " +
          "RETURNING * " +
          ") " +
          "SELECT ndla_id FROM i " +
          "UNION ALL " +
          "SELECT ndla_id FROM s ";

        val pst = conn.prepareStatement(query)
        pst.setString(1, googleUser.id)
        pst.setString(2, googleUser.id)
        pst.setString(3, googleUser.name.flatMap(_.givenName).orNull)
        pst.setString(4, googleUser.name.flatMap(_.middleName).orNull)
        pst.setString(5, googleUser.name.flatMap(_.familyName).orNull)
        pst.setString(6, googleUser.displayName.orNull)
        pst.setString(7, googleUser.etag.orNull)
        pst.setString(8, googleUser.objectType.orNull)
        pst.setBoolean(9, Option(true).map(_.asInstanceOf[java.lang.Boolean]).orNull)
        pst.setString(10, googleUser.email.orNull)
        pst.setTimestamp(11, new Timestamp(Calendar.getInstance().getTime().getTime()))
        pst.setString(12, googleUser.id)

        pst.executeQuery()
      }
      resultSet.next()

      resultSet.getString("ndla_id") match {
        case null => None // New user
        case value => Option(value)
      }
    }
  }
}


