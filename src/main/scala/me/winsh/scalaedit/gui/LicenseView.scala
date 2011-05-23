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
import scala.swing._
import scala.io.Source
import java.io.InputStream
import java.awt.Font
import javax.swing.text.DefaultCaret

class LicenseView(resourcePath: String) extends ScrollPane with Closeable {

  contents = new TextArea() {

    private val car = peer.getCaret().asInstanceOf[DefaultCaret]

    car.setUpdatePolicy(DefaultCaret.NEVER_UPDATE)

    editable = false

    peer.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12))

    private val licenseTextStream = this.getClass.getResourceAsStream(resourcePath)

    text = Source.fromInputStream(licenseTextStream).getLines().mkString("\n")

  }

  def close() = true

}