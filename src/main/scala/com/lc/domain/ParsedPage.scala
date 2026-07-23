package com.lc.domain

import org.http4s.Uri

final case class ParsedPage(
  url: Url,
  links: List[Uri]
)
