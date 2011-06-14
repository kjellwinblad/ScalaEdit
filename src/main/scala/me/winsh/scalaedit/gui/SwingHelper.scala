/*
ScalaEdit - A text editor for Scala programmers
Copyright (C) 2011  Kjell Winblad (kjellwinblad@gmail.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
*/

package me.winsh.scalaedit.gui

import scala.swing._

import javax.swing.SwingUtilities
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.HierarchyEvent
import java.awt.event.HierarchyListener
import java.awt.Toolkit
import java.awt.datatransfer._

object SwingHelper {

  /**
   * Set the divider location for a SplitPane. This works even if the SplitPane is not visible.
   * (http://blog.darevay.com/2011/06/jsplitpainintheass-a-less-abominable-fix-for-setdividerlocation/)
   */
  def setDividerLocation(splitter: SplitPane, proportion: Double): SplitPane = {
    val sSplitter = splitter.peer
    if (sSplitter.isShowing) {
      if (sSplitter.getWidth() > 0 && sSplitter.getHeight() > 0) {
        sSplitter.setDividerLocation(proportion)
      } else {
        sSplitter.addComponentListener(new ComponentAdapter() {
          override def componentResized(ce: ComponentEvent) {
            sSplitter.removeComponentListener(this)
            setDividerLocation(splitter, proportion)
          }
        })
      }
    } else {
      sSplitter.addHierarchyListener(new HierarchyListener() {
        def hierarchyChanged(e: HierarchyEvent) {
          if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 &&
            sSplitter.isShowing()) {
            sSplitter.removeHierarchyListener(this)
            setDividerLocation(splitter, proportion)
          }
        }
      })
    }
    splitter
  }

  def invokeAndWait(fun: () => Unit) =
    if (SwingUtilities.isEventDispatchThread())
      fun()
    else
      SwingUtilities.invokeAndWait(new Runnable {
        def run {
          fun()
        }
      })

  def invokeLater(fun: () => Unit) =
    SwingUtilities.invokeLater(new Runnable {
      def run {
        fun()
      }
    })

  def clipboardContents_=(contentToSet: String) {
    val stringSelection = new StringSelection(contentToSet);
    val clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(stringSelection, new ClipboardOwner() {
      def lostOwnership(clipboard: Clipboard, contents: Transferable) { /*Nothing needs to be done*/ }
    })
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
}