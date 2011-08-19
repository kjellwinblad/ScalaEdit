/*
ScalaEdit - A text editor for Scala programmers
Copyright (C) 2011  Kjell Winblad (kjellwinblad@gmail.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
*/

package me.winsh.scalaedit.gui.project

import java.io.File
import scala.xml._
import scala.swing._
import Swing._
import scala.swing.event._
import Tree._
import java.awt.Color
import me.winsh.scalaedit.gui._
import javax.swing.event._
import javax.swing.event.{ TreeExpansionEvent => JTreeExpansionEvent }
import javax.swing.tree.{ MutableTreeNode, TreeNode, DefaultTreeModel, TreePath, TreeModel => JTreeModel }
import java.util.concurrent.atomic.AtomicBoolean
import scala.io.Source
import java.io.FileWriter

class ProjectPanel(val fileSelectionHandler: (File) => Unit) extends BorderPanel {

  val properties = new ProjectPanelProperties()

  var tree = new ProjectTree(Utils.projectDir)


  val scrollPane = new ScrollPane(tree)

  add(scrollPane, BorderPanel.Position.Center)

  class ProjectTree(projectRoot: File) extends Tree[File] {


	  peer.setFont(new Font(peer.getFont().getName(),
    	                    peer.getFont().getStyle(),
      	                  properties.textSize.get))

    val root = projectRoot.getCanonicalFile

    private def filteredSortedChilds(f: File) = f.listFiles.toList.filter((f) => (!f.getName.startsWith(".")))
      .sortWith((e1, e2) => (e1, e2) match {
        case (e1, e2) if (e1.isDirectory && !e2.isDirectory) => true
        case (e1, e2) if (!e1.isDirectory && e2.isDirectory) => false
        case (e1, e2) => e1.getName <= e2.getName
      })

    renderer = new LabelRenderer({ f =>
      {
        val nameToShow = if (f.equals(root)) "" else f.getName
        val icon = if (f.equals(root)) Utils.getIcon("/images/small-icons/mimetypes/source_moc.png") else Utils.iconForFile(f)
        (icon, nameToShow)
      }

    })

    treeData = TreeModel(root) { f =>
      if (f.isDirectory) filteredSortedChilds(f)
      else Seq()
    }

    expandRow(0)

    selection.mode = Tree.SelectionMode.Single

    listenTo(mouse.clicks)

    def handleClick(point: Point, triggersPopup: Boolean) {
      val path = peer.getClosestPathForLocation(point.x, point.y)

      if (peer.getPathBounds(path).contains(point)) {

        tree.peer.setSelectionPath(path)
        if (triggersPopup) {
          fileClickPopupMenu.show(peer, point.x, point.y)
        } else {
          val file = path.getLastPathComponent().asInstanceOf[File]
          file match {
            case f: File if (!f.isDirectory) => {
              fileSelectionHandler(f)
              Utils.bestFileChooserDir = f.getParentFile
            }
            case f: File if (f.isDirectory) => Utils.bestFileChooserDir = f
            case _ => Unit
          }
        }
      } else if (triggersPopup) noFileClickPopupMenu.show(peer, point.x, point.y)
    }
    var mousePressedTriggeredPopup = false

    reactions += {
      case MousePressed(_, point, _, _, triggersPopup) => {
        mousePressedTriggeredPopup = triggersPopup
      }
      case MouseReleased(_, point, _, _, triggersPopup) => {
        handleClick(point, triggersPopup || mousePressedTriggeredPopup)
      }
    }

    private var expandedPaths = Set[TreePath]()

    def addExpandedPath(path: TreePath): Unit = expandedPaths += path

    def removeExpandedPath(path: TreePath) {
      expandedPaths = expandedPaths.filter(!path.isDescendant(_))
      expandedPaths -= path
    }

    def expandAllManuallyExpanded() {
      expandedPaths.foreach(path => {
        expandPath(path)
      })
    }

