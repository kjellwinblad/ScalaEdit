package me.winsh.scalaedit.gui.console

import me.winsh.scalaedit.api._
import java.io.File
import me.winsh.scalaedit.gui._


class StandAloneScalaConsolePanelProperties  extends StandAloneConsoleProperties(
	new File(Utils.propertiesDir, "scala_console.properties"), 
	"Scala Console Properties", 
"""The java_classpath variable can contain dirs and jar files.
Use ":" to separate classpath elements.
""")
{
	if(javaVMArguments==null) javaVMArguments = "-Xmx256M -Xms32M" + 
		(if(System.getProperty("os.name").toLowerCase.contains("windows")) 
			" -Djline.terminal=NONE"
		else
			"")

	protected val javaClasspathID = "java_classpath"
	def javaClasspath:String = props.getProperty(javaClasspathID)
	def javaClasspath_=(args:String) {
		props.setProperty(javaClasspathID, args)
		save()
	}
	if(javaClasspath==null) javaClasspath = ""
}