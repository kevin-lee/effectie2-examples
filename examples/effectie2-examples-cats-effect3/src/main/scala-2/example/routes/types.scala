package example.routes

import cats.{Eq, Show}
import eu.timepit.refined.cats._
import eu.timepit.refined.types.string.NonEmptyString
import io.circe.generic.semiauto.{deriveCodec, deriveDecoder, deriveEncoder}
import io.circe.refined._
import io.circe.{Codec, Decoder, Encoder}

object types {

  final case class Result[A](result: A)
  object Result {
    implicit def resultEq[A: Eq]: Eq[Result[A]]       = Eq.by(_.result)
    implicit def resultShow[A: Show]: Show[Result[A]] = cats.derived.semiauto.show

    implicit def resultEncoder[A: Encoder]: Encoder[Result[A]] = deriveEncoder
    implicit def resultDecoder[A: Decoder]: Decoder[Result[A]] = deriveDecoder
  }

  final case class ErrorMessage(message: NonEmptyString)
  object ErrorMessage {
    implicit val errorMessageEq: Eq[ErrorMessage]     = Eq.fromUniversalEquals
    implicit val errorMessageShow: Show[ErrorMessage] = cats.derived.semiauto.show

    implicit val codec: Codec[ErrorMessage] = deriveCodec
  }

}
