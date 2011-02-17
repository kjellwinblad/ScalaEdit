package me.winsh.scalaedit.gui

import scala.swing._
import me.winsh.scalaedit.api.FileBuffer

class EditorsPanel extends TabbedPane {

  def addFileEditor(buffer: FileBuffer) {

	val tabComponent = new ButtonTabComponentImpl(this);
	  
    pages += new TabbedPane.Page(buffer.name, new EditorPanel(buffer, tabComponent))

    peer.setTabComponentAt(pages.size - 1, tabComponent)

  }

}