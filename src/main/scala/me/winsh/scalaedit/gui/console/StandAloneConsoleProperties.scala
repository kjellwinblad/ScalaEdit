package me.winsh.scalaedit.gui.console

import me.winsh.scalaedit.api._
import java.io.File
import me.winsh.scalaedit.gui._

abstract class StandAloneConsoleProperties(override val storagePath:File, title:String, comments:String)
	extends PropertiesFile(storagePath, 
												 title +
"""
 
The starting_dir property is the dir that the process will run in.
If starting_dir is set to CURRENT_PROJECT_DIR then the current project directory will be used.
The value of the arguments propery will be sent to the process. It is optional.
""" +
comments){

	protected val javaVMArgumentsID = "java_vm_arguments"
	def javaVMArguments:String = props.getProperty(javaVMArgumentsID)
	def javaVMArguments_=(args:String) {
		props.setProperty(javaVMArgumentsID, args)
		save()
	}

	private val startingDirID = "starting_dir"
	def startingDir:File = props.getProperty(startingDirID) match {
		case "CURRENT_PROJECT_DIR" => Utils.projectDir
		case p => new File(p)
	}
	def startingDir_=(args:String) {
		props.setProperty(startingDirID, args)
		save()
	}
	if(props.getProperty(startingDirID) == null) startingDir = "CURRENT_PROJECT_DIR"

	private val argumentsID = "arguments"
	def arguments:String = props.getProperty(argumentsID)
	def arguments_=(args:String) {
		props.setProperty(argumentsID, args)
		save()
	}
	if(arguments==null)arguments = ""
}