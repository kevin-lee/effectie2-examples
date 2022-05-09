package example.service

import cats.Show
import effectie.core._
import effectie.syntax.all._
import eu.timepit.refined.cats._
import eu.timepit.refined.types.string.NonEmptyString
import io.circe.refined._
import io.circe.{Decoder, Encoder, Json}
import io.estatico.newtype.macros.newtype

trait Greeter[F[*]] {
  def hello(n: Greeter.Name): F[Greeter.Greeting]
  def greet(greetingMessage: Greeter.GreetingMessage, name: Greeter.Name): F[Greeter.Greeting]
}

object Greeter {

  @newtype case class Name(value: NonEmptyString)
  object Name {
    implicit val nameEncoder: Encoder[Name] = deriving
    implicit val nameDecoder: Decoder[Name] = deriving
  }

  @newtype case class GreetingMessage(value: NonEmptyString)
  object GreetingMessage {
    implicit val greetingMessageEncoder: Encoder[GreetingMessage] = deriving
    implicit val greetingMessageDecoder: Decoder[GreetingMessage] = deriving
  }

  @newtype case class Greeting(value: NonEmptyString)
  object Greeting {
    implicit val greetingEncoder: Encoder[Greeting] =
      greeting =>
        Json.obj(
          "message" -> Json.fromString(greeting.value.value),
        )

    implicit val greetingShow: Show[Greeting] = deriving
  }

  def apply[F[*]: Fx]: Greeter[F] = new GreeterF

  final class GreeterF[F[*]: Fx] extends Greeter[F] {
    def hello(name: Greeter.Name): F[Greeter.Greeting] = {
      pureOf(Greeting(NonEmptyString.unsafeFrom(s"Hello, ${name.value}")))
    }

    def greet(greetingMessage: GreetingMessage, name: Greeter.Name): F[Greeter.Greeting] =
      pureOf(Greeting(NonEmptyString.unsafeFrom(s"${greetingMessage.value} ${name.value}")))
  }

}
