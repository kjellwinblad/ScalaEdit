/*
ScalaEdit - A text editor for Scala programmers
Copyright (C) 2011  Kjell Winblad (kjellwinblad@gmail.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
*/

package me.winsh.scalaedit.api

sealed abstract class CodeNotification(val fileName: String, val line: Int, val message: String)

case class Error(override val fileName: String, override val line: Int, override val message: String) extends CodeNotification(fileName, line, message)

case class Warning(override val fileName: String, override val line: Int, override val message: String) extends CodeNotification(fileName, line, message)