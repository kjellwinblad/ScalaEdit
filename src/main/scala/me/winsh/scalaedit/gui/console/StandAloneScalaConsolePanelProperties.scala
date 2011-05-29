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

class StandAloneScalaConsolePanelProperties extends StandAloneConsoleProperties(
  new File(Utils.propertiesDir, "scala_console.properties"),
  "Scala Console Properties",
  """The java_classpath variable can contain dirs and jar files.
Use spaces to separate classpath elements.
""") {
  override val javaVMArguments = {
  	val defaultValue = 
	  	"-Xmx256M -Xms32M" +
	    (if (System.getProperty("os.name").toLowerCase.contains("windows"))
	      " -Djline.terminal=NONE"
	    else
	      "")
		new StringProperty("java_vm_arguments", defaultValue)
  }

	val javaClasspath = new StringProperty("java_classpath")

}