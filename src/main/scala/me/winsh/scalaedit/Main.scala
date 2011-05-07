package me.winsh.scalaedit

import me.winsh.scalaedit.gui.MainWindow
import me.winsh.scalaedit.gui.Utils
import scala.swing._
import java.awt.Toolkit
import java.awt.Point
import java.awt.SplashScreen
import java.awt.event._
/**   
 * @author Kjell Winblad
 */
object Main extends SimpleSwingApplication {

	private val splash = SplashScreen.getSplashScreen()
  if (splash != null) {
  	splash.setImageURL(this.getClass.getResource("/images/img3.png"))
  	splash.update()    
  }

	val window = new MainWindow(){
		override def closeOperation {
			dispose() 
			quit()
		}
	}

  def top = {
    
    //window.size = new Dimension(800,800)
    //window.preferredSize = new Dimension(800,800)
    
    window.maximize()

		if(splash != null)
			splash.close()
			
    //window.visible = true
    
    window
    
  }
   
  
  override def shutdown() {
	 window.shutDownOpenResources() 
  }

}