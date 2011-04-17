package me.winsh.scalaedit.api

sealed abstract case class CodeNotification(fileName:String,line:Int)

case class Error(override val fileName:String, override val line:Int, message:String) extends CodeNotification(fileName, line)

case class Warning(override val fileName:String, override val line:Int, message:String) extends CodeNotification(fileName, line)  