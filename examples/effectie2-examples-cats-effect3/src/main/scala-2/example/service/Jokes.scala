package example.service

import cats.Show
import cats.effect.Concurrent
import cats.syntax.all._
import effectie.core._
import effectie.syntax.all._
import eu.timepit.refined.types.string.NonEmptyString
import example.config.AppConfig.JokesConfig
import example.http4s.HttpClient
import io.circe.Codec
import io.circe.generic.semiauto._
import io.estatico.newtype.macros.newtype
import loggerf.core._
import loggerf.syntax.all._
import org.http4s.Method._
import org.http4s.{MediaType, Uri}
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.headers.Accept

import scala.util.control.NoStackTrace

trait Jokes[F[*]] {
  def get: F[Either[Jokes.JokeError, Jokes.Joke]]

  def testTimeout: F[String]
}

object Jokes {

  final case class Joke(joke: String)
  object Joke {
    implicit val jokeCodec: Codec[Joke] = deriveCodec

    implicit val jokeShow: Show[Joke] = Show.fromToString
  }

  final case class JokeError(message: NonEmptyString, cause: Option[Throwable]) extends NoStackTrace {
    override def getMessage: String = message.value

    override def getCause: Throwable = cause.orNull
  }
  object JokeError {
    implicit val jokeErrorShow: Show[JokeError] = _.getMessage
  }

  @newtype case class JokesUri(value: Uri)
  object JokesUri {
    def fromConfig(jokesConfig: JokesConfig): JokesUri = JokesUri(jokesConfig.uri.value)
  }

  def apply[F[*]: Fx: Concurrent: Log](uri: JokesUri)(C: HttpClient[F])(implicit dsl: Http4sClientDsl[F]): Jokes[F] =
    new Jokes[F] {
      import dsl._
      import org.http4s.syntax.all._

      def get: F[Either[Jokes.JokeError, Jokes.Joke]] =
        C.sendAndHandle(GET(uri.value, Accept(MediaType.application.json))) {
          case HttpClient.HttpResponse.Successful(res) =>
            res
              .as[Joke]
              .catchNonFatal {
                case err =>
                  JokeError(NonEmptyString.unsafeFrom(s"Error when getting a joke from ${uri.value.show}"), err.some)
              }
          case HttpClient.HttpResponse.Failed(res) =>
            import extras.fs2.text.syntax._
            res
              .body
              .utf8String
              .log(message => error(s"Failed response: ${res.status.show}, body: $message"))
              .map { message =>
                JokeError(
                  NonEmptyString.unsafeFrom(
                    s"Error when getting a joke from ${uri.value.show}. Message: ${message.show}"
                  ),
                  none
                ).asLeft[Jokes.Joke]
              }
        }

      def testTimeout: F[String] =
        ">>> Start test-timeout".logS(info) *>
          C.send[String](GET(uri"http://0.0.0.0:8080/take-seconds/30"))

    }
}
