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

abstract class StandAloneConsoleProperties(override val storagePath: File, title: String, comments: String)
  extends PropertiesFile(storagePath,
    title +
      """
 
The starting_dir property is the dir that the process will run in.
If starting_dir is set to CURRENT_PROJECT_DIR then the current project directory will be used.
The value of the arguments propery will be sent to the process. It is optional.
""" +
      comments) {

  val startingDir = {
    val defaultCurrentProjectDir = "CURRENT_PROJECT_DIR"
    val startingDirStringProp = new StringProperty("starting_dir", defaultCurrentProjectDir)

    new Property[File]("", new File(".")) {
      def get =
        if (startingDirStringProp.get.trim == defaultCurrentProjectDir) Utils.projectDir
        else new File(startingDirStringProp.get)

      def set(value: File) {
        startingDirStringProp.set(value.getCanonicalPath)
      }
    }
  }

  val arguments = new StringProperty("arguments")

  val echoInput = new BooleanProperty("echo_input",
    System.getProperty("os.name").toLowerCase.contains("windows"))
}