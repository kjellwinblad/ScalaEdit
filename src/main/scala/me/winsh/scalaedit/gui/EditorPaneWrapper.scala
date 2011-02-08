package me.winsh.scalaedit.gui

import scala.swing._
import jsyntaxpane._
import javax.swing._


	class EditorPaneWrapper extends Component {

	DefaultSyntaxKit.initKit()
	
	 lazy val editorPane = peer.getViewport.getView.asInstanceOf[JEditorPane]
	 
	  override lazy val peer: JScrollPane = new JScrollPane(new JEditorPane())
	}