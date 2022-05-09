package example.routes

import cats.Show
import cats.syntax.all._
import eu.timepit.refined.types.string.NonEmptyString
import io.circe.generic.semiauto.{deriveCodec, deriveDecoder, deriveEncoder}
import io.circe.refined._
import io.circe.{Codec, Decoder, Encoder}

object types {

  final case class Result[A](result: A)
  object Result {
    implicit def resultEncoder[A: Encoder]: Encoder[Result[A]] = deriveEncoder
    implicit def resultDecoder[A: Decoder]: Decoder[Result[A]] = deriveDecoder

    implicit def resultShow[A: Show]: Show[Result[A]] = result => s"Result(${result.result.show})"
  }

  final case class ErrorMessage(message: NonEmptyString)
  object ErrorMessage {
    implicit val codec: Codec[ErrorMessage] = deriveCodec
  }

}
