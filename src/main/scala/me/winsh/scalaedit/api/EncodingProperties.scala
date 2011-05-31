/*
ScalaEdit - A text editor for Scala programmers
Copyright (C) 2011  Kjell Winblad (kjellwinblad@gmail.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
*/

package me.winsh.scalaedit.api

import java.nio.charset.Charset
import scala.collection.JavaConversions._
import java.io.File
import me.winsh.scalaedit.gui.Utils

class EncodingProperties
  extends PropertiesFile(new File(Utils.propertiesDir, "encoding.properties"),
    """Encoding Properties

Available encodings: %s

The default encoding for the JVM is %s
    """.format(Charset.availableCharsets.keySet.toList.mkString(", "), Charset.defaultCharset.name)) {

  val defaultEncoding = new StringProperty("default_encoding", "UTF-8")

  def encoding: String = {
    val enc = defaultEncoding.get.trim
    if (!Charset.availableCharsets.keySet.contains(defaultEncoding.get)) {
      throw new Exception("The encoding (%s) configured in %s does not exist. \nSelect a valid encoding.".format(enc, storagePath.getPath))
    }
    enc
  }

}