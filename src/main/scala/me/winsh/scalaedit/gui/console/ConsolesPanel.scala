package me.winsh.scalaedit.gui.console

import scala.swing._
import me.winsh.scalaedit.gui._
import me.winsh.scalaedit.api.Closeable

class ConsolesPanel extends TabbedPane {

tabLayoutPolicy = TabbedPane.Layout.Scroll

  def addScalaTerminal() {
		addTerminal("Scala Terminal", new StandAloneScalaConsolePanel(), "/images/small-icons/illustrations/scala-terminal.png")
  }

  def addSBTTerminal() {
		addTerminal("SBT Terminal", new SBTConsoleWithErrorList(), "/images/small-icons/illustrations/sbt-terminal.png")
  }

  def addTerminal(name:String, component:ConsolePanel, iconPath:String) {

    pages += new TabbedPane.Page(name, component)
     
    peer.setTabComponentAt(pages.size -1, new ButtonTabComponentImpl(this, Utils.getIcon(iconPath)))
  
  	peer.setSelectedComponent(component.peer)
  	
  }
  
  def shutDownAllOpenResources(){
	  pages.foreach((page)=>{
	 	  page.content.asInstanceOf[Closeable].close()
	  })
  }

  addScalaTerminal()

}