/*
ScalaEdit - A text editor for Scala programmers
Copyright (C) 2011  Kjell Winblad (kjellwinblad@gmail.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
*/

package me.winsh.scalaedit.gui.editor

import scala.swing._
import scala.swing.event._
import me.winsh.scalaedit.api.FileBuffer
import me.winsh.scalaedit.api.CodeNotification
import me.winsh.scalaedit.api.Closeable
import me.winsh.scalaedit.gui._
import me.winsh.scalaedit.gui.project._
import scala.collection.mutable.HashMap
import java.io.File

abstract class EditorsPanel extends TabbedPane {

  val projectPanel: Option[ProjectPanel] = None

  val bufferToEditorMap = HashMap[FileBuffer, EditorPanel]()

  tabLayoutPolicy = TabbedPane.Layout.Scroll

  def shutDownAllOpenResources(keepCurrentOpen: Boolean = false) = {
    val pagesToClose =
      if (keepCurrentOpen) pages.filter(_.content.peer != peer.getSelectedComponent())
      else pages

    val toClose = pagesToClose.filter(_.content.asInstanceOf[Closeable].close())

    toClose.foreach(pages -= _)

    pages.size == 0
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

    //Perform notify about code info with current code notifications
    //to make sure they are shown in the editor
    notifyAboutCodeInfo(currentNotifications)

    editorPanel
  }

  def addLicenseView(name: String, resourcePath: String) {

    val tabComponent = new ButtonTabComponentImpl(this, () => Unit);

    val newEditorPanel = new LicenseView(resourcePath)

    pages += new TabbedPane.Page(name, newEditorPanel)

    peer.setTabComponentAt(pages.size - 1, tabComponent)

    peer.setSelectedComponent(newEditorPanel.peer)

  }

  listenTo(mouse.clicks)

  def currentEditorPanel = pages.find(_.content.peer == peer.getSelectedComponent()) match {
    case Some(page) if (page.content.isInstanceOf[EditorPanel]) =>
      Some(page.content.asInstanceOf[EditorPanel])
    case _ => None
  }

  def editorPanelSelected = currentEditorPanel match {
    case Some(_) => true
    case _ => false
  }

  def openSelectedInProjectBrowser() {
    currentEditorPanel.foreach(_.fileBuffer.file.foreach((f) => projectPanel.foreach(_.selectFile(f))))
  }

  def handleClick(point: Point, triggersPopup: Boolean) {
    val editorPanel = currentEditorPanel.get
    //Display popup
    (new PopupMenu() {
      add(new MenuItem(new Action("Select in Project Browser") {
        icon = Utils.getIcon("/images/small-icons/go-up.png")
        def apply = openSelectedInProjectBrowser()
      }))
      editorPanel.fileBuffer.file.foreach((file) => {
        def nicifyPath(p: String) =
          if (p.size <= 50) p
          else "..." + p.takeRight(47)
        addSeparator()
        add(new MenuItem(new Action("Copy Path: " + nicifyPath(file.getCanonicalPath())) {
          tooltip = file.getCanonicalPath()
          icon = Utils.getIcon("/images/small-icons/copy-to-clipboard.png")
          def apply = SwingHelper.clipboardContents = file.getCanonicalPath()
        }))
      })
      addSeparator()
      add(new MenuItem(new Action("Close All") {
        icon = Utils.getIcon("/images/small-icons/actions/fileclose.png")
        def apply = shutDownAllOpenResources()
      }))
      add(new MenuItem(new Action("Close All Except This") {
        icon = Utils.getIcon("/images/small-icons/actions/fileclose.png")
        def apply = shutDownAllOpenResources(true)
      }))
    }).peer.show(this.peer, point.x, point.y)
  }

  reactions += {
    case MousePressed(_, point, _, _, triggersPopup) if (triggersPopup && editorPanelSelected) => {
      handleClick(point, triggersPopup)
    }
    case MouseReleased(_, point, _, _, triggersPopup) if (triggersPopup && editorPanelSelected) => {
      handleClick(point, triggersPopup)
    }
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

  private var editorsPanel = null: EditorsPanel

  def apply(): EditorsPanel =
    if (editorsPanel == null) {
      editorsPanel = new EditorsPanel() {}
      editorsPanel
    } else editorsPanel

  def apply(p: ProjectPanel): EditorsPanel =
    if (editorsPanel == null) {
      editorsPanel = new EditorsPanel() {
        override val projectPanel = Some(p)
      }
      editorsPanel
    } else editorsPanel

}