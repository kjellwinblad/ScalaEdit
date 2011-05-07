package me.winsh.scalaedit.gui

import me.winsh.scalaedit.api._
import scala.swing._
import scala.io.Source
import java.io.InputStream
import java.awt.Font
import javax.swing.text.DefaultCaret

class LicenseView(resourcePath:String) extends ScrollPane with Closeable {

	contents = new TextArea(){

		private val car = peer.getCaret().asInstanceOf[DefaultCaret]
    
    car.setUpdatePolicy(DefaultCaret.NEVER_UPDATE)
		
		editable = false
	
		peer.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12))
	
		private val licenseTextStream = this.getClass.getResourceAsStream(resourcePath) 
		
		text = Source.fromInputStream(licenseTextStream).getLines().mkString("\n")
		
	}

	def close() = true
	
}