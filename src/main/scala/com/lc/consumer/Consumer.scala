package com.lc.consumer

trait Consumer[F[_]] {
  def run: F[Unit]
}
