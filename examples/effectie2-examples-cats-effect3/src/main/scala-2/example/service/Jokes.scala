package example.service

import cats.Show
import cats.effect.Concurrent
import cats.syntax.all._
import effectie.core._
import effectie.syntax.all._
import eu.timepit.refined.types.string.NonEmptyString
import example.config.AppConfig.JokesConfig
import io.circe.Codec
import io.circe.generic.semiauto._
import io.estatico.newtype.macros.newtype
import loggerf.core._
import loggerf.cats.syntax.all._
import loggerf.cats.show._
import org.http4s.Method._
import org.http4s.Uri
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl

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

  def apply[F[*]: Fx: Concurrent: Log](uri: JokesUri)(C: Client[F])(implicit dsl: Http4sClientDsl[F]): Jokes[F] =
    new Jokes[F] {
      import dsl._

      import org.http4s.syntax.all._

      def get: F[Either[Jokes.JokeError, Jokes.Joke]] =
        C.expect[Joke](GET(uri.value))
          .catchNonFatal { err =>
            JokeError(NonEmptyString.unsafeFrom(s"Error when getting a joke from ${uri.value.show}"), err.some)
          }

      def testTimeout: F[String] =
        pureOf(">>> Start test-timeout").log(infoA) >>
          C.expect[String](GET(uri"http://0.0.0.0:8080/take-seconds/30"))

    }
}
