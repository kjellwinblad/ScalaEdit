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

class ProjectsPanel(val fileSelectionHandler: (File) => Unit) extends BorderPanel {

	var projectPanel = new ProjectPanel(fileSelectionHandler)

	add(projectPanel, BorderPanel.Position.Center)

	def changeRootAction() {
		projectPanel.changeRootAction()
	}

	def changeRoot(root:File) {
		projectPanel.changeRoot(root)
	}

	def selectFile(file:File){
		projectPanel.selectFile(file)
	}

}

object ProjectsPanel{

	var projectsPanel:ProjectsPanel = null

	def apply(fileSelectionHandler: (File) => Unit) ={
		
		projectsPanel = new ProjectsPanel(fileSelectionHandler)
		
		projectsPanel

	}

	def apply() = projectsPanel
}