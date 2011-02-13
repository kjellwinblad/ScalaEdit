package me.winsh.scalaedit.api
import java.io.File
import scala.io.Source

class FileBuffer(var file: Option[File]) {

  val name = file match {
    case None => "New File"
    case Some(file) => file.getName
  }

  def content = file match {
    case None => ""
    case Some(f) => {
      val src = Source.fromFile(f)
      val cont = src.getLines.mkString("\n")
      src.close()
      cont
    }
  }

  def contentType = file match {
    case None => "text/plain"
    case Some(f) => {
      f.getName.split("\\.").toList match {
        case Nil => "text/plain"
        case x :: Nil => "text/plain"
        case x => "text/" + x.last
      }
    }
  }

  def content_=(newContent: String) = file match {
    case None => throw new Exception("Content can not be saved to this file buffer since it is not connected to a file in the file system")
    case Some(f) => {
      val out = new java.io.FileWriter(f)
      out.write(newContent)
      out.close
    }
  }

}