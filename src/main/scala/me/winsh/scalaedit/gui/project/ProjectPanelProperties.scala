/*
ScalaEdit - A text editor for Scala programmers
Copyright (C) 2011  Kjell Winblad (kjellwinblad@gmail.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
*/

package me.winsh.scalaedit.gui.project

import me.winsh.scalaedit.api._
import java.io.File
import me.winsh.scalaedit.gui.Utils

class ProjectPanelProperties
  extends PropertiesFile(new File(Utils.propertiesDir, "project_panel.properties"),
    "Project Panel Properties") {

  val autoRefreshEnabled = new BooleanProperty("auto_refresh_enabled", false)

  val autoRefreshDelayInSeconds = new IntProperty("auto_refresh_delay_in_seconds", 5)

  val textSize = new IntProperty("text_size", 12)

}
