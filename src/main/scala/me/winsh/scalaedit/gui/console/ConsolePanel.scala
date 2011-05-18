/*
ScalaEdit - A text editor for Scala programmers
Copyright (C) 2011  Kjell Winblad (kjellwinblad@gmail.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
*/

package me.winsh.scalaedit.gui.console

import scala.swing._
import me.winsh.scalaedit.api.Closeable

abstract class ConsolePanel extends BorderPanel with Closeable{

  abstract class ConsoleType

  case object ScalaConsole extends ConsoleType

  case object StandAloneScalaConsole extends ConsoleType
  
  case object SBTConsole extends ConsoleType

  case object SSHConsole extends ConsoleType

  val consoleType: ConsoleType

	preferredSize = new Dimension(700,200)

}