package com.lc.parser

import com.lc.domain.{HtmlPage, Url}
import munit.FunSuite
import org.http4s.Uri

class JsoupHtmlParserSpec extends FunSuite {

  private val parser =
    new JsoupHtmlParser

  test("extracts hyperlinks from html") {

    val url =
      Url(Uri.unsafeFromString("https://example.com"))

    val page =
      HtmlPage(
        url,
        """
          |<html>
          | <body>
          |   <a href="/about">About</a>
          |   <a href="https://google.com">Google</a>
          | </body>
          |</html>
        """.stripMargin
      )

    val result =
      parser.parse(page)

    assertEquals(
      result.links,
      List(
        Uri.unsafeFromString("https://example.com/about"),
        Uri.unsafeFromString("https://google.com")
      )
    )
  }

  test("ignores invalid links") {

    val url =
      Url(Uri.unsafeFromString("https://example.com"))

    val page =
      HtmlPage(
        url,
        """
          |<a href="not a url">bad</a>
        """.stripMargin
      )

    val result =
      parser.parse(page)

    assertEquals(
      result.links,
      Nil
    )
  }

}
