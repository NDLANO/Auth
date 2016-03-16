package no.ndla.auth.integration

import no.ndla.auth.{TestEnvironment, UnitSuite}

class PackageTest extends UnitSuite with TestEnvironment {
  test("That toQueryStringFormat correctly converts a Map to a query string") {
    assert(toQueryStringFormat(Map("state" -> "testState")) == "state=testState")
    assert(toQueryStringFormat(Map("key1" -> "value1", "KEY2" -> "VALUE2")) == "key1=value1&KEY2=VALUE2")
    assert(toQueryStringFormat(Map("key1" -> "value1", "Key2" -> "value2", "key3" -> "value3")) == "key1=value1&Key2=value2&key3=value3")
  }
}
