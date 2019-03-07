package org.conglomerate.kafka.utils

import java.io.BufferedReader

class BufferedReaderIterator(reader: BufferedReader) extends Iterator[String] {

  override def hasNext: Boolean = reader.ready()
  override def next: String = reader.readLine()
}
