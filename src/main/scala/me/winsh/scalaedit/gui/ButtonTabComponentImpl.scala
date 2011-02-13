package me.winsh.scalaedit.gui

import me.winsh.scalaedit.api.Closeable
import scala.swing._
import components.TabsComponent
import components.ButtonTabComponent

  class ButtonTabComponentImpl(val pane:TabbedPane) extends ButtonTabComponent {
     
	override def name(comp: ButtonTabComponent) = {

      val i = pane.peer.indexOfTabComponent(comp)
      
      if(i == -1) "" else pane.peer.getTitleAt(i)

    }

    override def close(comp: ButtonTabComponent) {
      val i = pane.peer.indexOfTabComponent(comp)
      pane.pages(i).content.asInstanceOf[Closeable].close()
      pane.pages.remove(i)
    }
  }