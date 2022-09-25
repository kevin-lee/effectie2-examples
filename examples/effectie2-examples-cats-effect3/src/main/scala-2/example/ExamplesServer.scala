package example

import cats.effect._
import cats.syntax.all._
import effectie.core._
import effectie.syntax.all._
import eu.timepit.refined.types.string.NonEmptyString
import example.config.AppConfig
import example.routes.types.ErrorMessage
import example.routes.{GreetingRoutes, JokeRoutes}
import example.service.{Greeter, Jokes}
import extras.cats.syntax.option._
import fs2.Stream
import loggerf.core._
import loggerf.syntax.all._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.{AutoSlash, Logger, Timeout}
import org.http4s.syntax.all._
import org.http4s.{Response, Status}

/** @author Kevin Lee
  * @since 2022-01-30
  */
object ExamplesServer {

  def stream[F[*]: Fx: Log: Async: Http4sDsl: Http4sClientDsl: Temporal](config: AppConfig): Stream[F, Nothing] =
    (for {
      client <- Stream.resource(
                  EmberClientBuilder
                    .default[F]
                    .withTimeout(config.jokes.client.requestTimeout)
                    .build
                )
      greeter = Greeter[F]
      jokes   = Jokes[F](Jokes.JokesUri.fromConfig(config.jokes))(client)

      allRoutes = {
        AutoSlash(
          Timeout(
            config.server.responseTimeout,
            pureOf(
              Response[F](Status.ServiceUnavailable).withEntity(
                ErrorMessage(
                  NonEmptyString.unsafeFrom(
                    s"Response timed out after ${config.server.responseTimeout.toSeconds} seconds"
                  )
                )
              )
            ).someT,
          )(
            example.routes.ExamplesRoutes.allRoutes <+> GreetingRoutes.allRoutes[F](greeter)
          ) <+> JokeRoutes.jokeRoutes[F](jokes) <+> example.routes.ExamplesRoutes.takeSecondsRoutes
        ).orNotFound
      }

      httpApp = Logger.httpApp(
                  logHeaders = true,
                  logBody = true,
                  logAction = ((msg: String) => pureOf(msg).log(info).void).some
                )(allRoutes)

      exitCode <- Stream.resource(
                    EmberServerBuilder
                      .default[F]
                      .withHost(config.server.host)
                      .withPort(config.server.port.toPort)
                      .withHttpApp(httpApp)
                      .build >> Resource.eval(Async[F].never)
                  )
    } yield exitCode).drain
}
