package me.winsh.scalaedit.gui

import scala.swing._


class ConsolesPanel extends TabbedPane {

tabLayoutPolicy = TabbedPane.Layout.Scroll

  def addScalaTerminal() {

    pages += new TabbedPane.Page("Scala Terminal", new ScalaConsolePanel())
     
    peer.setTabComponentAt(pages.size -1, new ButtonTabComponentImpl(this, Utils.getIcon("/images/small-icons/illustrations/scala-terminal.png")))
  }

  addScalaTerminal()

}