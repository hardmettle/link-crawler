package com.lc.reader

import cats.effect.IO
import com.lc.config.AppConfig
import com.lc.domain.Url
import munit.CatsEffectSuite
import org.http4s.Uri

class ConfigUrlReaderSpec extends CatsEffectSuite {

  test("reads urls from configuration") {

    val urls =
      List(
        Url(Uri.unsafeFromString("https://example.com")),
        Url(Uri.unsafeFromString("https://scala-lang.org"))
      )

    val config =
      AppConfig(
        urls = urls,
        queueSize = 10,
        producerParallelism = 2,
        consumerCount = 1
      )

    val reader =
      new ConfigUrlReader[IO](config)

    reader.read.map { result =>
      assertEquals(
        result,
        urls
      )
    }
  }
}
