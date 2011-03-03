package me.winsh.scalaedit.gui

import scala.swing._
import me.winsh.scalaedit.api.FileBuffer

class EditorsPanel extends TabbedPane {

  tabLayoutPolicy = TabbedPane.Layout.Scroll

  def isBufferInPanel(buffer: FileBuffer) = pages.exists((e) => {
    if (e.content.isInstanceOf[EditorPanel]) {
      e.content.asInstanceOf[EditorPanel].fileBuffer == buffer
    } else false
  })

  def openExistingBuffer(buffer: FileBuffer) = {

    pages.find((e) => {
      if (e.content.isInstanceOf[EditorPanel]) {
        e.content.asInstanceOf[EditorPanel].fileBuffer == buffer
      } else false
    }) match {
      case Some(page) => peer.setSelectedComponent(page.content.peer)
      case None => Unit
    }

  }

  def addFileEditor(buffer: FileBuffer) {

    //If the file buffer already exists in the editor then just open it

    if (isBufferInPanel(buffer))
      openExistingBuffer(buffer)
    else {

      val tabComponent = new ButtonTabComponentImpl(this);

      val newEditorPanel = new EditorPanel(buffer, tabComponent)
      
      pages += new TabbedPane.Page(buffer.name, newEditorPanel)

      peer.setTabComponentAt(pages.size - 1, tabComponent)

      peer.setSelectedComponent(newEditorPanel.peer)

      
    }
  }

}