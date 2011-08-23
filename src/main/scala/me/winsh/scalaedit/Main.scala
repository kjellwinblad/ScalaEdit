/*
ScalaEdit - A text editor for Scala programmers
Copyright (C) 2011  Kjell Winblad (kjellwinblad@gmail.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
*/

package me.winsh.scalaedit

import me.winsh.scalaedit.gui.MainWindow
import me.winsh.scalaedit.gui.Utils
import me.winsh.scalaedit.gui.SwingHelper
import scala.swing._
import java.awt.Toolkit
import javax.swing.UIManager
import java.awt.Point
import java.awt.event._
import java.io.File

object Main {

  //Initialize properties dir
  Utils.propertiesDir.mkdir()

  def main(args: Array[String]) = {

    SwingHelper.invokeLater(() => {

      val window = new MainWindow() {
        override def closeOperation: Unit = SwingHelper.invokeAndWait(() => {
          if (shutDownOpenResources()) {
            visible = false
            dispose()
            Thread.sleep(1200)
            sys.exit()
          }
        })
      }

      window.visible = true

    })

  }

}