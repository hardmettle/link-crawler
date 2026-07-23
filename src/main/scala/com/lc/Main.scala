package com.lc

import cats.effect.std.Queue
import cats.effect.{IO, IOApp}
import cats.syntax.all._
import com.lc.config.ConfigLoader
import com.lc.consumer.HtmlConsumer
import com.lc.domain.HtmlPage
import com.lc.fetcher.HttpHtmlFetcher
import com.lc.output.ConsoleResultWriter
import com.lc.parser.JsoupHtmlParser
import com.lc.producer.UrlProducer
import com.lc.reader.ConfigUrlReader
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp.Simple {

  override def run: IO[Unit] =
    application

  private def application: IO[Unit] =
    for {

      config   <-
        ConfigLoader.load[IO]
      urlReader =
        new ConfigUrlReader[IO](config)

      urls <- urlReader.read
      _    <-
        EmberClientBuilder
          .default[IO]
          .build
          .use { client =>
            for {
              queue  <- Queue.bounded[IO, Option[HtmlPage]](config.queueSize)
              logger <- Slf4jLogger.create[IO]
              fetcher = new HttpHtmlFetcher[IO](client)
              parser  = new JsoupHtmlParser
              writer  = new ConsoleResultWriter[IO]

              producer =
                new UrlProducer[IO](
                  urls,
                  fetcher,
                  queue,
                  config.producerParallelism,
                  config.consumerCount,
                  logger
                )

              consumers =
                List.fill(config.consumerCount) {
                  new HtmlConsumer[IO](
                    queue,
                    parser,
                    writer,
                    logger
                  )
                }

              producerFiber  <- producer.run.start
              consumerFibers <- consumers.traverse(_.run.start)
              _              <- producerFiber.join
              _              <- consumerFibers.traverse_(_.join)

            } yield ()
          }

    } yield ()
}
