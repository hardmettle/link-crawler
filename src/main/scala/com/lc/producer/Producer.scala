package com.lc.producer

trait Producer[F[_]] {
  def run: F[Unit]
}
