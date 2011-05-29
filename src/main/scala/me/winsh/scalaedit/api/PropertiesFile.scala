/*
ScalaEdit - A text editor for Scala programmers
Copyright (C) 2011  Kjell Winblad (kjellwinblad@gmail.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
*/

package me.winsh.scalaedit.api

import java.util.Properties
import java.awt.Color
import java.io.File
import java.io.FileWriter
import java.io.FileReader

abstract class PropertiesFile(val storagePath: File, val comments: String) {

  protected val props = new Properties()
  try {
    props.load(new FileReader(storagePath))
  } catch {
    case _ => //ignore it may be that the file does not exist
  }	
    
  protected def save() {
    val writer = new FileWriter(storagePath)
    props.store(writer, comments)  
  }

	private def isDefined(propertyId:String) = props.getProperty(propertyId) match {
    case null => false
    case s => true
  }

	//String
  private def setValue(propertyId:String, value:String){
    props.setProperty(propertyId, value)
    save()
  }
	
  private def getStringValue(propertyId:String, defaultValue:String = "") = props.getProperty(propertyId) match {
    case null => defaultValue
    case s => s
  }

	//Boolean
  private def setValue(propertyId:String, value:Boolean){
    props.setProperty(propertyId, (if(value)"YES"else"NO"))
    save()
  }
	
  private def getBooleanValue(propertyId:String, defaultValue:Boolean = true) = props.getProperty(propertyId) match {
    case null => defaultValue
    case s if(s.trim.matches("""(?i)yes|true|y|ye"""))=> true
    case s if(s.trim.matches("""(?i)no|false|n"""))=> false
    case _=> defaultValue
  }

	//Int
  private def setValue(propertyId:String, value:Int){
    props.setProperty(propertyId, value.toString)
    save()
  }

  private def getIntValue(propertyId:String, defaultValue:Int = 0) = props.getProperty(propertyId) match {
    case null => defaultValue
    case s => try{s.toInt}catch{case _=>defaultValue}
  }

	//Color
  private val defaultColor = new Color(1.0f,0.0f,0.0f,0.4f)
  private def setValue(propertyId:String, value:Color){
    props.setProperty(propertyId, 
    	List("red("+(value.getRed.toFloat/255)+")",
    		"green("+(value.getGreen.toFloat/255)+")",
    		"blue("+(value.getBlue.toFloat/255)+")",
    		"alpha("+(value.getAlpha.toFloat/255)+")").mkString(","))
    save()
  }

  private def getColorValue(propertyId:String, defaultValue:Color = defaultColor) = try{

		def cParm(n:String) = """\s*""" + n + """\s*\(\s*([0-9]+?\.?[0-9]+?)\s*\)\s*"""
		val colorRegExp = List("red","green","blue","alpha").map(cParm(_)).mkString(",").r

  	val colorRegExp(red,green,blue,alpha) = props.getProperty(propertyId)
  	new Color(red.toFloat,green.toFloat,blue.toFloat, alpha.toFloat)
  }catch{
  	case _ => defaultValue
  }

	//Properties
	abstract class Property[A](val id:String,val defaultValue:A){
		def get:A
		def set(value:A)
	}

  class StringProperty(id:String,defaultValue:String = "") extends Property[String](id,defaultValue){
  	if(!isDefined(id)) setValue(id, defaultValue)
  	def get = getStringValue(id, defaultValue)
  	def set(value:String) = setValue(id, value)
  }

  class IntProperty(id:String,defaultValue:Int = 0) extends Property[Int](id,defaultValue){
  	if(!isDefined(id)) setValue(id, defaultValue)
  	def get = getIntValue(id, defaultValue)
  	def set(value:Int) = setValue(id, value)
  }

  class ColorProperty(id:String,defaultValue:Color = defaultColor) extends Property[Color](id,defaultValue){
  	if(!isDefined(id)) setValue(id, defaultValue)
  	def get = getColorValue(id, defaultValue)
  	def set(value:Color) = setValue(id, value)
  }

  class BooleanProperty(id:String,defaultValue:Boolean = false) extends Property[Boolean](id,defaultValue){
  	if(!isDefined(id)) setValue(id, defaultValue)
  	def get = {
  		println("test before")
  		val v = getBooleanValue(id, defaultValue)
  		println("test after")
  		v
  	}
  	def set(value:Boolean) = setValue(id, value)
  }

}