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
import java.io.FileInputStream
import scala.actors.Actor._

object Main {

  //Initialize properties dir
  val propDir = Utils.propertiesDir
  propDir.mkdir()
  //Let the operating system buffer properties files in the background
  actor{
    propDir.listFiles().filter((f)=>f.isFile &&f.getName.endsWith(".properties")).foreach((file)=>{
      val is = new FileInputStream(file)
      while(is.read() != -1)None;
      is.close()
    })
  }

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