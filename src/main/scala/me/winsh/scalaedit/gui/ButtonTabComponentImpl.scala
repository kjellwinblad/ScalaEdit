/*
ScalaEdit - A text editor for Scala programmers
Copyright (C) 2011  Kjell Winblad (kjellwinblad@gmail.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
*/

package me.winsh.scalaedit.gui

import me.winsh.scalaedit.api.Closeable
import scala.swing._
import javax.swing.Icon
import components.ButtonTabComponent
import me.winsh.scalaedit.api.TabComponent

class ButtonTabComponentImpl(val pane: TabbedPane, val closeHandler: () => Unit = (() => {})) extends ButtonTabComponent with TabComponent {

  def this(pane: TabbedPane, icon: Icon) = {
    this(pane)
    setIcon(icon)
  }

  def icon_=(icon: Icon) = setIcon(icon)

  def icon = getIcon()

  def name_=(newName: String) = {
    setName(newName)
    val i = pane.peer.indexOfTabComponent(this)
    pane.peer.setTitleAt(i, newName)

  }

  def name = getName()

  override def name(comp: ButtonTabComponent) = {

    val i = pane.peer.indexOfTabComponent(comp)

    if (i == -1) "" else pane.peer.getTitleAt(i)

  }

  override def close(comp: ButtonTabComponent) {
    val i = pane.peer.indexOfTabComponent(comp)
    val closeOk = pane.pages(i).content.asInstanceOf[Closeable].close()

    if (closeOk) {
      pane.pages.remove(i)
      closeHandler()
    }
  }
}