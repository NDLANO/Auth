package no.ndla.auth.repository

import no.ndla.auth.exception.NoSuchUserException
import no.ndla.auth.model._
import no.ndla.auth.{TestEnvironment, UnitSuite}
import org.scalatest.PrivateMethodTester
import scalikejdbc.{DB, _}

class UsersRepositoryTest extends UnitSuite with TestEnvironment with PrivateMethodTester {

  val users = new UsersRepository

  test("That createNdlaUser returns an ndla user id") {
    val createNdlaUser = PrivateMethod[String]('createNdlaUser)
    val result = users invokePrivate createNdlaUser(TwitterUser("123", Some(""), Some(""), Some(""), Some(true)))
    assertResult(true) {
      result.isInstanceOf[String]
    }
  }
  test("That getNdlaUser returns an NdlaUser it exists") {
    val id = "5299e153-e601-19e5-96fe-e32a75c0b2b5"
    DB localTx { implicit session =>
      sql"INSERT INTO ndla_users (id, first_name, last_name) VALUES ($id, 'first', 'last')".update.apply()
    }
    val testUser = users.getNdlaUser(id)
    assertResult(true) {
      testUser.isInstanceOf[NdlaUser]
    }

    testUser.id should equal("5299e153-e601-19e5-96fe-e32a75c0b2b5")
  }

  test("That getNdlaUser throws a NoSuchUserException if the user does not exist") {
    intercept[NoSuchUserException] {
      users.getNdlaUser("5299e153-e601-19e5-96fe-e32a75c0b2b6")
    }
  }

  test("That getNdlaUserName returns an NdlaUserName when the user exists") {
    val id = "5299e153-e601-19e5-96fe-e32a75c0b2b7"
    DB localTx { implicit session =>
      sql"INSERT INTO ndla_users (id, first_name, middle_name, last_name) VALUES ($id, 'TestName', 'TestMiddleName', 'TestLastName')".update.apply()
    }
    val testUser = users.getNdlaUserName(id)

    testUser.first_name should equal(Some("TestName"))
    testUser.middle_name should equal(Some("TestMiddleName"))
    testUser.last_name should equal(Some("TestLastName"))
  }

  test("That getNdlaUserName throws a NoSuchUserException if the user does not exist") {
    intercept[NoSuchUserException] {
      users.getNdlaUserName("5299e153-e601-19e5-96fe-e32a75c0b2b8")
    }
  }

  test("That findOrCreateUser FacebookUser returns an ndla id if the user exists") {
    val id = "0123456789"
    DB localTx { implicit session =>
      sql"INSERT INTO facebook_users (id, ndla_id, first_name) VALUES ($id, $id, 'TestFirstName')".update.apply()
    }
    val facebookUser = new FacebookUser(id, Some(id), Some(""), Some(""), Some(""), Some(""), Some(""), true)
    val findOrCreateUser = PrivateMethod[String]('findOrCreateUser)
    assertResult(Some(id)) {
      users invokePrivate findOrCreateUser(facebookUser)
    }
  }

  test("That findOrCreateUser FacebookUser returns None if the user did not exist and was created") {
    val facebookUser = new FacebookUser("123", Some(""), Some(""), Some(""), Some(""), Some(""), Some(""), true)
    val findOrCreateUser = PrivateMethod[String]('findOrCreateUser)
    assertResult(None) {
      users invokePrivate findOrCreateUser(facebookUser)
    }
  }

  test("That findOrCreateUser TwitterUser returns an ndla id if the user exists") {
    val id = "0123456789"
    DB localTx { implicit session =>
      sql"INSERT INTO twitter_users (id, ndla_id, first_name) VALUES ($id, $id, 'TestFirstName')".update.apply()
    }
    val twitterUser = new TwitterUser(id, Some(""), Some(""), Some(""), Some(true))
    val findOrCreateUser = PrivateMethod[String]('findOrCreateUser)
    assertResult(Some(id)) {
      users invokePrivate findOrCreateUser(twitterUser)
    }
  }

  test("That findOrCreateUser TwitterUser returns None if the user did not exist and was created") {
    val twitterUser = new TwitterUser("123", Some(""), Some(""), Some(""), Some(true))
    val findOrCreateUser = PrivateMethod[String]('findOrCreateUser)
    assertResult(None) {
      users invokePrivate findOrCreateUser(twitterUser)
    }
  }

  test("That findOrCreateUser GoogleUser returns an ndla id if the user exists") {
    val id = "0123456789"
    DB localTx { implicit session =>
      sql"INSERT INTO google_users (id, ndla_id, first_name) VALUES ($id, $id, 'firstName')".update.apply()
    }

    val name = new Name(Some(""), Some(""), Some(""))
    val email = new Email("", "")
    val googleUser = new GoogleUser(id, Some(""), Some(""), Some(name), Some(""), List(email), Some(true))
    val findOrCreateUser = PrivateMethod[String]('findOrCreateUser)
    assertResult(Some(id)) {
      users invokePrivate findOrCreateUser(googleUser)
    }
  }

  test("That findOrCreateUser GoogleUser returns None if the user did not exist and was created") {
    val name = new Name(Some(""), Some(""), Some(""))
    val email = new Email("", "")
    val googleUser = new GoogleUser("123", Some(""), Some(""), Some(name), Some(""), List(email), Some(true))
    val findOrCreateUser = PrivateMethod[String]('findOrCreateUser)
    assertResult(None) {
      users invokePrivate findOrCreateUser(googleUser)
    }
  }
}
