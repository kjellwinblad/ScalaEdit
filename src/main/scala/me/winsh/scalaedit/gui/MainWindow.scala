package me.winsh.scalaedit.gui

import me.winsh.scalaedit.api._
import scala.swing._
import javax.swing.JSplitPane._
import javax.swing.ImageIcon
class MainWindow extends MainFrame {

  title = "ScalaEdit"

  iconImage = new ImageIcon(this.getClass.getResource("/images/img3.png")).getImage()

  
  val consolesPanel = new ConsolesPanel()

  val editorsPanel = new EditorsPanel()

  val projectsPanel = new EditorsPanel()

  val fileBuffers: Array[FileBuffer] = Array[FileBuffer]()

  menuBar = new MenuBar() {

    val fileMenu = new Menu("File") {

      contents += new MenuItem(Action("New") {
        editorsPanel.addFileEditor(new FileBuffer(None))
      })

      contents += new MenuItem(Action("Open") {
        // open file dialog
      })

      contents += new Separator()

      contents += new MenuItem(Action("Exit") {
        dispose()
      })
    }

    contents += fileMenu

  }

  contents = new SplitPane() {
    orientation = Orientation.Horizontal

    leftComponent = new SplitPane() {
      orientation = Orientation.Vertical
      leftComponent = projectsPanel
      rightComponent = editorsPanel
    }
    rightComponent = consolesPanel

  }
  
  pack()
  
}