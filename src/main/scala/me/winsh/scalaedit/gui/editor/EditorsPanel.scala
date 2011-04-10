package me.winsh.scalaedit.gui.editor

import scala.swing._
import me.winsh.scalaedit.api.FileBuffer
import me.winsh.scalaedit.api.CodeNotification
import me.winsh.scalaedit.gui._
import scala.collection.mutable.HashMap
import java.io.File

abstract class EditorsPanel extends TabbedPane {

  val bufferToEditorMap = HashMap[FileBuffer, EditorPanel]()
	
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

      val tabComponent = new ButtonTabComponentImpl(this, ()=> bufferToEditorMap -= buffer);

      val newEditorPanel = new EditorPanel(buffer, tabComponent)
      
      bufferToEditorMap += (buffer -> newEditorPanel)
      
      pages += new TabbedPane.Page(buffer.name, newEditorPanel)

      peer.setTabComponentAt(pages.size - 1, tabComponent)

      peer.setSelectedComponent(newEditorPanel.peer)

    }
  }
  
  def notifyAboutCodeInfo(notifications:List[CodeNotification]):Unit = notifications match {
	  case cn::lst => bufferToEditorMap.get(FileBuffer(new File(cn.fileName))) match {
	 	  case None => notifyAboutCodeInfo(lst)
	 	  case Some(editorPanel) => {
	 	 	  editorPanel.notifyAboutCodeInfo(cn)
	 	 	  notifyAboutCodeInfo(lst)
	 	  }
	  }
	  case Nil => Unit
  }
  
    def notifyAboutClearCodeInfo(){
	  for(e <- bufferToEditorMap.values)
	 	  e.notifyAboutClearCodeInfo()
  }

}

object EditorsPanel{
	
	private val editorsPanel = new EditorsPanel(){}
	
	def apply():EditorsPanel = editorsPanel
}