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

	var projectPanel = new ProjectPanel(fileSelectionHandler)

	add(projectPanel, BorderPanel.Position.Center)

	def changeRootAction() {
		projectPanel.changeRootAction()
	}

	def changeRoot(root:File) {
		projectPanel.changeRoot(root)
	}

	preferredSize = new Dimension(200,700)

}