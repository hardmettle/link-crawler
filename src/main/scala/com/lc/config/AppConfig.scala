package com.lc.config

import com.lc.domain.Url

final case class AppConfig(
  urls: List[Url],
  queueSize: Int,
  producerParallelism: Int,
  consumerCount: Int
)
