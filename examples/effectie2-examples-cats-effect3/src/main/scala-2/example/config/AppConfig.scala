package example.config

import effectie.core.Fx
import effectie.syntax.all._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric._
import eu.timepit.refined.pureconfig._
import example.config.AppConfig.ServerConfig
import io.estatico.newtype.macros.newtype
import org.http4s.Uri
import pureconfig._
import pureconfig.generic.semiauto._
import pureconfig.module.http4s._

/** @author Kevin Lee
  * @since 2022-02-17
  */
final case class AppConfig(server: ServerConfig)

object AppConfig {

  implicit val appConfigReader: ConfigReader[AppConfig] = deriveReader

  def load[F[*]: Fx](): F[ConfigReader.Result[AppConfig]] =
    effectOf(ConfigSource.default.load[AppConfig])

  final case class ServerConfig(host: Uri, port: PortNumber)
  object ServerConfig {
    implicit val serverConfigConfigReader: ConfigReader[ServerConfig] = deriveReader
  }

  @newtype case class PortNumber(value: PortNumber.Value)
  object PortNumber {
    type Value = Int Refined Interval.Closed[0, 65353]
    implicit val portNumberConfigReader: ConfigReader[PortNumber] = deriving
  }

}
