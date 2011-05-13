package me.winsh.scalaedit.api

import java.util.Properties
import java.io.File
import java.io.FileWriter
import java.io.FileReader

abstract class PropertiesFile(val storagePath:File, val comments:String) {
	
	protected val props = new Properties()
	try{
		props.load(new FileReader(storagePath))
	}catch{
		case _ => //ignore it may be that the file does not exist
	}
	protected def save(){
		val writer = new FileWriter(storagePath)
		props.store(writer, comments)
	}
	
}