package me.winsh.scalaedit.gui.project

import java.io.File
import scala.xml._
import scala.swing._
import Swing._
import scala.swing.event._
import Tree._
import java.awt.Color
import me.winsh.scalaedit.gui._

class ProjectsPanel(val fileSelectionHandler: (File) => Unit) extends BorderPanel {

  var editableFileSystemTree = createTree(Utils.projectDir)

  val scrollPane = new ScrollPane(editableFileSystemTree)

  add(scrollPane, BorderPanel.Position.Center)

  private def createTree(projectRoot: File): Tree[File] = new Tree[File] {

    val root = projectRoot

    private def filteredChilds(f: File) = f.listFiles.toSeq.filter((f) => (!f.getName.startsWith(".")))

    renderer = new LabelRenderer({ f =>
      {
        val nameToShow = if (f.equals(root)) "" else f.getName
        val icon = if (f.equals(root)) Utils.getIcon("/images/small-icons/mimetypes/source_moc.png") else Utils.iconForFile(f)
        (icon, nameToShow)
      }

    })

    treeData = TreeModel(root) { f =>
      if (f.isDirectory) filteredChilds(f)
      else Seq()
    }

    expandRow(0)

    listenTo(selection)

    reactions += {
      case TreeNodeSelected(file) =>
        file match {
          case f: File if (!f.isDirectory) => {
        	  fileSelectionHandler(f)
        	  Utils.bestFileChooserDir = f.getParentFile
          }
          case f: File if (f.isDirectory) => Utils.bestFileChooserDir = f
          case _ => Unit
        }
    }

    val popup = new javax.swing.JPopupMenu
    
    private val changeRootItem = new MenuItem(new Action("Change Root...") {
      icon = Utils.getIcon("/images/small-icons/mimetypes/source_moc.png")
      def apply() = changeRootAction()
    })

    popup.add(changeRootItem.peer)

    private val refreshItem = new MenuItem(new Action("Refresh") {
      icon = Utils.getIcon("/images/small-icons/find.png")
      def apply() = refreshAction()
    })

    popup.add(refreshItem.peer)
    
    this.peer.setComponentPopupMenu(popup)

  }

  def changeRootAction() {
    val chooser = new FileChooser(Utils.bestFileChooserDir) {
      title = "Select Folder"
      multiSelectionEnabled = false
      fileSelectionMode = FileChooser.SelectionMode.DirectoriesOnly
    }

    chooser.showDialog(this, "Select")

    if (chooser.selectedFile != null) {

      editableFileSystemTree = createTree(chooser.selectedFile)

      Utils.projectDir = chooser.selectedFile
      Utils.bestFileChooserDir = chooser.selectedFile
      
      scrollPane.contents = editableFileSystemTree
    }
  }
  
    def refreshAction() {

      editableFileSystemTree = createTree(Utils.projectDir)
 
      scrollPane.contents = editableFileSystemTree

  }

}