package com.lc.output

import com.lc.domain.ParsedPage

trait ResultWriter[F[_]] {

  def write(result: ParsedPage): F[Unit]

}
