package org.conglomerate.kafka.utils


import java.io.File

object FilesIterator extends FileContentIterator {
  def apply(file: File, encoding: String = "UTF-8"): Iterator[String] = new FilesIterator(file, encoding)
}

class FilesIterator(file: File, encoding: String) extends Iterator[String] {

  require(file.exists())

  val Empty: Iterator[String] = Iterator.empty

  def process(file: File): Option[Iterator[String]] = {
    file.getName match {
      case name if name.endsWith("csv.gz") => Some(GzFileIterator(file, encoding))
      case name if name.endsWith("csv.zip") => Some(ZipFileIterator(file, encoding))
      case name if name.endsWith("csv") => Some(TextFileIterator(file, encoding))
      case _ => None
    }
  }

  private val iterator: Iterator[String] = if (file.isDirectory) file.listFiles.flatMap(process).reduce(_ ++ _) else
    process(file).getOrElse(Empty)

  override def hasNext: Boolean = iterator.hasNext

  override def next: String = iterator.next
}

