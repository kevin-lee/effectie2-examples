package example.routes

import cats._
import cats.effect.Temporal
import cats.syntax.all._
import effectie.core._
import effectie.syntax.all._
import example.service.Example
import loggerf.core._
import loggerf.instances.show._
import loggerf.syntax.all._
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
          .log(infoAWith(prefix(">>> ")))
          .log(infoA)
          .flatMap(Ok(_))
    }
  }

  def divideRoutes[F[*]: Fx: Monad](implicit dsl: Http4sDsl[F]): HttpRoutes[F] = {
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

  def timeoutRoutes[F[*]: Log: Temporal](implicit dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "test-server-timeout" =>
        import scala.concurrent.duration._
        ">>> [test-server-timeout] Start to sleep for 30 seconds".logS(info) *>
          Temporal[F].sleep(30.seconds) *>
          ">>> [test-server-timeout] Woke up from 30 second sleep".logS(info) *>
          Ok("DONE")
    }
  }

  def takeSecondsRoutes[F[*]: Log: Temporal](implicit dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "take-seconds" / IntVar(a) =>
        import scala.concurrent.duration._
        s">>> [take-seconds] Sleep for ${a.toString} seconds".logS_(info) *>
          Temporal[F].sleep(a.seconds) *>
          s">>> [take-seconds] Woke up from ${a.toString} second sleep".logS_(info) *>
          Ok("DONE")
    }
  }

  def allRoutes[F[*]: Fx: Log: Temporal](implicit dsl: Http4sDsl[F]): HttpRoutes[F] =
    addRoutes <+> divideRoutes <+> timeoutRoutes

}
