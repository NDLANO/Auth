package no.ndla.auth

import org.scalatest._
import org.scalatest.mock.MockitoSugar

abstract class UnitSuite extends FunSuite with Matchers with OptionValues with Inside with Inspectors with MockitoSugar with BeforeAndAfterAll with BeforeAndAfter {

}