    peer.addTreeExpansionListener(new TreeExpansionListener {
      def treeCollapsed(event: JTreeExpansionEvent): Unit = removeExpandedPath(event.getPath)
      def treeExpanded(event: JTreeExpansionEvent): Unit = addExpandedPath(event.getPath)
    })

    def selectedPath = peer.getSelectionPath()

    val noFileClickPopupMenu = (new PopupMenu() {
      projectMenuItems.foreach(add(_))
    }).peer

    def fileClickPopupMenu = (new PopupMenu() {

      def selectedOrRoot = {
        val selFile = if (!selection.empty)
          selectedPath.getLastPathComponent().asInstanceOf[File]
        else
          root

        if (selFile.isDirectory) selFile else selFile.getParentFile
      }

      def existsAndShowMessageIfItDoes(file: File) = if (file.exists) {
        Dialog.showMessage(
          message = "The file:\n" + file.getCanonicalPath() + "\n, already exists.",
          title = "File Exists")
        true
      } else false

      def createAction(itemName: String, createAction: (File) => Unit) {
        val file = selectedOrRoot
        Dialog.showInput(
          message = itemName + " name:" + (-50 to file.getCanonicalPath().size).map((a) => " ").mkString(""),
          title = "New " + itemName,
          initial = file.getCanonicalPath() + File.separator) match {
            case Some(path) if (!existsAndShowMessageIfItDoes(new File(path))) => {
              createAction(new File(path))
              refreshAction()
            }
            case None =>
          }
      }

      add(new MenuItem(new Action("New File...") {
        icon = Utils.getIcon("/images/small-icons/actions/filenew.png")
        def apply() = createAction("File", (f: File) => {
          f.getParentFile.mkdirs
          f.createNewFile()
        })
      }))
      add(new MenuItem(new Action("New Directory...") {
        icon = Utils.getIcon("/images/small-icons/actions/fileopen.png")
        def apply() = createAction("Directory", (d: File) => d.mkdirs)
      }))
      add(new MenuItem(new Action("Delete Selected...") {
        icon = Utils.getIcon("/images/small-icons/actions/editdelete.png")
        def apply() = if (!selection.empty) {
          val file = selectedPath.getLastPathComponent().asInstanceOf[File]
          Dialog.showConfirmation(
            message = "Do you want to delete:\n" + file.getCanonicalPath + " ?",
            title = "Delete?") match {
              case Dialog.Result.Yes => {
                file.delete()
                refreshAction()
              }
            }
        }
      }))

      addSeparator()

      add(new MenuItem(new Action("Expand All From Selected") {
        icon = Utils.getIcon("/images/small-icons/go-up.png")
        def apply() = if (!selection.empty) {

          def expandAllFromSelected(selectedPath: TreePath) {
            expandPath(selectedPath)
            selectedPath.getLastPathComponent().asInstanceOf[File].listFiles().foreach((file) => if (file.isDirectory) {
              expandAllFromSelected(selectedPath.pathByAddingChild(file))
            })
          }

          expandAllFromSelected(selectedPath)

        }
      }))

      addSeparator()

      add(new MenuItem(new Action("Copy Selected Path") {
        icon = Utils.getIcon("/images/small-icons/copy-to-clipboard.png")
        def apply() = if (!selection.empty) {
          val file = selectedPath.getLastPathComponent().asInstanceOf[File]
          SwingHelper.clipboardContents = file.getCanonicalPath
        }
      }))

      addSeparator()

      projectMenuItems.foreach(add(_))

    }).peer

    //this.peer.setComponentPopupMenu(popupMenu)

  }

  def changeRootAction() {
    val chooser = new FileChooser(Utils.bestFileChooserDir) {
      title = "Select Folder"
      multiSelectionEnabled = false
      fileSelectionMode = FileChooser.SelectionMode.DirectoriesOnly
    }

    chooser.showDialog(this, "Select")

    if (chooser.selectedFile != null) {
      changeRoot(chooser.selectedFile)
    }
  }

