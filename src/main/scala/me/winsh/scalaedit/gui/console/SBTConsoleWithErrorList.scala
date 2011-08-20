/*
ScalaEdit - A text editor for Scala programmers
Copyright (C) 2011  Kjell Winblad (kjellwinblad@gmail.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
*/

package me.winsh.scalaedit.gui.console

import scala.swing._
import me.winsh.scalaedit.api._
import me.winsh.scalaedit.gui._
import me.winsh.scalaedit.gui.editor.EditorsPanel
import scala.swing.ListView.Renderer
import javax.swing.border._
import javax.swing.JPopupMenu
import scala.swing.event._
import java.io.File
import java.awt.Font
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class SBTConsoleWithErrorList(sbtVersion:String="0.7.7") extends ConsolePanel {

  val consoleType = SBTConsole

  private val sbtConsolePanel = new SBTConsolePanel(sbtVersion)

  private val errorList = new ListView[CodeNotification]() {

    renderer = new Renderer[CodeNotification] {
      val cellView = new BoxPanel(Orientation.Vertical) {

        opaque = true;

        private val notificationTypeLabel = new TextField()
        private val lineNumberField = new TextField() {
          editable = false
        }
        private val errorMessageBox = new TextArea() {
          editable = false
          font = new Font(Font.MONOSPACED, Font.PLAIN, font.getSize())
        }
        private val fileNameField = new TextField() {
          editable = false
        }
        contents += new FlowPanel(FlowPanel.Alignment.Left)() {
          opaque = false
          contents += new Label("Type: ")
          contents += notificationTypeLabel
          contents += new Label("Line: ")
          contents += lineNumberField
          contents += new Label("File: ")
          contents += fileNameField
        }

        contents += new FlowPanel(FlowPanel.Alignment.Left)() {
          opaque = false
          contents += new Label("Message: ")
        }
        contents += errorMessageBox

        contents += new Separator()

        def populateWithNotification(n: CodeNotification) {
          val (typeColor, notificationType, fileName, lineNumber, message) = n match {
            case Error(fileName, lineNumber, message) => (java.awt.Color.RED, "error", fileName, lineNumber, message)
            case Warning(fileName, lineNumber, message) => (java.awt.Color.YELLOW, "warning", fileName, lineNumber, message)
          }

          notificationTypeLabel.text = notificationType
          lineNumberField.text = lineNumber.toString
          errorMessageBox.text = message
          errorMessageBox.background = notificationTypeLabel.background
          fileNameField.text = fileName

        }

      }

      override def componentFor(list: scala.swing.ListView[_], isSelected: Boolean, focused: Boolean, a: CodeNotification, index: Int): Component = {

        if (isSelected) {
          cellView.background = selectionBackground
          cellView.foreground = selectionForeground
        } else {
          cellView.background = background
          cellView.foreground = foreground
        }

        cellView.populateWithNotification(a)
        cellView
      }
    }

    listenTo(mouse.clicks)

    reactions += {
      case MouseClicked(_, _, _, _, _) if (peer.getSelectedIndex() >= 0) => {
        //Open upp the file

        val notification = listData(peer.getSelectedIndex())
        val fileName = notification.fileName
        val lineNumber = notification.line
        val editor = EditorsPanel().addFileEditor(FileBuffer(new File(fileName))).editorPane
        editor.setCaretPosition(editor.getLineStartOffset(lineNumber - 1))
      }
    }

    def popupMenuForNotification(notification: CodeNotification): JPopupMenu = {
      (new PopupMenu() {
        add(new MenuItem(new Action("Copy File Path") {
          icon = Utils.getIcon("/images/small-icons/copy-to-clipboard.png")
          def apply() {
            SwingHelper.clipboardContents = notification.fileName
          }
        }))
        add(new MenuItem(new Action("Copy Line Number") {
          icon = Utils.getIcon("/images/small-icons/copy-to-clipboard.png")
          def apply() {
            SwingHelper.clipboardContents = notification.line.toString
          }
        }))
        add(new MenuItem(new Action("Copy Message") {
          icon = Utils.getIcon("/images/small-icons/copy-to-clipboard.png")
          def apply() {
            SwingHelper.clipboardContents = notification.message
          }
        }))
      }).peer
    }

    peer.addMouseListener(new MouseAdapter() {
      override def mouseClicked(me: MouseEvent) {
        // if right mouse button clicked (or me.isPopupTrigger())
        if (javax.swing.SwingUtilities.isRightMouseButton(me)
          && peer.locationToIndex(me.getPoint()) >= 0) {

          val selectedNotification = listData(peer.locationToIndex(me.getPoint()))
          val popupMenu = popupMenuForNotification(selectedNotification)

          popupMenu.show(peer, me.getX(), me.getY());
        }
      }
    })

  }

  private val splitPane = new SplitPane() {

    orientation = Orientation.Vertical

    oneTouchExpandable = true

    leftComponent = sbtConsolePanel

    rightComponent = new ScrollPane(errorList)

  }

  add(splitPane, BorderPanel.Position.Center)

  def close() = {
    sbtConsolePanel.close()
  }

  private var toMatchOn = new StringBuffer()

  private val beginning = """(?s)(.*Compiling.*sources.*)""".r

  private case object FirstErrorLine {

    val regexpLine = """(?s).*error.*0m(.*):(\d*):(.*)\e.*""" //""".*\[.*e.*r.*r.*o.*r.*\]0m(.*):([^:]*):\s*(.*)\n"""

    val le = regexpLine.r

    def unapply(line: String) = {

      try {

        val le(sourceFileName, lineNumber, messagePart1) = line
        Some(sourceFileName, lineNumber.toInt, messagePart1)

      } catch {
        case e => None
      }
    }
  }

  private val errorLine = """(?s).*error.*0m.*0m(.*)\e.*""".r

  case object FirstWarningLine {

    val regexpLine1 = """(?s).*warn.*0m(.*):(\d*):(.*)\e.*"""

    val le = regexpLine1.r

    def unapply(line: String) = {

      try {

        val le(sourceFileName, lineNumber, messagePart1) = line
        Some(sourceFileName, lineNumber.toInt, messagePart1)

      } catch {
        case e => None
      }
    }
  }

  private val warningLine = """(?s).*warn.*0m.*0m(.*)\e.*""".r

  val end = """(?s)(.*==\s*compile\s*==.*)""".r

  var codeNotifications: List[CodeNotification] = Nil

  sbtConsolePanel.addInvokeOnConsoleOutput((in: Int) => {
    toMatchOn.append(in.toChar)

    //See if it matches something biginning, error, warning, end
    if (in == 10) {

      toMatchOn.toString match {
        case beginning(s) => {
          EditorsPanel().notifyAboutClearCodeInfo()
          toMatchOn = new StringBuffer()
        } case FirstErrorLine(fileName, lineNumber, message) => {

          codeNotifications = Error(fileName, lineNumber, message) :: codeNotifications

          toMatchOn = new StringBuffer()
        } case errorLine(messageExtention) if (!messageExtention.contains("error found")) => {

          try {
            val Error(fileName, lineNumber, message) :: rest = codeNotifications
            codeNotifications = Error(fileName, lineNumber, message + "\n " + messageExtention) :: rest
          } catch { case _ => }

          toMatchOn = new StringBuffer()
        } case FirstWarningLine(fileName, lineNumber, message) => {

          codeNotifications = Warning(fileName, lineNumber, message) :: codeNotifications

          toMatchOn = new StringBuffer()
        } case warningLine(messageExtention) if (!messageExtention.contains("warning found")) => {

          try {
            val Warning(fileName, lineNumber, message) :: rest = codeNotifications
            codeNotifications = Warning(fileName, lineNumber, message + "\n " + messageExtention) :: rest
          } catch { case _ => }

          toMatchOn = new StringBuffer()
        } case end(s) => {

          errorList.listData = codeNotifications.reverse
          EditorsPanel().notifyAboutCodeInfo(codeNotifications)

          codeNotifications = Nil
          toMatchOn = new StringBuffer()
        }
        case _ =>
      }

      toMatchOn = new StringBuffer()

    }
  })

}