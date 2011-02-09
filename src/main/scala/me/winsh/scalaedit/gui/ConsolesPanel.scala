package me.winsh.scalaedit.gui

import scala.swing._
import components.TabsComponent
import components.ButtonTabComponent

class ConsolesPanel extends TabbedPane {

  class ButtonTabComponentImpl extends ButtonTabComponent {
    override def name(comp: ButtonTabComponent) = {

      val i = peer.indexOfTabComponent(comp)
      
      if(i == -1) "" else peer.getTitleAt(i)

    }

    override def close(comp: ButtonTabComponent) {
      val i = peer.indexOfTabComponent(comp)
      pages(i).content.asInstanceOf[ConsolePanel].close()
      pages.remove(i)
    }
  }

  def addScalaTerminal() {

    pages += new TabbedPane.Page("Scala Terminal", new ScalaConsolePanel())
     
    peer.setTabComponentAt(pages.size -1, new ButtonTabComponentImpl())
  }

  addScalaTerminal()

}