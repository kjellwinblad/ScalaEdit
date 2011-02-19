package me.winsh.scalaedit.gui

import java.io.File
import scala.xml._
import scala.swing._
import Swing._
import scala.swing.event._
import Tree._
import java.awt.Color

class ProjectPanel extends BorderPanel {

  val editableFileSystemTree = new Tree[File] {

    val root = Utils.bestFileChooserDir

    private def filteredChilds(f: File) = f.listFiles.toSeq.filter((f) => (!f.getName.startsWith(".")))

    renderer = new LabelRenderer({ f =>
      {
        val nameToShow = if (f.equals(root)) "" else f.getName
        (Utils.iconForFile(f), nameToShow)
      }

    })

    treeData = TreeModel(root) { f =>
      if (f.isDirectory) filteredChilds(f)
      else Seq()
    }

    treeData = treeData updatableWith { (path, newValue) =>
      val existing = path.last
      val renamedFile = new File(existing.getParent + File.separator + newValue.getName)
      existing renameTo renamedFile
      renamedFile
    }

    editor = Editor((_: File).getName, new File(_: String))

    expandRow(0)

    private val item = new MenuItem(new Action("Change Root...") {
      icon = Utils.getIcon("/images/small-icons/mimetypes/folder.png")
      def apply = println("Hello World");
    })
    //SO FAR SO GOOD, NOW FOR THE UGLY BIT!
    val popup = new javax.swing.JPopupMenu
    popup.add(item.peer)

    this.peer.setComponentPopupMenu(popup)

  }

  val scrollPane = new ScrollPane(editableFileSystemTree)

  add(scrollPane, BorderPanel.Position.Center)

}