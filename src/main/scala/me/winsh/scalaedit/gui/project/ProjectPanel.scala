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
import scala.io.Source
import java.io.FileWriter

class ProjectPanel(val fileSelectionHandler: (File) => Unit) extends BorderPanel {

	val autorefreshIntervallSeconds = 5

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

    listenTo(mouse.clicks)

    reactions += {
      case MousePressed (_, point, _, _, _) => {
        val path = peer.getClosestPathForLocation(point.x, point.y)
        if(peer.getPathBounds(path).contains(point)){
        	tree.peer.setSelectionPath(path)
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

    def selectedPath = peer.getSelectionPath()

    val popupMenu = (new PopupMenu() {

			def selectedOrRoot = {
				val selFile = if(!selection.empty)
      		selectedPath.getLastPathComponent().asInstanceOf[File]
      	else
      		projectRoot

      	if(selFile.isDirectory) selFile else selFile.getParentFile
			}

			def existsAndShowMessageIfItDoes(file:File) = if(file.exists){
				Dialog.showMessage (
					message = "The file:\n" + file.getCanonicalPath() + "\n, already exists.", 
					title = "File Exists")
				true
			}else false

			def createAction(itemName:String, createAction:(File)=>Unit){
				val file = selectedOrRoot
      	Dialog.showInput(
      		message = itemName + " name:" + (-50 to file.getCanonicalPath().size).map((a)=>" ").mkString(""), 
      		title = "New " + itemName,
      		initial = file.getCanonicalPath() + File.separator) match{
      			case Some(path) if(!existsAndShowMessageIfItDoes(new File(path)))=> {
      				createAction(new File(path))
      				refreshAction()
      			}
      			case None => 
      		}
			}
    	
    	add(new MenuItem(new Action("New File...") {
      	icon = Utils.getIcon("/images/small-icons/actions/filenew.png")
      	def apply() = createAction("File", (f:File)=>{
      		f.getParentFile.mkdirs
      		f.createNewFile()
      	})
    	}))
    	add(new MenuItem(new Action("New Directory...") {
      	icon = Utils.getIcon("/images/small-icons/actions/fileopen.png")
      	def apply() = createAction("Directory", (d:File)=>d.mkdirs)
    	}))
    	add(new MenuItem(new Action("Delete Selected...") {
      	icon = Utils.getIcon("/images/small-icons/actions/editdelete.png")
      	def apply() = if(!selection.empty) {
      		val file = selectedPath.getLastPathComponent().asInstanceOf[File]
      		Dialog.showConfirmation (
      			message = "Do you want to delete:\n" + file.getCanonicalPath + " ?", 
      			title = "Delete?" ) match{
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
      	def apply() = if(!selection.empty) {

					def expandAllFromSelected(selectedPath:TreePath){
						expandPath(selectedPath)
						selectedPath.getLastPathComponent().asInstanceOf[File].listFiles().foreach((file)=>if(file.isDirectory){
							expandAllFromSelected(selectedPath.pathByAddingChild(file))
						})
					}

					expandAllFromSelected(selectedPath)
      		 
      	}
    	}))
      
      addSeparator()

			add(new MenuItem(new Action("Copy Selected Path") {
      	icon = Utils.getIcon("/images/small-icons/copy-to-clipboard.png")
      	def apply() = if(!selection.empty) {
					val file = selectedPath.getLastPathComponent().asInstanceOf[File]
					Utils.clipboardContents = file.getCanonicalPath
      	}
    	}))

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
    		selected = false
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
    
    this.peer.setComponentPopupMenu(popupMenu)

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

  def changeRoot(root:File) {

    tree = new ProjectTree(root)

    Utils.projectDir = root
    Utils.bestFileChooserDir = root
      
    scrollPane.contents = tree

		val recentProjectRootsStore = new File(Utils.propertiesDir, ".recentlyOpenedProjectDirs")

    val recentPaths = try{
    	val source = Source.fromFile(recentProjectRootsStore)
    	(root.getCanonicalPath::source.getLines().toList).distinct.take(6)
    }catch{
    	case _ => List(root.getCanonicalPath)
    }

		val writer = new FileWriter(recentProjectRootsStore)

		recentPaths.foreach((path)=>writer.write(path+"\n"))

    writer.close()

  }

  changeRoot(Utils.projectDir)
  
  def refreshAction() {
  	val selection = tree.peer.getSelectionPath
    tree.treeData.peer.fireTreeStructureChanged(new TreePath(Utils.projectDir), Utils.projectDir)
    tree.expandRow(0)
    tree.expandAllManuallyExpanded()
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

}