/*
ScalaEdit - A text editor for Scala programmers
Copyright (C) 2011  Kjell Winblad (kjellwinblad@gmail.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
*/

package me.winsh.scalaedit.api
import java.io.File
import scala.io.Source

abstract class FileBuffer(var file: Option[File]) {

  val name = file match {
    case None => "New File"
    case Some(file) => file.getName
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

  def content = file match {
    case None => ""
    case Some(f) => {
      var src: Source = null
      try {

        src = Source.fromFile(f)
        val cont = src.getLines.mkString("\n")
        cont

      } finally { src.close() }
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

  override def equals(that: Any) = {
    if (!that.isInstanceOf[FileBuffer])
      false
    else {
      val f = that.asInstanceOf[FileBuffer]

      (f.file, this.file) match {
        case (Some(f1), Some(f2)) if (f1.getCanonicalPath == f2.getCanonicalPath) => true
        case _ => false
      }

    }
  }

  override def hashCode() = {
    this.file match {
      case None => None.hashCode
      case Some(f) => f.getCanonicalPath.hashCode
    }
  }

  override def toString = file.toString

}

object FileBuffer {

  class FileBufferImpl(file: Option[File]) extends FileBuffer(file)

  def apply(): FileBuffer = new FileBufferImpl(None)

  def apply(file: File): FileBuffer = new FileBufferImpl(Some(file))
}