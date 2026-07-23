package com.lc.config

import cats.effect.Sync
import com.lc.domain.Url
import org.http4s.Uri
import pureconfig._
import pureconfig.generic.auto._

object ConfigLoader {

  private final case class FileConfig(
    urls: List[String],
    queueSize: Int,
    producerParallelism: Int,
    consumerCount: Int
  )

  def load[F[_]: Sync]: F[AppConfig] =
    Sync[F].delay {

      val config =
        ConfigSource
          .resources("application.conf")
          .at("crawler")
          .loadOrThrow[FileConfig]

      AppConfig(
        urls = config.urls.map { url =>
          Url(Uri.unsafeFromString(url))
        },
        queueSize = config.queueSize,
        producerParallelism = config.producerParallelism,
        consumerCount = config.consumerCount
      )
    }
}
