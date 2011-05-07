package me.winsh.scalaedit.gui.editor

import scala.swing._
import me.winsh.scalaedit.api.FileBuffer
import me.winsh.scalaedit.api.CodeNotification
import me.winsh.scalaedit.api.Closeable
import me.winsh.scalaedit.gui._
import scala.collection.mutable.HashMap
import java.io.File

abstract class EditorsPanel extends TabbedPane {

  val bufferToEditorMap = HashMap[FileBuffer, EditorPanel]()

  tabLayoutPolicy = TabbedPane.Layout.Scroll

  def shutDownAllOpenResources() {
    pages.foreach((page) => {
      page.content.asInstanceOf[Closeable].close()
    })
  }

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
      case Some(page) => {
        peer.setSelectedComponent(page.content.peer)
        Some(page.content.asInstanceOf[EditorPanel])
      }
      case None => None
    }

  }

  def addFileEditor(buffer: FileBuffer): EditorPanel = {

    //If the file buffer already exists in the editor then just open it

    val editorPanel = openExistingBuffer(buffer).getOrElse {

      val tabComponent = new ButtonTabComponentImpl(this, () => bufferToEditorMap -= buffer);

      val newEditorPanel = new EditorPanel(buffer, tabComponent)

      bufferToEditorMap += (buffer -> newEditorPanel)

      pages += new TabbedPane.Page(buffer.name, newEditorPanel)

      peer.setTabComponentAt(pages.size - 1, tabComponent)

      peer.setSelectedComponent(newEditorPanel.peer)

      newEditorPanel

    }

    //Perform notify about code info with current code notifications to make sure they are shown in the editor
    notifyAboutCodeInfo(currentNotifications)

    editorPanel
  }

  def addLicenseView(name:String, resourcePath:String) {

      val tabComponent = new ButtonTabComponentImpl(this, () =>Unit);

      val newEditorPanel = new LicenseView(resourcePath)

      pages += new TabbedPane.Page(name, newEditorPanel)

      peer.setTabComponentAt(pages.size - 1, tabComponent)

      peer.setSelectedComponent(newEditorPanel.peer)

  }

  private var currentNotifications: List[CodeNotification] = Nil

  def notifyAboutCodeInfo(notifications: List[CodeNotification]): Unit = {

    currentNotifications = notifications

    def notifyAboutCodeInfoWithoutSavingInfos(notifications: List[CodeNotification]): Unit =
      notifications match {
        case cn :: lst =>
          bufferToEditorMap.get(FileBuffer(new File(cn.fileName))) match {
            case None => notifyAboutCodeInfoWithoutSavingInfos(lst)
            case Some(editorPanel) => {
              editorPanel.notifyAboutCodeInfo(cn)
              notifyAboutCodeInfoWithoutSavingInfos(lst)
            }
          }
        case Nil => Unit
      }

    notifyAboutCodeInfoWithoutSavingInfos(notifications)
  }

  def notifyAboutClearCodeInfo() {
    for (e <- bufferToEditorMap.values)
      e.notifyAboutClearCodeInfo()
  }

}

object EditorsPanel {

  private val editorsPanel = new EditorsPanel() {}

  def apply(): EditorsPanel = editorsPanel
}