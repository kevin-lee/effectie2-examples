package example.routes

import cats._
import cats.syntax.all._
import effectie.core.Fx
import effectie.syntax.all._
import eu.timepit.refined.auto._
import example.http4s.HttpError
import example.service.Jokes
import loggerf.instances.show._
import loggerf.syntax.all._
import loggerf.core.Log
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import types.ErrorMessage

/** @author Kevin Lee
  * @since 2022-01-27
  */
object JokeRoutes {

  def jokeRoutes[F[*]: Fx: Monad: Log](jokes: Jokes[F])(implicit dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "joke" =>
        for {
          joke <- jokes.get.log(errorA, infoA)
          resp <- joke.fold(err => InternalServerError(ErrorMessage(err.message)), j => Ok(j))
        } yield resp

      case GET -> Root / "test-client-timeout" =>
        jokes
          .testTimeout
          .log(info(prefix(">>> It finished without timeout: ")))
          .flatMap(Ok(_))
          .recoverFromNonFatalWith {
            case HttpError.ResponseError(_, err: java.util.concurrent.TimeoutException) =>
              logS_(s">>> Client timeout: ${err.getMessage}")(error) *>
                ServiceUnavailable(ErrorMessage("[Client] The request timed out"))
          }
    }
  }

}
