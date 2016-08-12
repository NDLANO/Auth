/*
 * Part of NDLA auth.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

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

    private def createNdlaUser(user: ExternalUser): String = {
      val ndla_user_id: UUID = UUID.randomUUID()
      // Av sikkerhetshensyn er det ikke lov å bruke SQLInterpolation direkte i en SQL-spørring for å spesifisere kolonnenavn.
      // I vårt tilfelle er ikke dette et problem fordi user.userType ikke er bruker-input, men en enum (no.ndla.auth.model.UserType).
      // createUnsafely er en måte å unngå denne restriksjonen på.
      val colName = SQLSyntax.createUnsafely(user.userType.toString)

      DB localTx { implicit session =>
        sql"""INSERT INTO ndla_users (id, first_name, middle_name, last_name, email, ${colName}_id, created)
              VALUES ($ndla_user_id, ${user.first_name.orNull}, ${user.middle_name.orNull}, ${user.last_name.orNull},
              ${user.email.orNull}, ${user.id}, ${new Timestamp(Calendar.getInstance().getTime().getTime())})""".update().apply()
        sql"UPDATE ${colName}_users SET ndla_id = $ndla_user_id WHERE id = ${user.id}".update.apply()
      }

      ndla_user_id.toString
    }

    private def mapNdlaUser(rs: WrappedResultSet, ndlaUserId: String): NdlaUser = {
      NdlaUser(ndlaUserId, Option(rs.string("first_name")), Option(rs.string("middle_name")), Option(rs.string("last_name")), Option(rs.string("email")), new DateTime(rs.timestamp("created")))
    }

    def getNdlaUser(ndlaUserId: String): NdlaUser = {
      DB readOnly { implicit session =>
        sql"SELECT * from ndla_users WHERE id = ${UUID.fromString(ndlaUserId)}".map(rs => mapNdlaUser(rs, ndlaUserId)).single.apply()
      } match {
        case Some(user) => user
        case _ => throw new NoSuchUserException(s"No user with id $ndlaUserId found")
      }
    }

    def getNdlaUserName(ndlaUserId: String): NdlaUserName = {
      val ndlaUser = getNdlaUser(ndlaUserId)
      NdlaUserName(ndlaUser.first_name, ndlaUser.middle_name, ndlaUser.last_name)
    }

    private def findOrCreateUser(user: FacebookUser): Option[String] = {
      DB localTx { implicit session =>
        val ndla_id = sql"SELECT ndla_id FROM facebook_users WHERE id = ${user.id}".map { rs => rs.string("ndla_id") }.single.apply()

        ndla_id match {
          case Some(value) => Option(value)
          case None => {
            sql"""INSERT INTO facebook_users (id, first_name, middle_name, last_name, created, email)
                    VALUES (${user.id}, ${user.first_name}, ${user.middle_name}, ${user.last_name}, ${new Timestamp(Calendar.getInstance.getTime.getTime())}, ${user.email})""".update().apply()
            None
          }
        }
      }
    }

    private def findOrCreateUser(user: TwitterUser): Option[String] = {
      DB localTx { implicit session =>
        val ndla_id = sql"SELECT ndla_id FROM twitter_users WHERE id = ${user.id}".map { rs => rs.string("ndla_id") }.single.apply()

        ndla_id match {
          case Some(value) => Option(value)
          case None => {
            sql"""INSERT INTO twitter_users (id, first_name, middle_name, last_name, created, email)
                    VALUES (${user.id}, ${user.first_name}, ${user.middle_name}, ${user.last_name}, ${new Timestamp(Calendar.getInstance.getTime.getTime())}, ${user.email})""".update().apply()
            None
          }
        }
      }
    }

    private def findOrCreateUser(user: GoogleUser): Option[String] = {
      DB localTx { implicit session =>
        val ndla_id = sql"SELECT ndla_id FROM google_users WHERE id = ${user.id}".map { rs => rs.string("ndla_id") }.single.apply()

        ndla_id match {
          case Some(value) => Option(value)
          case None => {
            sql"""INSERT INTO google_users (id, first_name, middle_name, last_name, display_name, etag, object_type, email, created)
                    VALUES (${user.id}, ${user.name.flatMap(_.givenName).orNull}, ${user.name.flatMap(_.middleName).orNull},
                    ${user.name.flatMap(_.familyName).orNull}, ${user.displayName.orNull}, ${user.etag.orNull}, ${user.objectType.orNull}, ${user.email.orNull},
                    ${new Timestamp(Calendar.getInstance.getTime.getTime())})""".update().apply()
            None
          }
        }
      }
    }
  }
}


