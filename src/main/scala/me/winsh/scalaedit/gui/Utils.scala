/*
ScalaEdit - A text editor for Scala programmers
Copyright (C) 2011  Kjell Winblad (kjellwinblad@gmail.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
*/

package me.winsh.scalaedit.gui

import me.winsh.scalaedit.api._
import javax.swing._
import java.awt.Toolkit
import java.awt.datatransfer._
import java.io.File
import scala.swing._

object Utils { 

  var bestFileChooserDir = projectDir

  var projectDir = new File(".")
  
  def clipboardContents_=(contentToSet: String) {
    val stringSelection = new StringSelection(contentToSet);
    val clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(stringSelection, new ClipboardOwner() {
      def lostOwnership(clipboard: Clipboard, contents: Transferable) { /*Nothing needs to be done*/ }
    });
  }

  def clipboardContents = {
    var result = "";
    val clipboard = Toolkit.getDefaultToolkit().getSystemClipboard()

    val contents = clipboard.getContents(this)

    val hasTransferableText =
      (contents != null) &&
        contents.isDataFlavorSupported(DataFlavor.stringFlavor)

    if (hasTransferableText) {
      try {
        result = contents.getTransferData(DataFlavor.stringFlavor).toString
      } catch {
        case e => {
          println(e)
          e.printStackTrace()
        }
      }

    }
    result
  }

  def getImage(classPathPath: String) = getIcon(classPathPath).getImage()

  def getIcon(classPathPath: String) = new ImageIcon(this.getClass.getResource(classPathPath))

  def swingInvokeAndWait(fun: () => Unit) {

  	if(SwingUtilities.isEventDispatchThread())
  		fun()
  	else
    	SwingUtilities.invokeAndWait(new Runnable {
      	def run {
        	fun()
      	}
    	})
  }

  def swingInvokeLater(fun: () => Unit) {

  	if(SwingUtilities.isEventDispatchThread())
  		fun()
  	else
    	SwingUtilities.invokeLater(new Runnable {
      	def run {
        	fun()
      	}
    	})
  }

  def runInNewThread(fun: () => Unit) {
    new Thread(new Runnable() {
      def run {
        fun()
      }
    }).start()
  }

  def iconForFile(file:File) = 
	  if(file.isDirectory)
	 	  getIcon("/images/small-icons/mimetypes/folder.png")
	  else
	 	  iconFromContentType((FileBuffer(file)).contentType)
  
  def iconFromContentType(contentType: String) = contentType.toLowerCase match {
    case "text/c" => getIcon("/images/small-icons/mimetypes/source_c.png")
    case "text/cpp" => getIcon("/images/small-icons/mimetypes/source_cpp.png")
    case "text/c++" => getIcon("/images/small-icons/mimetypes/source_cpp.png")
    case "text/cs" => getIcon("/images/small-icons/mimetypes/source.png")
    case "text/c#" => getIcon("/images/small-icons/mimetypes/source.png")
    case "text/css" => getIcon("/images/small-icons/mimetypes/source.png")
    case "text/pas" => getIcon("/images/small-icons/mimetypes/source.png")
    case "text/for" => getIcon("/images/small-icons/mimetypes/source_f.png")
    case "text/f" => getIcon("/images/small-icons/mimetypes/source_f.png")
    case "text/f77" => getIcon("/images/small-icons/mimetypes/source_f.png")
    case "text/f90" => getIcon("/images/small-icons/mimetypes/source_f.png")
    case "text/groovy" => getIcon("/images/small-icons/mimetypes/source.png")
    case "text/html" => getIcon("/images/small-icons/mimetypes/html.png")
    case "text/htm" => getIcon("/images/small-icons/mimetypes/html.png")
    case "text/java" => getIcon("/images/small-icons/mimetypes/source_java.png")
    case "text/js" => getIcon("/images/small-icons/mimetypes/source_j.png")
    case "text/jsp" => getIcon("/images/small-icons/mimetypes/source_j.png")
    case "text/lisp" => getIcon("/images/small-icons/mimetypes/source_l.png")
    case "text/lsp" => getIcon("/images/small-icons/mimetypes/source_l.png")
    case "text/cl" => getIcon("/images/small-icons/mimetypes/source_l.png")
    case "text/lua" => getIcon("/images/small-icons/mimetypes/source_l.png")
    case "text/makefile" => getIcon("/images/small-icons/mimetypes/source.png")
    case "text/pl" => getIcon("/images/small-icons/mimetypes/source_pl.png")
    case "text/php" => getIcon("/images/small-icons/mimetypes/source_php.png")
    case "text/properties" => getIcon("/images/small-icons/mimetypes/source.png")
    case "text/py" => getIcon("/images/small-icons/mimetypes/source_py.png")
    case "text/rb" => getIcon("/images/small-icons/mimetypes/source.png")
    case "text/sas" => getIcon("/images/small-icons/mimetypes/source_s.png")
    case "text/scala" => getIcon("/images/small-icons/mimetypes/scala_file.png")
    case "text/sql" => getIcon("/images/small-icons/mimetypes/source_s.png")
    case "text/tcl" => getIcon("/images/small-icons/mimetypes/source.png")
    case "text/sh" => getIcon("/images/small-icons/mimetypes/shell1.png")
    case "text/bat" => getIcon("/images/small-icons/mimetypes/shell1.png")
    case "text/xml" => getIcon("/images/small-icons/mimetypes/resource.png")
    case _ => getIcon("/images/small-icons/mimetypes/txt.png")
  }
  
  val propertiesDir = new File(new File(System.getProperty("user.home")), ".scalaedit")
  
  def showErrorMessage(parent:Component = null, message:String){
	  Dialog.showMessage(parent,
      message = message,
      title = "Error",
      messageType = Dialog.Message.Error)
  }

}