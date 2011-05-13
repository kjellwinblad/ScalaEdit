package me.winsh.scalaedit.gui.console

import me.winsh.scalaedit.api._
import java.io.File
import me.winsh.scalaedit.gui._

class SBTConsolePanelProperties extends StandAloneConsoleProperties(
	new File(Utils.propertiesDir, "sbt_console.properties"), 
	"SBT Console Properties",""){

	if(javaVMArguments == null) javaVMArguments = "-Xmx512M" + 
		(if(System.getProperty("os.name").toLowerCase.contains("windows")) 
			" -Djline.terminal=jline.UnsupportedTerminal"
		else
			"")

}
  