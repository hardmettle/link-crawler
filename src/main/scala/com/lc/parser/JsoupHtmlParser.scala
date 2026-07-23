package com.lc.parser

import com.lc.domain.{HtmlPage, ParsedPage}
import org.http4s.Uri
import org.jsoup.Jsoup

import scala.jdk.CollectionConverters._

final class JsoupHtmlParser extends HtmlParser {

  override def parse(page: HtmlPage): ParsedPage = {

    val document =
      Jsoup.parse(
        page.html,
        page.url.value.renderString
      )

    val links =
      document
        .select("a[href]")
        .asScala
        .flatMap(element => Uri.fromString(element.attr("abs:href")).toOption)
        .toList

    ParsedPage(
      url = page.url,
      links = links
    )
  }
}
