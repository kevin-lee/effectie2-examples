package example.service

import cats.Monad
import cats.syntax.all._
import effectie.syntax.all._
import effectie.core._
import eu.timepit.refined.types.string.NonEmptyString

/** @author Kevin Lee
  * @since 2022-01-27
  */
object Example {

  def add[F[*]: Fx](a: Int, b: Int): F[Int] = pureOf(a + b)

  def divide[F[*]: Fx: Monad](a: Int, b: Int): F[Either[ExampleError, Int]] =
    pureOrError(a / b)
      .map(_.asRight[ExampleError])
      .recoverFromNonFatal {
        case _: ArithmeticException =>
          ExampleError.divideByZero(a).asLeft[Int]
      }

  sealed trait ExampleError
  object ExampleError {

    final case class DivideByZero(dividend: Int) extends ExampleError
    def divideByZero(dividend: Int): ExampleError = DivideByZero(dividend)

    implicit class ExampleErrorOps(private val exampleError: ExampleError) extends AnyVal {
      def render: NonEmptyString = exampleError match {
        case ExampleError.DivideByZero(dividend) =>
          NonEmptyString.unsafeFrom(s"Error: Divide $dividend by zero")
      }
    }
  }
}
