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
Use ":" to separate classpath elements.
""") {
  if (javaVMArguments == null) javaVMArguments = "-Xmx256M -Xms32M" +
    (if (System.getProperty("os.name").toLowerCase.contains("windows"))
      " -Djline.terminal=NONE"
    else
      "")

  protected val javaClasspathID = "java_classpath"
  def javaClasspath: String = props.getProperty(javaClasspathID)
  def javaClasspath_=(args: String) {
    props.setProperty(javaClasspathID, args)
    save()
  }
  if (javaClasspath == null) javaClasspath = ""
}