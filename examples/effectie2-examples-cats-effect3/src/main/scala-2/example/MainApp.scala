package example

import cats.effect._
import cats.syntax.all._
import effectie.ce3.fx._
import example.config.AppConfig
import extras.cats.syntax.all._
import loggerf.instances.cats._
import loggerf.logger._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.Http4sDsl

/** @author Kevin Lee
  * @since 2022-01-30
  */
object MainApp extends IOApp {
  implicit val canLog: CanLog = Log4sLogger.log4sCanLog[MainApp.type]

  implicit val dsl: Http4sDsl[IO]             = org.http4s.dsl.io
  implicit val clientDsl: Http4sClientDsl[IO] = org.http4s.client.dsl.io

  def run(args: List[String]): IO[ExitCode] =
    for {
      config   <- AppConfig
                    .load[IO]
                    .eitherT
                    .foldF(
                      err => IO.raiseError[AppConfig](new RuntimeException(err.prettyPrint(2))),
                      _.pure[IO]
                    )
      exitCode <- ExamplesServer
                    .stream[IO](config)
                    .compile
                    .drain
                    .as(ExitCode.Success)
    } yield exitCode
}
