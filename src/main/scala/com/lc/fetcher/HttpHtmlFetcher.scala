package com.lc.fetcher

import cats.effect.Async
import cats.implicits.toFunctorOps
import com.lc.domain.{HtmlPage, Url}
import org.http4s.client.Client

final class HttpHtmlFetcher[F[_]: Async](
  client: Client[F]
) extends HtmlFetcher[F] {

  override def fetch(url: Url): F[HtmlPage] =
    client
      .expect[String](url.value)
      .map(html => HtmlPage(url, html))
}
