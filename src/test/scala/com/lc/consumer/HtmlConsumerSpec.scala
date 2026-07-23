package com.lc.consumer

import cats.effect.IO
import cats.effect.std.Queue
import com.lc.domain._
import com.lc.output.ResultWriter
import com.lc.parser.HtmlParser
import munit.CatsEffectSuite
import org.http4s.Uri
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

class HtmlConsumerSpec extends CatsEffectSuite {

  implicit val logger: SelfAwareStructuredLogger[IO] =
    Slf4jLogger.getLogger[IO]

  test("consumer parses and writes pages") {

    val url =
      Url(Uri.unsafeFromString("https://example.com"))

    val page =
      HtmlPage(
        url,
        "<html/>"
      )

    val parser =
      new HtmlParser {
        override def parse(page: HtmlPage): ParsedPage =
          ParsedPage(
            page.url,
            List(
              Uri.unsafeFromString("https://google.com")
            )
          )
      }

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

      _ <- queue.offer(Some(page))
      _ <- queue.offer(None)

      consumer =
        new HtmlConsumer[IO](
          queue,
          parser,
          writer,
          logger
        )

      _ <- consumer.run

    } yield {
      assertEquals(
        written.toList,
        List(
          ParsedPage(
            url,
            List(Uri.unsafeFromString("https://google.com"))
          )
        )
      )
    }
  }
}
