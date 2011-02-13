package me.winsh.scalaedit.gui

import me.winsh.scalaedit.api._
import scala.swing._
import jsyntaxpane._
import javax.swing._

class EditorPanel(val fileBuffer: FileBuffer) extends BorderPanel with Closeable {

  private val advancedPane = new EditorPaneWrapper()

  val editorPane = advancedPane.editorPane

  val scrollPane = advancedPane.peer

  add(advancedPane, BorderPanel.Position.Center)

  editorPane.setContentType("text/scala")

  def close() = {
    false
  }

}