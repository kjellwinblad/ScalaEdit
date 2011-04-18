package me.winsh.scalaedit.api

sealed abstract class CodeNotification(val fileName:String,val line:Int, val message:String)

case class Error(override val fileName:String, override val line:Int, override val  message:String) extends CodeNotification(fileName, line, message)

case class Warning(override val fileName:String, override val line:Int, override val  message:String) extends CodeNotification(fileName, line, message)  