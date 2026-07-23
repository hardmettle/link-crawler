package com.lc.producer

import cats.effect.Async
import cats.effect.implicits.{concurrentParTraverseOps, monadCancelOps_}
import cats.effect.std.Queue
import cats.syntax.all._
import com.lc.domain.{HtmlPage, Url}
import com.lc.fetcher.HtmlFetcher
import org.typelevel.log4cats.Logger

final class UrlProducer[F[_]: Async](
  urls: List[Url],
  fetcher: HtmlFetcher[F],
  queue: Queue[F, Option[HtmlPage]],
  parallelism: Int,
  consumerCount: Int,
  logger: Logger[F]
) extends Producer[F] {

  override def run: F[Unit] =
    urls
      .parTraverseN(parallelism)(fetchAndEnqueue)
      .void
      .guarantee(signalDone)

  private def fetchAndEnqueue(url: Url): F[Unit] =
    fetcher
      .fetch(url)
      .attempt
      .flatMap {
        case Right(page) =>
          queue.offer(Some(page))

        case Left(error) =>
          logger.error(error)(s"Failed fetching ${url.value.renderString}")
      }

  private def signalDone: F[Unit] =
    List.fill(consumerCount)(queue.offer(None)).sequence_
}
