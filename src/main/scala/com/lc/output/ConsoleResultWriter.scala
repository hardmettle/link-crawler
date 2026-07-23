package com.lc.output

import cats.effect.std.Console
import com.lc.domain.ParsedPage

final class ConsoleResultWriter[F[_]: Console] extends ResultWriter[F] {

  override def write(page: ParsedPage): F[Unit] =
    Console[F].println(
      s"${page.url.value.renderString} -> ${page.links.map(_.renderString).mkString(", ")}"
    )
}
