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
import java.awt.Desktop
import java.net.URI
import scala.swing.event._
import scala.io.Source
import javax.swing.event.MenuListener
import javax.swing.event.MenuEvent

class MainWindow extends Frame {

  val version = "0.2.0"

  title = "ScalaEdit (" + version + ")"

  iconImage = Utils.getImage("/images/img3.png")

  val consolesPanel = new ConsolesPanel()

  val editorsPanel = EditorsPanel()

  val projectsPanel = ProjectsPanel((f: File) => editorsPanel.addFileEditor(FileBuffer(f)))

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
        contents += new MenuItem(new Action("Change Project Root...") {

          icon = Utils.getIcon("/images/small-icons/mimetypes/source_moc.png")

          def apply() {
            projectsPanel.changeRootAction()
          }

        })

        val recentPathItems = try {
          val src = Source.fromFile(new File(Utils.propertiesDir, ".recentlyOpenedProjectDirs"))

          contents += new Separator()

          src.getLines().foreach((path) => {
            contents += new MenuItem(Action(path) {
              projectsPanel.changeRoot(new File(path))
            })
          })

          src.close()
        } catch {
          case _ =>
        }

        //revalidate()
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

      contents += new MenuItem(new Action("New SBT Terminal") {

        icon = Utils.getIcon("/images/small-icons/illustrations/sbt-terminal.png")

        def apply() {
          consolesPanel.addSBTTerminal()
        }

      })

      contents += new Menu("Properties") {
        contents += new MenuItem(new Action("SBT Terminal Properties...") {

          icon = Utils.getIcon("/images/small-icons/illustrations/sbt-terminal.png")

          def apply() {
            val props = new SBTConsolePanelProperties()
            editorsPanel.addFileEditor(FileBuffer(props.storagePath))
          }

        })
        contents += new MenuItem(new Action("Scala Terminal Properties...") {

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

      contents += new MenuItem(Action("About") {

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

      val desktop = Desktop.getDesktop

      contents += new MenuItem(Action("""<html><font color="BLUE"><u>Scala API</u></font>""") {
        desktop.browse(new URI("http://www.scala-lang.org/api/current/index.html"))
      })

      contents += new MenuItem(Action("""<html><font color="BLUE"><u>Java API</u></font>""") {
        desktop.browse(new URI("http://download.oracle.com/javase/6/docs/api/"))
      })

      contents += new MenuItem(Action("""<html><font color="BLUE"><u>sbt documentation</u></font>""") {
        desktop.browse(new URI("http://code.google.com/p/simple-build-tool/"))
      })

      contents += new MenuItem(Action("""<html><font color="BLUE"><u>ScalaEdit Web Site</u></font>""") {
        desktop.browse(new URI("http://code.google.com/p/scala-edit/"))
      })

    }

    contents += fileMenu

    contents += projectMenu

    contents += terminalMenu

    contents += Swing.HGlue

    contents += helpMenu

  }

  private val mainSplitPane = new SplitPane() {
    orientation = Orientation.Horizontal
    resizeWeight = 1.0

    val editorProjectSplitPane = new SplitPane() {
      resizeWeight = 0.0
      orientation = Orientation.Vertical
      leftComponent = projectsPanel
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

  mainSplitPane.dividerLocation = 477

  mainSplitPane.editorProjectSplitPane.dividerLocation = 200

  pack()

  maximize()

}