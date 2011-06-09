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
import java.awt.Font
import org.fife.ui.rtextarea._
import org.fife.ui.rsyntaxtextarea._
import java.awt.GraphicsEnvironment

class EditorPanelStandardColorsProperties
  extends PropertiesFile(new File(Utils.propertiesDir, "editor_standard_colors.properties"),
    "Editor Standard Colors Properties") {

  val errorLineColor = new ColorProperty("error_line_color", new Color(1.0f, 0.0f, 0.0f, 0.35f))

  val warningLineColor = new ColorProperty("warning_line_color", new Color(1.0f, 1.0f, 0.0f, 0.9f))

  private val textArea = new RSyntaxTextArea(20, 60)

  val backgroundColor = new ColorProperty("background_color", textArea.getBackground)

  val defaultTextColor = new ColorProperty("foreground_color", textArea.getForeground)

  val currentLineColorEnabled = new BooleanProperty("current_line_color_enabled", textArea.getHighlightCurrentLine)

  val currentLineColor = new ColorProperty("current_line_color", textArea.getCurrentLineHighlightColor)

  val selectionColor = new ColorProperty("selection_color", textArea.getSelectionColor)

  val caretColor = new ColorProperty("caret_color", textArea.getCaretColor)

}