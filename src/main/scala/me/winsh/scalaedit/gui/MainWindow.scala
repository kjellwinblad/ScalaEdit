package me.winsh.scalaedit.gui

import me.winsh.scalaedit.api._
import scala.swing._
import javax.swing.JSplitPane._
import javax.swing.ImageIcon
import java.io.File
import javax.swing.Box
import javax.swing.JOptionPane

class MainWindow extends MainFrame {

  val version = "0.1 Alpha"	
	
  title = "ScalaEdit (" + version + ")" 

  iconImage = Utils.getImage("/images/img3.png")

  
  val consolesPanel = new ConsolesPanel()

  val editorsPanel = new EditorsPanel()

  val projectsPanel = new EditorsPanel()

  val fileBuffers: Array[FileBuffer] = Array[FileBuffer]()

  menuBar = new MenuBar() {

    val fileMenu = new Menu("File") {

      contents += new MenuItem(Action("New") {
        editorsPanel.addFileEditor(new FileBuffer(None))
      })

      contents += new MenuItem(Action("Open...") {
        new FileChooser (new File(".")){
        	title = "Open File(s)"
        	multiSelectionEnabled=true
        	fileSelectionMode = FileChooser.SelectionMode.FilesOnly
        }.showOpenDialog (this)
      })

      contents += new Separator()

      contents += new MenuItem(Action("Exit") {
        dispose()
      })
    }

     val terminalMenu = new Menu("Terminal") {

      contents += new MenuItem(Action("New Scala Terminal") {
        consolesPanel.addScalaTerminal()
      })

    }

    val helpMenu = new Menu("Help") {

      contents += new MenuItem(Action("About") {
    	
val text =
<html>
<h1>ScalaEdit</h1>
<p>
<table>
<tr><td><i>Version:</i></td><td> {version}</td></tr>
<tr><td><i>Homepage:</i></td><td> http://scala-edit.googlecode.com</td></tr>
<tr><td><i>Source code:</i></td><td> http://github.com/kjellwinblad/ScalaEdit</td></tr>
<tr><td><i>Contact:</i></td><td> kjellwinblad@gmail.com</td></tr>
</table>
</p>
</html>.toString.split("\n").foldLeft("")((a,b)=>a+b)
    	  
    		val options = Array[Object]( "OK" );
        JOptionPane.showOptionDialog(null, text, "About",
                                          JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                                          Utils.getIcon("/images/img3.png"), options, options(0)
        )
    		
        //JOptionPane.showMessageDialog(parentComponent = null, message = text, title="About", messageType=JOptionPane.PLAIN_MESSAGE, icon=Utils.getImage("/images/img3.png"),null,null)
      })

    }
     
    contents += fileMenu
    
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
  
  contents = mainSplitPane
  pack()
  
}