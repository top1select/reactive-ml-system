package com.reactivemachinelearning.feature

import org.conglomerate.utils.RawData

trait Generator[V] {
  def generate(t: RawData): Feature[V,_,_,_]
}
