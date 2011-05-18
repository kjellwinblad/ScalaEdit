/*
ScalaEdit - A text editor for Scala programmers
Copyright (C) 2011  Kjell Winblad (kjellwinblad@gmail.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
*/

package me.winsh.scalaedit.gui.editor

import me.winsh.scalaedit.gui._
import me.winsh.scalaedit.gui.project._
import me.winsh.scalaedit.api._
import scala.swing._
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class EditorButtonTabComponent(fileBuffer:FileBuffer,pane:TabbedPane, closeHandler:()=>Unit = (()=>{})) 
	extends ButtonTabComponentImpl(pane, closeHandler){

	//addMouseListener(new MouseAdapter(){
	//	override def mousePressed(e:MouseEvent){
	//		println("Test " + e.isPopupTrigger())
	//		e.getSource.asInstanceOf[java.awt.Component].getParent().dispatchEvent(e)
	//	}
	//})

	//setComponentPopupMenu((new PopupMenu() {
	//		add(new MenuItem(Action("Select in Project Browser"){
	//			fileBuffer.file.foreach(ProjectsPanel().selectFile(_))
	//		}))
	//}).peer)
		 
}