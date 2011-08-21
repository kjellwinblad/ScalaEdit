/*
ScalaEdit - A text editor for Scala programmers
Copyright (C) 2011  Kjell Winblad (kjellwinblad@gmail.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
*/

package me.winsh.scalaedit.gui.console

import scala.swing._
import me.winsh.scalaedit.gui._
import me.winsh.scalaedit.api.Closeable

class ConsolesPanel extends TabbedPane {

  tabLayoutPolicy = TabbedPane.Layout.Scroll

  def addScalaTerminal() {
    addTerminal("Scala Terminal", new StandAloneScalaConsolePanel(), "/images/small-icons/illustrations/scala-terminal.png")
  }

  def addSBTTerminal(version: String) {
    addTerminal("SBT Terminal", new SBTConsoleWithErrorList(version), "/images/small-icons/illustrations/sbt-terminal.png")
  }

  def addTerminal(name: String, component: ConsolePanel, iconPath: String) {

    pages += new TabbedPane.Page(name, component)

    peer.setTabComponentAt(pages.size - 1, new ButtonTabComponentImpl(this, Utils.getIcon(iconPath)))

    peer.setSelectedComponent(component.peer)

  }

  def shutDownAllOpenResources() {
    pages.foreach((page) => {
      page.content.asInstanceOf[Closeable].close()
    })
  }

  def currentConsolePanel = pages.find(_.content.peer == peer.getSelectedComponent()) match {
    case Some(page) if (page.content.isInstanceOf[ConsolePanel]) =>
      Some(page.content.asInstanceOf[ConsolePanel])
    case _ => None
  }

  def requestFocusForTopComponent() = currentConsolePanel match {
    case None => requestFocusInWindow()
    case Some(panel) => panel.requestFocusForTerminal
  }

  def requestFocusForToolComponent() {
    currentConsolePanel match {
      case None => ; //Do nothing
      case Some(panel) =>
        try {
          panel.asInstanceOf[SBTConsoleWithErrorList].toolBar.requestFocusInWindow()
        } catch { case _ => ; /*Do nothing*/ }
    }
  }

  def executeRunOnTopComponentIfPossible() {
    currentConsolePanel match {
      case None => ; //Do nothing
      case Some(panel) =>
        try {
          panel.asInstanceOf[SBTConsoleWithErrorList].run()
        } catch { case _ => ; /*Do nothing*/ }
    }
  }

}