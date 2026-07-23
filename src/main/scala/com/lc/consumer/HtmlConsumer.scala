package com.lc.consumer

import cats.effect.Async
import cats.effect.std.Queue
import cats.syntax.all._
import com.lc.domain.HtmlPage
import com.lc.output.ResultWriter
import com.lc.parser.HtmlParser
import org.typelevel.log4cats.Logger

final class HtmlConsumer[F[_]: Async](
  queue: Queue[F, Option[HtmlPage]],
  parser: HtmlParser,
  writer: ResultWriter[F],
  logger: Logger[F]
) extends Consumer[F] {

  override def run: F[Unit] =
    consume

  private def consume: F[Unit] =
    queue.take.flatMap {
      case Some(page) =>
        process(page) >> consume

      case None =>
        Async[F].unit
    }

  private def process(page: HtmlPage): F[Unit] =
    Async[F]
      .delay(parser.parse(page))
      .attempt
      .flatMap {
        case Right(parsed) =>
          writer.write(parsed).handleErrorWith { error =>
            logger.error(error)(
              s"Failed writing ${page.url.value.renderString}"
            )
          }

        case Left(error) =>
          logger.error(error)(
            s"Failed parsing ${page.url.value.renderString}"
          )
      }
}
