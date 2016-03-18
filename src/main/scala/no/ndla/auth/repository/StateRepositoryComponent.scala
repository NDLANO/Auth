package no.ndla.auth.repository

import java.util.UUID

import no.ndla.auth.exception.IllegalStateFormatException
import no.ndla.auth.integration.DataSourceComponent
import scalikejdbc._
import scala.util.{Failure, Success, Try}


trait StateRepositoryComponent {
    this: DataSourceComponent =>
    val stateRepository: StateRepository

    class StateRepository {

        ConnectionPool.singleton(new DataSourceConnectionPool(dataSource))

        /**
          * Creates a new state variable in the database.
          *
          * @return the new state as a UUID
          */
        def createState(successUrl: String, failureUrl: String): UUID = {
            val uuid = UUID.randomUUID()
            using(ConnectionPool.borrow()) { conn: java.sql.Connection =>
                val pst = conn.prepareStatement("INSERT INTO state (id, success, failure) VALUES (?, ?, ?)")
                pst.setObject(1, uuid)
                pst.setString(2, successUrl)
                pst.setString(3, failureUrl)
                pst.executeUpdate()
            }
            uuid
        }

        def getRedirectUrls(uuid: String): (String, String) = {
            val rs = using(ConnectionPool.borrow()) { conn: java.sql.Connection =>
                val pst = conn.prepareStatement("SELECT success, failure FROM state WHERE id = ?")
                pst.setObject(1, UUID.fromString(uuid))
                pst.executeQuery()
            }

            rs.next() match {
                case false => ("", "") // No rows were retrieved
                case true => (rs.getString("success"), rs.getString("failure"))
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
            using(ConnectionPool.borrow()) { conn: java.sql.Connection =>
                val pst = conn.prepareStatement("DELETE FROM state WHERE id = ?")
                pst.setObject(1, uuid)
                pst.executeUpdate() // "returns either (1) the row count for SQL Data Manipulation Language (DML) statements
                // or (2) 0 for SQL statements that return nothing"
            } == 1
        }

        private def asUuid(uuid: String): UUID = {
            Try(UUID.fromString(uuid)) match {
                case Success(validUUID) => validUUID
                case Failure(ex) => throw new IllegalStateFormatException("Invalid state", ex)
            }
        }
    }
}
