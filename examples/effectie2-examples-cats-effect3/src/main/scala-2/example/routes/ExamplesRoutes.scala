package example.routes

import cats._
import cats.effect.Temporal
import cats.syntax.all._
import effectie.core._
import effectie.syntax.all._
import example.service.Example
import loggerf.cats.show._
import loggerf.cats.syntax.all._
import loggerf.core._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl
import types.{ErrorMessage, Result}

object ExamplesRoutes {

  def addRoutes[F[*]: Fx: Log: Monad](implicit dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "add" / IntVar(a) / IntVar(b) =>
        Example
          .add[F](a, b)
          .log(r => info(s"$a + $b = $r"))
          .map(Result(_))
          .log(a => info(show">>> $a"))
          .log(infoA)
          .flatMap(Ok(_))
    }
  }

  def divideRoutes[F[*]: Fx: Log: Monad](implicit dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "divide" / IntVar(a) / IntVar(b) =>
        Example
          .divide[F](a, b)
          .flatMap {
            case Right(r) => Ok(pureOf(Result(r)))
            case Left(err) => BadRequest(ErrorMessage(err.render))
          }
    }
  }

  def timeoutRoutes[F[*]: Fx: Log: Monad: Temporal](implicit dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "test-server-timeout" =>
        import scala.concurrent.duration._
        pureOf(">>> [test-server-timeout] Start to sleep for 30 seconds").log(infoA) >>
          Temporal[F].sleep(30.seconds) >>
          pureOf(">>> [test-server-timeout] Woke up from 30 second sleep").log(infoA) >>
          Ok("DONE")
    }
  }

  def takeSecondsRoutes[F[*]: Fx: Monad: Log: Temporal](implicit dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "take-seconds" / IntVar(a) =>
        import scala.concurrent.duration._
        pureOf(s">>> [take-seconds] Sleep for ${a.toString} seconds").log(infoA) >>
          Temporal[F].sleep(a.seconds) >>
          pureOf(s">>> [take-seconds] Woke up from ${a.toString} second sleep").log(infoA) >>
          Ok("DONE")
    }
  }

  def allRoutes[F[*]: Fx: Log: Monad: Temporal](implicit dsl: Http4sDsl[F]): HttpRoutes[F] =
    addRoutes <+> divideRoutes <+> timeoutRoutes

}
