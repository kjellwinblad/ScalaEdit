/*
ScalaEdit - A text editor for Scala programmers
Copyright (C) 2011  Kjell Winblad (kjellwinblad@gmail.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
*/

package me.winsh.scalaedit.gui

import me.winsh.scalaedit.api._
import java.io.File
import me.winsh.scalaedit.gui._
import java.awt.Color

class EditorPanelProperties
  extends PropertiesFile(new File(Utils.propertiesDir, "editor.properties"),
    """Editor Properties""") {

	val tabLengthInSpaces = new IntProperty("tab_length_in_spaces", 2)

	val wrapLines = new BooleanProperty("wrap_lines", false)

	val errorLineColor = new ColorProperty("error_line_color", new Color(1.0f,0.0f,0.0f,0.4f))

	val warningLineColor = new ColorProperty("warning_line_color", new Color(1.0f,1.0f,0.0f,0.9f))

}