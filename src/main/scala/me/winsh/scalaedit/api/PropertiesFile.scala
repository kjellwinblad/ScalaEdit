/*
ScalaEdit - A text editor for Scala programmers
Copyright (C) 2011  Kjell Winblad (kjellwinblad@gmail.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
*/

package me.winsh.scalaedit.api

import java.util.Properties
import java.io.File
import java.io.FileWriter
import java.io.FileReader

abstract class PropertiesFile(val storagePath: File, val comments: String) {

  protected val props = new Properties()
  try {
    props.load(new FileReader(storagePath))
  } catch {
    case _ => //ignore it may be that the file does not exist
  }
  protected def save() {
    val writer = new FileWriter(storagePath)
    props.store(writer, comments)
  }

}