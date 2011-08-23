/*
ScalaEdit - A text editor for Scala programmers
Copyright (C) 2011  Kjell Winblad (kjellwinblad@gmail.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
*/

package me.winsh.scalaedit.gui

import java.awt.event.KeyEvent
import java.awt.event.InputEvent
import javax.swing.KeyStroke
import javax.swing.WindowConstants
import me.winsh.scalaedit.api._
import scala.swing._
import javax.swing.JSplitPane._
import javax.swing.ImageIcon
import java.io.File
import javax.swing.Box
import javax.swing.JOptionPane
import me.winsh.scalaedit.gui.editor._
import me.winsh.scalaedit.gui.project._
import me.winsh.scalaedit.gui.console._
import scala.swing.event._
import scala.io.Source
import javax.swing.event.MenuListener
import javax.swing.event.MenuEvent
import javax.swing.LookAndFeel
import javax.swing.SwingUtilities
import javax.swing.UIManager
import java.awt.Toolkit
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.HierarchyEvent
import java.awt.event.HierarchyListener
import java.awt.EventQueue
import java.awt.AWTEvent

class MainWindow extends Frame {

  val version = "0.3.3"

  title = "ScalaEdit (" + version + ")"

  iconImage = Utils.getImage("/images/img3.png")

  val consolesPanel = new ConsolesPanel()

  val projectPanel = new ProjectPanel((f: File) => editorsPanel.addFileEditor(FileBuffer(f)))

  val editorsPanel: EditorsPanel = EditorsPanel(projectPanel)

