package example

import cats.effect._
import cats.syntax.all._
import effectie.core._
import effectie.syntax.all._
import eu.timepit.refined.types.string.NonEmptyString
import example.config.AppConfig
import example.http4s.HttpClient
import example.routes.types.ErrorMessage
import example.routes.{GreetingRoutes, JokeRoutes}
import example.service.{Greeter, Jokes}
import extras.cats.syntax.option._
import fs2.io.net.Network
import loggerf.core._
import loggerf.syntax.all._
import org.http4s.Response
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.{AutoSlash, Logger, Timeout}
import org.http4s.syntax.all._

import scala.concurrent.duration._

/** @author Kevin Lee
  * @since 2022-01-30
  */
object ExamplesServer {

  def start[F[*]: Fx: Log: Async: Network: Temporal](config: AppConfig): F[ExitCode] =
    EmberClientBuilder
      .default[F]
      .withTimeout(config.jokes.client.requestTimeout)
      .withIdleConnectionTime(config.jokes.client.requestTimeout)
      .withHttp2
      .build
      .use { client =>
        val httpClient = HttpClient(client)
        val greeter    = Greeter[F]
        val jokes      = Jokes[F](Jokes.JokesUri.fromConfig(config.jokes))(httpClient)

        val allRoutes = {
          implicit val http4sDsl = Http4sDsl[F]
          AutoSlash(
            Timeout(
              config.server.responseTimeout,
              pureOf(
                Response
                  .timeout[F]
                  .withEntity(
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

        val httpApp = Logger.httpApp(
          logHeaders = true,
          logBody = true,
          logAction = ((msg: String) => msg.logS_(debug)).some
        )(allRoutes)
        //      httpApp = allRoutes
        EmberServerBuilder
          .default[F]
          .withHost(config.server.host)
          .withPort(config.server.port.value)
          .withHttp2
          .withHttpApp(httpApp)
          .withShutdownTimeout(1.second)
          .build
          .use(_ => Async[F].never[Unit])
      }
      .redeemWith(
        { err =>
          logS_(s">>> The app crashed due to ${err.getMessage}")(error) *>
            pureOf(ExitCode.Error)
        },
        _ => pureOf(ExitCode.Success)
      )

}
