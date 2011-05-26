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

  private val themeID = "theme_class_name"
  def theme: String = props.getProperty(themeID)
  def theme_=(args: String) {
    props.setProperty(themeID, args)
    save()
  }
  if (props.getProperty(themeID) == null)
    theme = UIManager.getLookAndFeel().getClass().getName()

}
