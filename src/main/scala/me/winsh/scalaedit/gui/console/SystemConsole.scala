/*
ScalaEdit - A text editor for Scala programmers
Copyright (C) 2011  Kjell Winblad (kjellwinblad@gmail.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
*/

package me.winsh.scalaedit.gui.console

import scala.swing._

//import com.wittams.gritty.swing.GrittyTerminal
//import com.wittams.gritty.Tty
//import com.wittams.gritty.Questioner
import java.awt.Dimension
import java.io.IOException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
//import com.wittams.gritty.jsch.JSchTty
//import com.wittams.gritty.RequestOrigin;
//import com.wittams.gritty.ResizePanelDelegate;

class SystemConsole extends ConsolePanel {

	def close()={false}
	
  private val in = new ByteArrayInputStream(new Array[Byte](10000))
  private val out = new ByteArrayOutputStream(10000)

  val consoleType = ScalaConsole

  private class TerminalPaneWrapper extends Component {
//    override lazy val peer: GrittyTerminal = new GrittyTerminal()
  }

  private val terminalPaneWrapper = new TerminalPaneWrapper()

  val terminalPane = terminalPaneWrapper.peer

//  terminalPane.setTty(new JSchTty());
//  terminalPane.start();

 // val termPanel = terminalPane.getTermPanel();

 // termPanel.setVisible(true);

  def sizeFrameForTerm(frame: Panel) {
    val d = terminalPane.getPreferredSize();

    d.width += frame.size.width //- frame.getContentPane().getWidth();
    d.height += frame.size.height //- frame.contentPane.getHeight(); 
//    frame.size = (d);
  }
  val thisPanel = this

  //termPanel.setResizePanelDelegate(new ResizePanelDelegate() {
  //  def resizedPanel(pixelDimension: Dimension, origin: RequestOrigin) {
  //    if (origin == RequestOrigin.Remote)
  //      sizeFrameForTerm(thisPanel);
  //  }
  //});

  add(terminalPaneWrapper, BorderPanel.Position.Center)

}