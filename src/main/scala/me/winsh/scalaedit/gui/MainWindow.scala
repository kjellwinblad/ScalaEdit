package me.winsh.scalaedit.gui

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

class MainWindow extends MainFrame {

  val version = "0.0.3"

  title = "ScalaEdit (" + version + ")"

  iconImage = Utils.getImage("/images/img3.png")

  val consolesPanel = new ConsolesPanel()

  val editorsPanel = EditorsPanel()

  val projectsPanel = new ProjectsPanel((f: File) => editorsPanel.addFileEditor(FileBuffer(f)))

  val fileBuffers: Array[FileBuffer] = Array[FileBuffer]()

  menuBar = new MenuBar() {

    val fileMenu = new Menu("File") {

      contents += new MenuItem(new Action("New") {

        icon = Utils.getIcon("/images/small-icons/actions/filenew.png")

        def apply() {
          editorsPanel.addFileEditor(FileBuffer())
        }

      })

      contents += new MenuItem(new Action("Open...") {

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
          dispose()
        }

      })
    }

    val projectMenu = new Menu("Project") {
      contents += new MenuItem(new Action("Change Project Root...") {

        icon = Utils.getIcon("/images/small-icons/mimetypes/source_moc.png")

        def apply() {
          projectsPanel.changeRootAction()
        }

      })
    }

    val terminalMenu = new Menu("Terminal") {
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
    }

    val helpMenu = new Menu("Help") {
  
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

    }

    contents += fileMenu
    
    contents += projectMenu

    contents += terminalMenu

    contents += Swing.HGlue

    contents += helpMenu

  }

  private val mainSplitPane = new SplitPane() {
    orientation = Orientation.Horizontal

    val editorProjectSplitPane = new SplitPane() {
      orientation = Orientation.Vertical
      leftComponent = projectsPanel
      rightComponent = editorsPanel
    }

    editorProjectSplitPane.oneTouchExpandable = true
    
    leftComponent = editorProjectSplitPane
    rightComponent = consolesPanel

  }
  mainSplitPane.oneTouchExpandable = true
  
  mainSplitPane.dividerLocation = 0.8

  contents = mainSplitPane
  pack()

}