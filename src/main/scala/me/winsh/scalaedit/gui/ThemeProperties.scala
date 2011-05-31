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
import javax.swing.UIManager

class ThemeProperties
  extends PropertiesFile(new File(Utils.propertiesDir, "theme.properties"),
    """Theme Properties

This properties file is modified from the user interface.
""") {

  val theme = new StringProperty("theme_class_name", UIManager.getLookAndFeel().getClass().getName())

}