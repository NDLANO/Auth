/*
 * Part of NDLA auth.
 * Copyright (C) 2016 NDLA
 *
 * See LICENSE
 *
 */

package no.ndla.auth

import no.ndla.network.secrets.PropertyKeys._
import org.scalatest._
import org.scalatest.mock.MockitoSugar

abstract class UnitSuite extends FunSuite with Matchers with OptionValues with Inside with Inspectors with MockitoSugar with BeforeAndAfterAll with BeforeAndAfter {

  setEnv("NDLA_ENVIRONMENT", "local")

  setEnv("GOOGLE_CLIENT_SECRET", "client-secret-key")
  setEnv("GOOGLE_CLIENT_ID", "client-id-key")
  setEnv("FACEBOOK_CLIENT_SECRET", "client-secret-key")
  setEnv("FACEBOOK_CLIENT_ID", "client-id-key")
  setEnv("TWITTER_API_KEY", "api-key")
  setEnv("TWITTER_CLIENT_SECRET", "client-secret-key")

  setEnv(MetaUserNameKey, "username")
  setEnv(MetaPasswordKey, "password")
  setEnv(MetaResourceKey, "resource")
  setEnv(MetaServerKey, "server")
  setEnv(MetaPortKey, "1234")
  setEnv(MetaSchemaKey, "schema")

  def setEnv(key: String, value: String) = {
    val field = System.getenv().getClass.getDeclaredField("m")
    field.setAccessible(true)
    val map = field.get(System.getenv()).asInstanceOf[java.util.Map[java.lang.String, java.lang.String]]
    map.put(key, value)
  }
}