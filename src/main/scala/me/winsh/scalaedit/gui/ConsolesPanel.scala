package me.winsh.scalaedit.gui

import scala.swing._

class ConsolesPanel extends TabbedPane{

	def addScalaTerminal() {
		
		pages += new TabbedPane.Page("Scala Terminal", new ScalaConsolePanel())
	}
	
	addScalaTerminal()
	
}