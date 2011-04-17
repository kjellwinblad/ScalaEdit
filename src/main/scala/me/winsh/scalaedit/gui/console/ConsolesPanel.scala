package me.winsh.scalaedit.gui.console

import scala.swing._
import me.winsh.scalaedit.gui._

class ConsolesPanel extends TabbedPane {

tabLayoutPolicy = TabbedPane.Layout.Scroll

  def addScalaTerminal() {

    pages += new TabbedPane.Page("Scala Terminal", new ScalaConsolePanel())
     
    peer.setTabComponentAt(pages.size -1, new ButtonTabComponentImpl(this, Utils.getIcon("/images/small-icons/illustrations/scala-terminal.png")))
  }

  def addSBTTerminal() {

    pages += new TabbedPane.Page("SBT Terminal", new SBTConsoleWithErrorList())
     
    peer.setTabComponentAt(pages.size -1, new ButtonTabComponentImpl(this, Utils.getIcon("/images/small-icons/illustrations/sbt-terminal.png")))
  }

  addScalaTerminal()

}