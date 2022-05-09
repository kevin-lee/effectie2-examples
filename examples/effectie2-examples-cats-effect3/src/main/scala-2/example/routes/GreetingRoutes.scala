package example.routes

import cats._
import cats.effect._
import cats.syntax.all._
import effectie.syntax.all._
import effectie.core._
import eu.timepit.refined.types.string.NonEmptyString
import example.service.Greeter
import io.circe.generic.semiauto._
import io.circe.refined._
import io.circe.{Codec, Decoder, Encoder}
import io.estatico.newtype.macros.newtype
import loggerf.core.Log
import loggerf.cats.syntax.all._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl

object GreetingRoutes {

  def helloWorldRoutes[F[*]: Monad: Log](greeter: Greeter[F])(implicit dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "hello" / NonEmptyString(name) =>
        for {
          greeting <- greeter
                        .hello(Greeter.Name(name))
                        .log(x => info(s"""Saying "${x.show}""""))
          resp     <- Ok(greeting)
        } yield resp
    }
  }

  def greetRoutes[F[*]: Fx: Monad: Concurrent](greeter: Greeter[F])(implicit dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl._
    HttpRoutes.of[F] {
      case request @ POST -> Root / "greet" =>
        for {
          greetRequest    <- request.as[GreetRequest]
          greetingMessage <- pureOf(Greeter.GreetingMessage(greetRequest.greet.value))
          name            <- pureOf(Greeter.Name(greetRequest.to.value))
          greeting        <- greeter.greet(greetingMessage, name)
          resp            <- Ok(greeting)
        } yield resp
    }
  }

  def allRoutes[F[*]: Fx: Log: Monad: Concurrent](greeter: Greeter[F])(implicit dsl: Http4sDsl[F]): HttpRoutes[F] =
    helloWorldRoutes(greeter) <+> greetRoutes(greeter)

  final case class GreetRequest(greet: GreetRequest.Greet, to: GreetRequest.To)
  object GreetRequest {

    implicit val greetRequestCodec: Codec[GreetRequest] = deriveCodec

    @newtype case class Greet(value: NonEmptyString)
    object Greet {
      implicit val greetEncoder: Encoder[Greet] = deriving
      implicit val greetDecoder: Decoder[Greet] = deriving
    }
    @newtype case class To(value: NonEmptyString)
    object To {
      implicit val toEncoder: Encoder[To] = deriving
      implicit val toDecoder: Decoder[To] = deriving

    }

  }
}
