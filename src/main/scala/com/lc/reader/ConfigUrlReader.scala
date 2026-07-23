package com.lc.reader

import cats.effect.Sync
import com.lc.config.AppConfig
import com.lc.domain.Url

final class ConfigUrlReader[F[_]: Sync](
  config: AppConfig
) extends UrlReader[F] {

  override def read: F[List[Url]] =
    Sync[F].pure(config.urls)
}
