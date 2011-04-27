package me.winsh.scalaedit

import me.winsh.scalaedit.gui.MainWindow
import scala.swing._
import java.awt.Toolkit
import java.awt.Point
/**   
 * @author Kjell Winblad
 */
object Main extends SimpleSwingApplication {
  
	val window = new MainWindow(){
		override def closeOperation {
			dispose() 
			quit()
		}
	}

  def top = {
    
    window.visible = true
    window
  }
   
  
  override def shutdown() {
	 window.shutDownOpenResources() 
  }

}