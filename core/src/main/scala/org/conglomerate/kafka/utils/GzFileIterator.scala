package org.conglomerate.kafka.utils

import java.io.{ BufferedReader, FileInputStream, InputStreamReader }
import java.util.zip.GZIPInputStream

object GzFileIterator extends FileContentIterator {
  def apply(file: java.io.File, encoding: String) = {
    new BufferedReaderIterator(
      new BufferedReader(
        new InputStreamReader(new GZIPInputStream(new FileInputStream(file)), encoding)
      )
    )
  }
}
