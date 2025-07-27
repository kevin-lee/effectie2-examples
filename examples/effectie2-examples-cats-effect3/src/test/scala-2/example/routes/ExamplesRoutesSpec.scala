package example.routes

import cats.effect._
import effectie.core._
import eu.timepit.refined.types.string.NonEmptyString
import example.service.Greeter
import extras.hedgehog.ce3.CatsEffectRunner
import hedgehog._
import hedgehog.runner._
import loggerf.core.Log
import loggerf.instances.cats._
import loggerf.logger.CanLog
import org.http4s._
import org.http4s.dsl.Http4sDsl

/** @author Kevin Lee
  * @since 2022-01-29
  */
object ExamplesRoutesSpec extends Properties with CatsEffectRunner {
  type F[A] = IO[A]
  val F = IO

  override val tests: List[Test] = List(
    property("test Greeter.hello returns status code 200", testGreeterReturnsStatusCode200),
    property("test Greeter.hello returns hello message", testGreeterReturnsHelloMessage),
    property("test Greeter.greet returns status code 200", testGreeterGreetReturnsStatusCode200),
    property("test Greeter.greet returns greeting message", testGreeterGreetReturnsGreetingMessage),
  )

  implicit val canLog: CanLog = new loggerf.logger.CanLog {
    override def debug(message: => String): Unit = println(s"[DEBUG] $message")
    override def info(message: => String): Unit  = println(s"[INFO] $message")
    override def warn(message: => String): Unit  = println(s"[WARN] $message")
    override def error(message: => String): Unit = println(s"[ERROR] $message")
  }

  def testGreeterReturnsStatusCode200: Property =
    for {
      name <- Gen
                .string(Gen.alpha, Range.linear(1, 10))
                .map((Greeter.Name.apply _).compose(NonEmptyString.unsafeFrom))
                .log("name")
    } yield withIO { implicit ticker =>
      implicit val dsl: Http4sDsl[F] = org.http4s.dsl.io

      import effectie.instances.ce3.fx._
      val expected = Status.Ok
      val actual   = helloWorld[F](name).map(_.status)
      actual.completeThen(status => status ==== expected)
    }

  def testGreeterReturnsHelloMessage: Property =
    for {
      name <- Gen
                .string(Gen.alpha, Range.linear(1, 10))
                .map(Greeter.Name.apply _ compose NonEmptyString.unsafeFrom)
                .log("name")
    } yield withIO { implicit ticker =>
      implicit val dsl: Http4sDsl[F] = org.http4s.dsl.io

      import effectie.instances.ce3.fx._
      val expected = s"{\"message\":\"Hello, ${name.value}\"}"
      val actual   = helloWorld[F](name).flatMap(_.as[String])
      actual.completeThen(message => message ==== expected)
    }

  def testGreeterGreetReturnsStatusCode200: Property =
    for {
      message <- Gen
                   .string(Gen.alpha, Range.linear(5, 20))
                   .map(Greeter.GreetingMessage.apply _ compose NonEmptyString.unsafeFrom)
                   .log("message")
      name    <-
        Gen
          .string(Gen.alpha, Range.linear(1, 10))
          .map(Greeter.Name.apply _ compose NonEmptyString.unsafeFrom)
          .log("name")
    } yield withIO { implicit ticker =>
      implicit val dsl: Http4sDsl[F] = org.http4s.dsl.io

      import effectie.instances.ce3.fx._
      val expected = Status.Ok
      val actual   = greet[F](message, name).map(_.status)
      actual.completeThen(status => status ==== expected)
    }

  def testGreeterGreetReturnsGreetingMessage: Property =
    for {
      message <- Gen
                   .string(Gen.alpha, Range.linear(5, 20))
                   .map(Greeter.GreetingMessage.apply _ compose NonEmptyString.unsafeFrom)
                   .log("message")
      name    <-
        Gen
          .string(Gen.alpha, Range.linear(1, 10))
          .map(Greeter.Name.apply _ compose NonEmptyString.unsafeFrom)
          .log("name")
    } yield withIO { implicit ticker =>
      implicit val dsl: Http4sDsl[F] = org.http4s.dsl.io

      import effectie.instances.ce3.fx._
      val expected = s"{\"message\":\"${message.value} ${name.value}\"}"
      val actual   = greet[F](message, name).flatMap(_.as[String])
      actual.completeThen(message => message ==== expected)
    }

  private[this] def helloWorld[G[*]: Fx: Log: Http4sDsl: Temporal](name: Greeter.Name): G[Response[G]] = {
    val getHW   = Request[G](Method.GET, Uri.unsafeFromString(s"/hello/${name.value}"))
    val greeter = Greeter[G]
    GreetingRoutes.helloWorldRoutes(greeter).orNotFound(getHW)
  }

  private[this] def greet[G[*]: Fx: Http4sDsl: Temporal](
    message: Greeter.GreetingMessage,
    name: Greeter.Name
  ): G[Response[G]] = {
    val greetRequest = GreetingRoutes.GreetRequest(
      GreetingRoutes.GreetRequest.Greet(message.value),
      GreetingRoutes.GreetRequest.To(name.value)
    )
    import org.http4s.circe.CirceEntityCodec._
    val getHW        = Request[G](Method.POST, Uri.unsafeFromString(s"/greet")).withEntity(greetRequest)
    val greeter      = Greeter[G]
    GreetingRoutes.greetRoutes(greeter).orNotFound(getHW)
  }

}
