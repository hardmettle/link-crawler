package com.lc.integration

import cats.effect.IO
import cats.effect.std.Queue
import cats.syntax.all._
import com.lc.consumer.HtmlConsumer
import com.lc.domain._
import com.lc.fetcher.HttpHtmlFetcher
import com.lc.output.ResultWriter
import com.lc.parser.JsoupHtmlParser
import com.lc.producer.UrlProducer
import munit.CatsEffectSuite
import org.http4s._
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

class LinkCrawlerIntegrationSpec extends CatsEffectSuite {

  implicit val logger: Logger[IO] =
    Slf4jLogger.getLogger[IO]

  test("crawls page and extracts links end to end") {

    val url =
      Url(
        uri"https://example.com"
      )

    val app: HttpApp[IO] =
      HttpApp[IO] {
        case GET -> Root =>
          IO.pure(
            Response[IO](
              status = Status.Ok
            ).withEntity(
              """
                |<html>
                | <body>
                |   <a href="https://google.com">
                |      Google
                |   </a>
                | </body>
                |</html>
          """.stripMargin
            )
          )

        case _ =>
          IO.pure(
            Response[IO](
              status = Status.NotFound
            )
          )
      }

    val client =
      Client.fromHttpApp[IO](app)

    val written =
      scala.collection.mutable.ListBuffer.empty[ParsedPage]

    val writer =
      new ResultWriter[IO] {
        override def write(page: ParsedPage): IO[Unit] =
          IO {
            written += page
          }
      }

    for {
      queue <- Queue.bounded[IO, Option[HtmlPage]](10)

      fetcher = new HttpHtmlFetcher[IO](client)

      producer =
        new UrlProducer[IO](
          urls = List(url),
          fetcher = fetcher,
          queue = queue,
          parallelism = 1,
          consumerCount = 1,
          logger = logger
        )

      consumer =
        new HtmlConsumer[IO](
          queue = queue,
          parser = new JsoupHtmlParser,
          writer = writer,
          logger = logger
        )

      _ <- (producer.run, consumer.run).parTupled

    } yield {
      assertEquals(
        written.toList,
        List(
          ParsedPage(
            url,
            List(
              uri"https://google.com"
            )
          )
        )
      )
    }
  }
}
