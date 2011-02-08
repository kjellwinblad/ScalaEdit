package me.winsh.scalaedit

import me.winsh.scalaedit.gui.MainWindow
import scala.swing._

/**
 * @author Kjell Winblad
 */
object Main {
  

  def main(args : Array[String]) {
	  val window = new MainWindow()
	   window.size = new Dimension(500,500)
	   window.visible=true
  }

}
