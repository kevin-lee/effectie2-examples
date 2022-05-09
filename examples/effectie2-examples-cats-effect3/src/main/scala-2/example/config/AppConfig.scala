package example.config

import com.comcast.ip4s.{Hostname, Port}
import effectie.core.Fx
import effectie.syntax.all._
import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.numeric._
import eu.timepit.refined.pureconfig._
import example.config.AppConfig.{JokesConfig, ServerConfig}
import io.estatico.newtype.macros.newtype
import org.http4s.Uri
import pureconfig._
import pureconfig.generic.semiauto._
import pureconfig.module.http4s._
import pureconfig.module.ip4s._

import scala.concurrent.duration.FiniteDuration

/** @author Kevin Lee
  * @since 2022-02-17
  */
final case class AppConfig(server: ServerConfig, jokes: JokesConfig)

object AppConfig {

  implicit val appConfigReader: ConfigReader[AppConfig] = deriveReader

  def load[F[*]: Fx]: F[ConfigReader.Result[AppConfig]] =
    effectOf(ConfigSource.default.load[AppConfig])

  final case class ServerConfig(host: Hostname, port: PortNumber, responseTimeout: FiniteDuration)
  object ServerConfig {
    implicit val serverConfigConfigReader: ConfigReader[ServerConfig] = deriveReader
  }

  @newtype case class PortNumber(value: PortNumber.Value)
  object PortNumber {
    type Value = Int Refined Interval.Closed[0, 65535]
    object Value extends RefinedTypeOps[Value, Int]

    implicit val portNumberConfigReader: ConfigReader[PortNumber] = deriving

    implicit class PortNumberOps(private val portNumber: PortNumber) extends AnyVal {
      def toPort: Port = Port
        .fromInt(portNumber.value.value)
        .getOrElse(sys.error(s"Invalid port number but it's impossible!!! ${portNumber.value.value.toString}"))
    }
  }

  final case class JokesConfig(uri: JokesConfig.JokesUri, client: JokesConfig.ClientConfig)
  object JokesConfig {

    @newtype case class JokesUri(value: Uri)
    object JokesUri {
      implicit val jokesUriConfigReader: ConfigReader[JokesUri] = deriving
    }

    final case class ClientConfig(requestTimeout: FiniteDuration)
    object ClientConfig {
      implicit val clientConfigConfigReader: ConfigReader[ClientConfig] = deriveReader
    }

    implicit val jokesConfigConfigReader: ConfigReader[JokesConfig] = deriveReader

  }
}
