package me.winsh.scalaedit.gui

import me.winsh.scalaedit.api.Closeable
import me.winsh.scalaedit.api.Iconifyable
import scala.swing._
import javax.swing.Icon
import components.ButtonTabComponent

  class ButtonTabComponentImpl(val pane:TabbedPane) extends ButtonTabComponent with Iconifyable{
     
	def this(pane:TabbedPane, icon:Icon)={
		this(pane)
		setIcon(icon)
	}
	 
	
	def icon_=(icon:Icon) = setIcon(icon)
	
	override def name(comp: ButtonTabComponent) = {

      val i = pane.peer.indexOfTabComponent(comp)
      
      if(i == -1) "" else pane.peer.getTitleAt(i)

    }

    override def close(comp: ButtonTabComponent) {
      val i = pane.peer.indexOfTabComponent(comp)
      val closeOk = pane.pages(i).content.asInstanceOf[Closeable].close()
      
      if(closeOk)
    	  pane.pages.remove(i)
    }
  }