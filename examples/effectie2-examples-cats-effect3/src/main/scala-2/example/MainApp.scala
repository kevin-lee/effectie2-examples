package example

import cats.effect._
import cats.syntax.all._
import effectie.instances.ce3.fx._
import example.config.AppConfig
import extras.cats.syntax.all._
import loggerf.instances.cats._
import loggerf.logger._

/** @author Kevin Lee
  * @since 2022-01-30
  */
object MainApp extends IOApp {
  implicit val canLog: CanLog = Log4sLogger.log4sCanLog("example-app")
//  implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog("example-app")

  def run(args: List[String]): IO[ExitCode] =
    for {
      config   <- AppConfig
                    .load[IO]
                    .innerFoldF(err => IO.raiseError(new RuntimeException(err.prettyPrint(2))))(_.pure[IO])
      exitCode <- ExamplesServer
                    .stream[IO](config)
                    .compile
                    .drain
                    .as(ExitCode.Success)
    } yield exitCode
}
