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
import javax.swing.event.{TreeExpansionEvent => JTreeExpansionEvent}
import javax.swing.tree.{MutableTreeNode, TreeNode, DefaultTreeModel, TreePath, TreeModel => JTreeModel}
import java.util.concurrent.atomic.AtomicBoolean 

class ProjectPanel(val fileSelectionHandler: (File) => Unit) extends BorderPanel {

	val autorefreshIntervallSeconds = 2

  var tree = new ProjectTree(Utils.projectDir)

  val scrollPane = new ScrollPane(tree)

  add(scrollPane, BorderPanel.Position.Center)

  class ProjectTree(val projectRoot:File) extends Tree[File] {

    val root = projectRoot

    private def filteredSortedChilds(f: File) = f.listFiles.toList.filter((f) => (!f.getName.startsWith(".")))
    .sortWith((e1, e2) =>(e1, e2) match{
    	case (e1,e2) if(e1.isDirectory && !e2.isDirectory) => true
    	case (e1,e2) if(!e1.isDirectory && e2.isDirectory) => false
    	case (e1,e2) => e1.getName <= e2.getName
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

    listenTo(selection)

		private var ignoreSelection = false

		def ignoreNextSelection() = ignoreSelection = true

    reactions += {
    	case TreeNodeSelected(_) if(ignoreSelection) => ignoreSelection = false
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

		private var expandedPaths = Set[TreePath]()

		def addExpandedPath(path:TreePath):Unit = expandedPaths += path

		def removeExpandedPath(path:TreePath){
			expandedPaths = expandedPaths.filter(!path.isDescendant(_))
			expandedPaths -= path
		}

		def expandAllManuallyExpanded(){
			expandedPaths.foreach(path => {
				expandPath(path)
			})
		}
		
    peer.addTreeExpansionListener(new TreeExpansionListener{
    	def treeCollapsed(event:JTreeExpansionEvent):Unit = removeExpandedPath(event.getPath)
			def	treeExpanded(event:JTreeExpansionEvent):Unit =  addExpandedPath(event.getPath)
    })



    val popup = (new PopupMenu() {
      addSeparator()
      add(new MenuItem(new Action("Change Root...") {
      	icon = Utils.getIcon("/images/small-icons/mimetypes/source_moc.png")
      	def apply() = changeRootAction()
    	}))
    	add(new MenuItem(new Action("Refresh") {
      	icon = Utils.getIcon("/images/small-icons/find.png")
      	def apply() = refreshAction()
    	}))
    	add(new CheckMenuItem (""){
    		selected = true
    		action = new Action("Auto Refresh ("+autorefreshIntervallSeconds+" sec)") {
    			icon = Utils.getIcon("/images/small-icons/find.png")
    			def apply(){
    				if(selected)
    					startAutoRefresh()
    				else
    					stopAutoRefresh()
    			}
    		}
    	})
    }).peer
    
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

      tree = new ProjectTree(chooser.selectedFile)

      Utils.projectDir = chooser.selectedFile
      Utils.bestFileChooserDir = chooser.selectedFile
      
      scrollPane.contents = tree
    }
  }
  
  def refreshAction() {
  	val selection = tree.peer.getSelectionPath
    tree.treeData.peer.fireTreeStructureChanged(new TreePath(Utils.projectDir), Utils.projectDir)
    tree.expandRow(0)
    tree.expandAllManuallyExpanded()
    tree.ignoreNextSelection()
    tree.peer.setSelectionPath(selection)
  }

	val autoRefreshRunning = new AtomicBoolean(false)
  def startAutoRefresh() = synchronized {
		if(!autoRefreshRunning.get) {
			
			autoRefreshRunning.set(true)
			
			Utils.runInNewThread(()=>{
		
				while(autoRefreshRunning.get){
					try{
						Thread.sleep(autorefreshIntervallSeconds*1000)
						Utils.swingInvokeAndWait(() => refreshAction())
					}catch{case _ => autoRefreshRunning.set(false)}
				}
  	
  		})
		}
  }

  def stopAutoRefresh() {
    autoRefreshRunning.set(false)
  }

  startAutoRefresh()

}