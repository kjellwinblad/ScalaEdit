/*
ScalaEdit - A text editor for Scala programmers
Copyright (C) 2011  Kjell Winblad (kjellwinblad@gmail.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
*/

package me.winsh.scalaedit.gui.console

import me.winsh.scalaedit.api._
import java.io.File
import me.winsh.scalaedit.gui._

class SBTConsolePanelProperties extends StandAloneConsoleProperties(
  new File(Utils.propertiesDir, "sbt_console.properties"),
  "SBT Console Properties", "") {

	override val javaVMArguments = {

		val defaultArguments =
			"-Xmx512M" +
		    (if (System.getProperty("os.name").toLowerCase.contains("windows"))
		      " -Djline.terminal=jline.UnsupportedTerminal"
		    else
		      "")
		
		new StringProperty("java_vm_arguments", defaultArguments)
	}

}