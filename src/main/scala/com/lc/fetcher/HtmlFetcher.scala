package com.lc.fetcher

import com.lc.domain.{HtmlPage, Url}

trait HtmlFetcher[F[_]] {

  def fetch(url: Url): F[HtmlPage]

}
