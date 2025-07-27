package example.routes

import cats.{Eq, Show}
import eu.timepit.refined.cats._
import eu.timepit.refined.types.string.NonEmptyString
import io.circe.generic.semiauto.{deriveCodec, deriveDecoder, deriveEncoder}
import io.circe.refined._
import io.circe.{Codec, Decoder, Encoder}

import scala.annotation.nowarn

object types {

  final case class Result[A](result: A)
  object Result {
    implicit def resultEq[A: Eq]: Eq[Result[A]]       = Eq.by(_.result)
    implicit def resultShow[A: Show]: Show[Result[A]] = cats.derived.semiauto.show

    @nowarn("""msg=evidence parameter .+ of type io\.circe\.Encoder\[A\] in method resultEncoder is never used""")
    implicit def resultEncoder[A: Encoder]: Encoder[Result[A]] = deriveEncoder
    @nowarn("""msg=evidence parameter .+ of type io\.circe\.Decoder\[A\] in method resultDecoder is never used""")
    implicit def resultDecoder[A: Decoder]: Decoder[Result[A]] = deriveDecoder
  }

  final case class ErrorMessage(message: NonEmptyString)
  object ErrorMessage {
    implicit val errorMessageEq: Eq[ErrorMessage]     = Eq.fromUniversalEquals
    implicit val errorMessageShow: Show[ErrorMessage] = cats.derived.semiauto.show

    implicit val codec: Codec[ErrorMessage] = deriveCodec
  }

}
