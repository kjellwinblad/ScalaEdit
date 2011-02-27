package me.winsh.scalaedit

import me.winsh.scalaedit.gui.MainWindow
import scala.swing._

/**
 * @author Kjell Winblad
 */
object Main extends SimpleSwingApplication {

  def top = {
    val window = new MainWindow()
    window.size = new Dimension(500, 500)
    window.visible = true
    window  
  }
  
}