  private object RecentlyOpenedRootsHandler {
    private val recentProjectRootsStore = new File(Utils.propertiesDir, "recently_opened_project_dirs.list")

    def saveNewRoot(newRootPath: String) {

      val recentPaths =
        (newRootPath :: latestRoots.toList).distinct.take(6)

      val writer = new FileWriter(recentProjectRootsStore)
      recentPaths.foreach((path) => writer.write(path + "\n"))
      writer.close()

    }

    def latestRoots: List[String] = try {
      val src = Source.fromFile(recentProjectRootsStore)
      val list = src.getLines().toList
      src.close()
      list
    } catch {
      case _ => Nil
    }
  }

  def changeRoot(root: File) {

    root.mkdirs()

    tree = new ProjectTree(root)

    Utils.projectDir = root
    Utils.bestFileChooserDir = root

    scrollPane.contents = tree

    RecentlyOpenedRootsHandler.saveNewRoot(root.getCanonicalPath)

    if (properties.autoRefreshEnabled.get)
      startAutoRefresh()

  }

  changeRoot(Utils.projectDir)

  def refreshAction() {
    val selection = tree.peer.getSelectionPath
    tree.treeData.peer.fireTreeStructureChanged(new TreePath(Utils.projectDir), Utils.projectDir)
    tree.expandRow(0)
    tree.expandAllManuallyExpanded()
    tree.peer.setSelectionPath(selection)
  }

  lazy val autoRefreshRunning = new AtomicBoolean(false)
  def startAutoRefresh() = synchronized {
    if (!autoRefreshRunning.get) {
      properties.autoRefreshEnabled.set(true)
      autoRefreshRunning.set(true)

      Utils.runInNewThread(() => {

        while (autoRefreshRunning.get) {
          try {
            Thread.sleep(properties.autoRefreshDelayInSeconds.get * 1000)
            SwingHelper.invokeAndWait(() => refreshAction())
          } catch { case _ => autoRefreshRunning.set(false) }
        }

      })
    }
  }

  def stopAutoRefresh() {
    properties.autoRefreshEnabled.set(false)
    autoRefreshRunning.set(false)
  }

  def projectMenuItems =
    (new MenuItem(new Action("Change Root...") {
      icon = Utils.getIcon("/images/small-icons/mimetypes/source_moc.png")
      def apply() = changeRootAction()
    })) ::
      (new Separator()) ::
      (new MenuItem(new Action("Refresh") {
        icon = Utils.getIcon("/images/small-icons/find.png")
        def apply() = refreshAction()
      })) ::
      (new CheckMenuItem("") {
        selected = properties.autoRefreshEnabled.get
        action = new Action("Auto Refresh") {
          icon = Utils.getIcon("/images/small-icons/find.png")
          def apply() {
            if (selected)
              startAutoRefresh()
            else
              stopAutoRefresh()
          }
        }
      }) :: Nil

  def projectMenuItemsAndLatestRoots =
    projectMenuItems ::: ((new Separator()) ::
      RecentlyOpenedRootsHandler.latestRoots.map((r) => new MenuItem(Action(r) {
        changeRoot(new File(r))
      })))

  def selectFile(file: File) {

    //Get the shortest common root of the file and the root
    val conFile = file.getCanonicalFile()

    def filePartsAboveRoot(root: File, file: File): List[AnyRef] = {
      if (root.getPath == file.getPath) {
        root :: tree.peer.getClosestPathForLocation(0, 0).getPath()(0) :: Nil
      } else if (root.getPath.size >= file.getPath.size) {
        //Display error message
        Dialog.showMessage(
          message = "The file could not be found in the project directory.",
          title = "File Not Found")
        Nil
      } else {
        file :: filePartsAboveRoot(root, file.getParentFile)
      }
    }

    val path = new TreePath(filePartsAboveRoot(tree.root, conFile).reverse.toArray)

    tree.peer.setSelectionPath(path)
  }
}
