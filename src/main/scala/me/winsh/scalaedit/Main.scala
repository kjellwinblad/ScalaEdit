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
import me.winsh.scalaedit.gui.ThemeProperties
import scala.swing._
import java.awt.Toolkit
import javax.swing.UIManager
import java.awt.Point
import java.awt.SplashScreen
import java.awt.event._
import java.io.File

object Main {

  //Initialize properties dir
  Utils.propertiesDir.mkdir()

  private val splash = SplashScreen.getSplashScreen()
  if (splash != null) {
    splash.setImageURL(this.getClass.getResource("/images/img3.png"))
    splash.update()
  }

  def main(args: Array[String]) = {

    if (splash != null)
      splash.close()

    Utils.swingInvokeLater(() => {

      //val themeProperties = new ThemeProperties()

      //UIManager.setLookAndFeel(themeProperties.theme)

      val window = new MainWindow() {
        override def closeOperation: Unit = Utils.swingInvokeAndWait(() => {
          if (shutDownOpenResources()) {
            visible = false
            dispose()
            Thread.sleep(800)
            sys.exit()
          }
        })
      }

      window.visible = true

    })

  }

}