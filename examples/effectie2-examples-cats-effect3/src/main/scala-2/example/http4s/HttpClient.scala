package example.http4s

import cats.effect._
import cats.syntax.all._
import effectie.core.Fx
import effectie.syntax.all._
import io.circe.Decoder
import org.http4s.Status.{Successful => H4sSuccessful}
import org.http4s._
import org.http4s.client.{Client, UnexpectedStatus}

/** @author Kevin Lee
  * @since 2020-05-10
  */
trait HttpClient[F[*]] {
  def send[A: Decoder](request: Request[F]): F[A]
  def sendWith[A: Decoder](request: F[Request[F]]): F[A]

  def sendAndHandle[A](request: Request[F])(handler: HttpClient.HttpResponse[F] => F[A]): F[A]

  def sendWithAndHandle[A](request: F[Request[F]])(handler: HttpClient.HttpResponse[F] => F[A]): F[A]
}
object HttpClient {

  def apply[F[*]: Fx: Concurrent](client: Client[F]): HttpClient[F] =
    new HttpClientF[F](client)

  private final class HttpClientF[F[*]: Fx: Concurrent](client: Client[F]) extends HttpClient[F] {
    import org.http4s.circe.CirceEntityCodec._
    override def send[A: Decoder](request: Request[F]): F[A] = {
      val entityDecoder = EntityDecoder[F, A]
      val theRequest    = if (entityDecoder.consumes.nonEmpty) {
        import org.http4s.headers._
        val mediaRanges = entityDecoder.consumes.toList
        mediaRanges match {
          case head :: tail =>
            request.addHeader(Accept(MediaRangeAndQValue(head), tail.map(MediaRangeAndQValue(_)): _*))
          case Nil =>
            request
        }
      } else request
      sendAndHandle(theRequest) {
        case HttpResponse.Successful(res) =>
          entityDecoder.decode(res, strict = false).leftWiden[Throwable].rethrowT
        case HttpResponse.Failed(res) =>
          errorOf[F](UnexpectedStatus(res.status, request.method, request.uri))
      }
    }

    override def sendWith[A: Decoder](request: F[Request[F]]): F[A] =
      request.flatMap(send[A](_))

    override def sendAndHandle[A](request: Request[F])(handler: HttpResponse[F] => F[A]): F[A] =
      client
        .run(request)
        .use(response => handler(HttpResponse.fromHttp4s[F](response)))

    override def sendWithAndHandle[A](request: F[Request[F]])(handler: HttpResponse[F] => F[A]): F[A] =
      request.flatMap(sendAndHandle(_)(handler))
  }

  sealed trait HttpResponse[F[*]]
  object HttpResponse {

    final case class Successful[F[*]] private (value: Response[F]) extends HttpResponse[F]

    final case class Failed[F[*]] private (value: Response[F]) extends HttpResponse[F]

    def successful[F[*]](value: Response[F]): HttpResponse[F] = Successful[F](value)

    def failed[F[*]](value: Response[F]): HttpResponse[F] = Failed[F](value)

    def fromHttp4s[F[*]](response: Response[F]): HttpResponse[F] =
      H4sSuccessful.unapply(response).fold(failed(response))(successful)

    implicit class HttpResponseOps[F[*]](private val httpResponse: HttpResponse[F]) extends AnyVal {

      def response: Response[F] = httpResponse match {
        case HttpResponse.Successful(response) => response
        case HttpResponse.Failed(response) => response
      }
    }

  }

}
