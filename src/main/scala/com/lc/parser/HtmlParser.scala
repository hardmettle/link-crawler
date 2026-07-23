package com.lc.parser

import com.lc.domain.{HtmlPage, ParsedPage}

trait HtmlParser {

  def parse(page: HtmlPage): ParsedPage

}