  peer.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)

  def shutDownOpenResources() =
    if (editorsPanel.shutDownAllOpenResources()) {
      consolesPanel.shutDownAllOpenResources()
      true
    } else false

  menuBar = new MenuBar() {

    val fileMenu = new Menu("File") {

      mnemonic = Key.F

      contents += new MenuItem(new Action("New") {

        accelerator = Some(KeyStroke.getKeyStroke('N', InputEvent.CTRL_DOWN_MASK))

        icon = Utils.getIcon("/images/small-icons/actions/filenew.png")

        def apply() {
          editorsPanel.addFileEditor(FileBuffer())
        }

      })

      contents += new MenuItem(new Action("Open...") {

        accelerator = Some(KeyStroke.getKeyStroke('O', InputEvent.CTRL_DOWN_MASK))

        icon = Utils.getIcon("/images/small-icons/actions/fileopen.png")

        def apply() {
          val chooser = new FileChooser(Utils.bestFileChooserDir) {
            title = "Open File(s)"
            multiSelectionEnabled = true
            fileSelectionMode = FileChooser.SelectionMode.FilesOnly
          }

          chooser.showOpenDialog(null)

          chooser.selectedFiles.foreach(file => {
            editorsPanel.addFileEditor(FileBuffer(file))
          })

        }

      })

      contents += new Separator()

      contents += new MenuItem(new Action("Exit") {

        icon = Utils.getIcon("/images/small-icons/actions/exit.png")

        def apply() {
          closeOperation()
        }

      })
    }

    val projectMenu = new Menu("Project") {

      mnemonic = Key.P

      peer.addMenuListener(new MenuListener() {
        def menuCanceled(e: MenuEvent) {}
        def menuDeselected(e: MenuEvent) {}
        def menuSelected(e: MenuEvent) {
          prepareMenu()
        }
      })

      def prepareMenu() {
        contents.clear()
        projectPanel.projectMenuItemsAndLatestRoots.foreach(contents += _)
      }

    }

    val terminalMenu = new Menu("Terminal") {

      mnemonic = Key.T

      contents += new MenuItem(new Action("New Scala Terminal") {

        icon = Utils.getIcon("/images/small-icons/illustrations/scala-terminal.png")

        def apply() {
          consolesPanel.addScalaTerminal()
        }

      })

      contents += new Menu("SBT Terminal") {

        icon = Utils.getIcon("/images/small-icons/illustrations/sbt-terminal.png")

        contents += new MenuItem(new Action("Version 0.7.7") {

          def apply() {
            consolesPanel.addSBTTerminal("0.7.7")
          }

        })

        contents += new MenuItem(new Action("Version 0.10.1") {

          def apply() {
            consolesPanel.addSBTTerminal("0.10.1")
          }

        })
      }

    }

    val propertiesMenu = new Menu("Properties") {

      contents += new Menu("Editor") {
        contents += new MenuItem(Action("General...") {
          val props = new EditorPanelProperties()
          editorsPanel.addFileEditor(FileBuffer(props.storagePath))
        })
        contents += new MenuItem(Action("Colors...") {
          val props = new EditorPanelStandardColorsProperties()
          editorsPanel.addFileEditor(FileBuffer(props.storagePath))
        })
        contents += new MenuItem(Action("Encoding...") {
          val props = new EncodingProperties()
          editorsPanel.addFileEditor(FileBuffer(props.storagePath))
        })
        contents += new MenuItem(Action("Syntax Highlighting...") {
          val props = new SyntaxHighlightingProperties()
          editorsPanel.addFileEditor(FileBuffer(props.storagePath))
        })
      }
      contents += new MenuItem(Action("Project Panel...") {
        val props = new ProjectPanelProperties()
        editorsPanel.addFileEditor(FileBuffer(props.storagePath))
      })
      contents += new Menu("Terminal") {
        contents += new MenuItem(new Action("SBT Terminal...") {

          icon = Utils.getIcon("/images/small-icons/illustrations/sbt-terminal.png")

          def apply() {
            val props = new SBTConsolePanelProperties()
            editorsPanel.addFileEditor(FileBuffer(props.storagePath))
          }

        })
        contents += new MenuItem(new Action("Scala Terminal...") {

          icon = Utils.getIcon("/images/small-icons/illustrations/scala-terminal.png")

          def apply() {
            val props = new StandAloneScalaConsolePanelProperties()
            editorsPanel.addFileEditor(FileBuffer(props.storagePath))
          }

        })
      }
    }

    val helpMenu = new Menu("Help") {

      mnemonic = Key.H

      contents += new MenuItem(Action("About...") {

        val text =
          <html>
            <h1>ScalaEdit</h1>
            <p>
              <table>
                <tr><td><i>Version:</i></td><td>{ version }</td></tr>
                <tr><td><i>Homepage:</i></td><td>http://scala-edit.googlecode.com</td></tr>
                <tr><td><i>Source code:</i></td><td>http://github.com/kjellwinblad/ScalaEdit</td></tr>
                <tr><td><i>Contact:</i></td><td>kjellwinblad@gmail.com</td></tr>
              </table>
            </p>
          </html>.toString.split("\n").foldLeft("")((a, b) => a + b)

        val options = Array[Object]("OK");
        JOptionPane.showOptionDialog(null, text, "About",
          JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
          Utils.getIcon("/images/img3.png"), options, options(0))

      })

      contents += new Menu("Licenses") {
        contents += new MenuItem(Action("ScalaEdit") {
          EditorsPanel().addLicenseView("ScalaEdit License", "/licenses/scala_edit_license")
        })
        contents += new Separator()
        contents += new MenuItem(Action("RSyntaxTextArea") {
          EditorsPanel().addLicenseView("RSyntaxTextArea License", "/licenses/rsyntaxtextarea_license")
        })
        contents += new MenuItem(Action("Scala") {
          EditorsPanel().addLicenseView("Scala License", "/licenses/scala_licence")
        })
        contents += new MenuItem(Action("sbt (Simple Build Tool)") {
          EditorsPanel().addLicenseView("sbt License", "/licenses/sbt_license")
        })
        contents += new MenuItem(Action("JLine") {
          EditorsPanel().addLicenseView("JLine License", "/licenses/jline_license")
        })
        contents += new MenuItem(Action("Apache log4j") {
          EditorsPanel().addLicenseView("Apache log4j License", "/licenses/log4j_license")
        })
        contents += new MenuItem(Action("JTA - Telnet/SSH for Java") {
          EditorsPanel().addLicenseView("JTA License", "/licenses/jta_license")
        })
      }

      contents += new Separator()

      contents += new MenuItem(Action("Global Shortcuts...") {

        val text =
          <html>
            <h1>Global Shortcuts</h1>
            <p>The following global shortcuts can be used in the application:</p>
            <p>
              <table>
                <tr><td><i><b>Key</b></i></td><td><i><b>Function</b></i></td></tr>
                <tr><td><b>F1</b></td><td>is reserved for future use.</td></tr>
                <tr><td><b>F2</b></td><td>selects the project browser so folders can be opened with the<br/>keyboard.</td></tr>
                <tr><td><b>F3</b></td><td>selects the currently selected text editor so text can be entered.</td></tr>
                <tr><td><b>F4</b></td><td>selects the currently selected terminal so commands can be typed.</td></tr>
                <tr><td><b>F5</b></td><td>selects the editor tab panel so the editor can be changed with<br/>the arrow keys.</td></tr>
                <tr><td><b>F6</b></td><td>selects the terminal tab panel so the terminal can be changed<br/>with the arrow keys.</td></tr>
                <tr><td><b>F7</b></td><td>
                                        selects the tools panel in the currently opened SBT terminal.<br/>
                                        This only works if an SBT panel is opened and selected.
                                      </td></tr>
                <tr><td><b>F8</b></td><td>
                                        executes the SBT run command in the currently opened SBT terminal.<br/>
                                        This only works if an SBT panel is opened and selected.
                                      </td></tr>
              </table>
            </p>
          </html>.toString.split("\n").foldLeft("")((a, b) => a + b)

        val options = Array[Object]("OK");
        JOptionPane.showOptionDialog(null, text, "ScalaEdit Global Shortcuts",
          JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
          null, options, options(0))

      })

      contents += new Separator()

      contents += new MenuItem(Action("""<html><font color="BLUE"><u>Scala API</u></font>""") {
        Utils.openURLInBrowser("http://www.scala-lang.org/api/current/index.html")
      })

      contents += new MenuItem(Action("""<html><font color="BLUE"><u>Java API</u></font>""") {
        Utils.openURLInBrowser("http://download.oracle.com/javase/6/docs/api/")
      })

      contents += new MenuItem(Action("""<html><font color="BLUE"><u>sbt 0.7.7 documentation</u></font>""") {
        Utils.openURLInBrowser("http://code.google.com/p/simple-build-tool/")
      })

      contents += new MenuItem(Action("""<html><font color="BLUE"><u>sbt 0.10.1 documentation</u></font>""") {
        Utils.openURLInBrowser("http://github.com/harrah/xsbt/wiki")
      })

      contents += new MenuItem(Action("""<html><font color="BLUE"><u>ScalaEdit Web Site</u></font>""") {
        Utils.openURLInBrowser("http://code.google.com/p/scala-edit/")
      })

    }

    contents += fileMenu

    contents += projectMenu

    contents += terminalMenu

    contents += propertiesMenu

    contents += Swing.HGlue

    contents += helpMenu

  }

  private val mainSplitPane = new SplitPane() {
    orientation = Orientation.Horizontal
    resizeWeight = 1.0

    val editorProjectSplitPane = new SplitPane() {
      resizeWeight = 0.0
      orientation = Orientation.Vertical
      leftComponent = projectPanel
      rightComponent = editorsPanel
    }

    editorProjectSplitPane.oneTouchExpandable = true

    leftComponent = editorProjectSplitPane
    rightComponent = consolesPanel

  }
  mainSplitPane.oneTouchExpandable = true

  preferredSize = new Dimension(800, 700)

  size = new Dimension(800, 700)

  contents = mainSplitPane

  pack()

  maximize()

  SwingHelper.setDividerLocation(mainSplitPane, 0.72)

  SwingHelper.setDividerLocation(mainSplitPane.editorProjectSplitPane, 0.28)

  //Listen to global shortcuts:
  val ev = Toolkit.getDefaultToolkit().getSystemEventQueue()
  ev.push(new EventQueue() {
    override protected def dispatchEvent(event: AWTEvent) {
      event match {
        case key: KeyEvent if (key.getKeyCode() == KeyEvent.VK_F2) =>
          projectPanel.requestFocusInWindow()
        case key: KeyEvent if (key.getKeyCode() == KeyEvent.VK_F3) =>
          editorsPanel.requestFocusForTopComponent()
        case key: KeyEvent if (key.getKeyCode() == KeyEvent.VK_F4) =>
          consolesPanel.requestFocusForTopComponent()
        case key: KeyEvent if (key.getKeyCode() == KeyEvent.VK_F5) =>
          editorsPanel.requestFocusInWindow()
        case key: KeyEvent if (key.getKeyCode() == KeyEvent.VK_F6) =>
          consolesPanel.requestFocusInWindow()
        case key: KeyEvent if (key.getKeyCode() == KeyEvent.VK_F7) =>
          consolesPanel.requestFocusForToolComponent()
        case key: KeyEvent if (key.getKeyCode() == KeyEvent.VK_F8) =>
          consolesPanel.executeRunOnTopComponentIfPossible()
        case _ => super.dispatchEvent(event)
      }

    }
  })

}