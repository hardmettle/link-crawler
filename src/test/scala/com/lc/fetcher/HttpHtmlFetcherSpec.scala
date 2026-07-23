package com.lc.fetcher

import cats.effect.IO
import com.lc.domain.Url
import munit.CatsEffectSuite
import org.http4s.Method.GET
import org.http4s._
import org.http4s.client.Client
import org.http4s.implicits._
import org.http4s.dsl.io._

class HttpHtmlFetcherSpec extends CatsEffectSuite {

  test("fetches html successfully") {

    val url =
      Url(uri"https://example.com")

    val client =
      Client.fromHttpApp[IO](
        HttpApp[IO] {

          case GET -> Root / "page" =>
            IO.pure(
              Response[IO](
                status = Status.Ok
              ).withEntity(
                "<html><body>Hello</body></html>"
              )
            )

          case _ =>
            IO.pure(
              Response[IO](
                status = Status.NotFound
              )
            )

        }
      )

    val fetcher =
      new HttpHtmlFetcher[IO](client)

    for {

      page <-
        fetcher.fetch(
          Url(uri"https://example.com/page")
        )

    } yield {

      assertEquals(
        page.html,
        "<html><body>Hello</body></html>"
      )

    }

  }

  test("propagates http failures") {

    val client =
      Client.fromHttpApp[IO](
        HttpApp[IO] { case _ =>
          IO.pure(
            Response[IO](
              status = Status.InternalServerError
            )
          )
        }
      )

    val fetcher =
      new HttpHtmlFetcher[IO](client)

    val result =
      fetcher
        .fetch(
          Url(uri"https://example.com/fail")
        )
        .attempt

    result.map { response =>
      assert(
        response.isLeft
      )

    }

  }

}
