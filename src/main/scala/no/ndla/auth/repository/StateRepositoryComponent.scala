package no.ndla.auth.repository

import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit

import no.ndla.auth.exception.IllegalStateFormatException
import no.ndla.auth.integration.DataSourceComponent
import scalikejdbc._

import scala.util.{Failure, Success, Try}


trait StateRepositoryComponent {
  this: DataSourceComponent =>
  val stateRepository: StateRepository

  class StateRepository {
    ConnectionPool.singleton(new DataSourceConnectionPool(dataSource))
    val stateTimeToLiveInSeconds = TimeUnit.MINUTES.toSeconds(10)

    /**
      * Creates a new state variable in the database.
      *
      * @return the new state as a UUID
      */
    def createState(successUrl: String, failureUrl: String): UUID = {
      val uuid = UUID.randomUUID()
      DB localTx { implicit session =>
        sql"INSERT INTO state (id, success, failure) VALUES ($uuid, $successUrl, $failureUrl)".update().apply()
      }
      uuid
    }

    def getRedirectUrls(uuid: String): (String, String) = {
      DB readOnly { implicit session =>
        sql"SELECT success AS s, failure AS f FROM state WHERE id = ${UUID.fromString(uuid)}".map(rs => {
          (rs.string("s"), rs.string("f"))
        }).single().apply() match {
          case Some(value) => value
          case None => ("", "")
        }
      }
    }

    /**
      * Check that the given string is a valid state that exists in the database.
      * If the state is valid, true is returned. If the state does not exist, false is returned.
      * If the state is in an invalid format, an exception is returned.
      *
      * @param uuid the uuid
      * @return
      */
    def isStateValid(uuid: String): Boolean = {
      isStateValid(asUuid(uuid))
    }

    def isStateValid(uuid: UUID): Boolean = {
      DB localTx { implicit session =>
        val created = sql"SELECT created from state where id = $uuid".map(rs => rs.timestamp("created")).single().apply()
        sql"DELETE FROM state WHERE id = ${uuid}".update().apply()

        created match {
          case None => false
          case Some(time) => time.getTime() + stateTimeToLiveInSeconds > Instant.now().getEpochSecond();
        }
      }
    }

    private def asUuid(uuid: String): UUID = {
      Try(UUID.fromString(uuid)) match {
        case Success(validUUID) => validUUID
        case Failure(ex) => throw new IllegalStateFormatException("Invalid state", ex)
      }
    }
  }

}
