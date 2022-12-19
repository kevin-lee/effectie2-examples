package example.routes

import eu.timepit.refined.cats._
import eu.timepit.refined.types.numeric.PosInt
import extras.hedgehog.circe.RoundTripTester
import hedgehog._
import hedgehog.extra.refined._
import hedgehog.runner._
import io.circe.refined._

/** @author Kevin Lee
  * @since 2022-11-29
  */
object typesSpec extends Properties {
  override def tests: List[Test] = List(
    property("round-trip test Result", roundTripTestResult),
    property("test JSON format for Result", testJsonFormatResult),
  )

  def roundTripTestResult: Property =
    for {
      message <- StringGens.genNonWhitespaceString(PosInt(100)).log("message")
    } yield {
      RoundTripTester(types.Result(message)).test()
    }

  def testJsonFormatResult: Property =
    for {
      message <- StringGens.genNonWhitespaceString(PosInt(100)).log("message")
    } yield {
      import io.circe.literal._

      val expected =
        json"""
          {
            "result": $message
          }
        """

      import io.circe.syntax._
      val actual = types.Result(message).asJson

      actual ==== expected
    }

}
