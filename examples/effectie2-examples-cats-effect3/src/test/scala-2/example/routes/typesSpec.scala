package example.routes

import extras.hedgehog.circe.RoundTripTester
import hedgehog._
import hedgehog.runner._

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
      message <- Gen.string(Gen.unicode, Range.linear(1, 100)).log("message")
    } yield {
      RoundTripTester(types.Result(message)).test()
    }

  def testJsonFormatResult: Property =
    for {
      message <- Gen.string(Gen.unicode, Range.linear(1, 100)).log("message")
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
