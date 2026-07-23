package com.lc.producer

import cats.effect.IO
import cats.effect.std.Queue
import com.lc.domain.{HtmlPage, Url}
import com.lc.fetcher.HtmlFetcher
import munit.CatsEffectSuite
import org.http4s.Uri
import org.typelevel.log4cats.slf4j.Slf4jLogger

class UrlProducerSpec extends CatsEffectSuite {

  implicit val logger =
    Slf4jLogger.getLogger[IO]

  test("successful fetches are added to queue") {

    val url =
      Url(Uri.unsafeFromString("https://example.com"))

    val fetcher =
      new HtmlFetcher[IO] {
        override def fetch(url: Url): IO[HtmlPage] =
          IO.pure(
            HtmlPage(
              url,
              "<html/>"
            )
          )
      }

    for {
      queue <- Queue.bounded[IO, Option[HtmlPage]](10)

      producer =
        new UrlProducer[IO](
          urls = List(url),
          fetcher = fetcher,
          queue = queue,
          parallelism = 2,
          consumerCount = 1,
          logger = logger
        )

      _ <- producer.run

      item <- queue.take

    } yield {
      assertEquals(item.map(_.url), Some(url))
    }
  }

  test("failed fetch does not stop other urls processing") {

    val goodUrl =
      Url(Uri.unsafeFromString("https://good.com"))

    val badUrl =
      Url(Uri.unsafeFromString("https://bad.com"))

    val fetcher =
      new HtmlFetcher[IO] {
        override def fetch(url: Url): IO[HtmlPage] =
          if (url == badUrl)
            IO.raiseError(
              new RuntimeException("boom")
            )
          else
            IO.pure(
              HtmlPage(
                url,
                "<html/>"
              )
            )
      }

    for {
      queue <- Queue.bounded[IO, Option[HtmlPage]](10)

      producer =
        new UrlProducer[IO](
          urls = List(goodUrl, badUrl),
          fetcher = fetcher,
          queue = queue,
          parallelism = 2,
          consumerCount = 1,
          logger = logger
        )

      _ <- producer.run

      item <- queue.take

    } yield {
      assertEquals(
        item.map(_.url),
        Some(goodUrl)
      )
    }
  }

  test("producer signals completion via None sentinels") {

    val url =
      Url(Uri.unsafeFromString("https://example.com"))

    val fetcher =
      new HtmlFetcher[IO] {
        override def fetch(url: Url): IO[HtmlPage] =
          IO.pure(
            HtmlPage(
              url,
              "<html/>"
            )
          )
      }

    val consumerCount = 2

    for {
      queue <- Queue.bounded[IO, Option[HtmlPage]](10)

      producer =
        new UrlProducer[IO](
          urls = List(url),
          fetcher = fetcher,
          queue = queue,
          parallelism = 1,
          consumerCount = consumerCount,
          logger = logger
        )

      _ <- producer.run

      _ <- queue.take

      sentinel1 <- queue.take
      sentinel2 <- queue.take

    } yield {
      assertEquals(sentinel1, None)
      assertEquals(sentinel2, None)
    }
  }
}
