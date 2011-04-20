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
			quit()
		}
	}

  def top = {

    // Get the size of the screen
    val dim = Toolkit.getDefaultToolkit().getScreenSize();
    window.size = new Dimension((dim.height * 0.97).toInt, (dim.height * 0.65).toInt)

    // Determine the new location of the window
    val w = window.size.width;
    val h = window.size.height;
    val x = 20;
    val y = (dim.height - h) / 2;

    // Move the window
    window.location = new Point(x, y);
    
    window.visible = true
    window
  }
   
  
  override def shutdown() {
	 window.shutDownOpenResources() 
  }

}