package example.config

import com.comcast.ip4s.{Hostname, Port}
import effectie.core.Fx
import effectie.syntax.all._
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

  @newtype case class PortNumber(value: Port)
  object PortNumber {
    implicit val portNumberConfigReader: ConfigReader[PortNumber] = deriving
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
