package com.lc.reader

import com.lc.domain.Url

trait UrlReader[F[_]] {

  def read: F[List[Url]]

}
