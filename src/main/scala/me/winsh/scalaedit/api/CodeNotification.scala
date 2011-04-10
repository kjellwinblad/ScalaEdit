package me.winsh.scalaedit.api

sealed abstract case class CodeNotification(fileName:String)

case class Error(override val fileName:String,line:Int, message:String) extends CodeNotification(fileName)

case class Warning(override val fileName:String,line:Int, message:String) extends CodeNotification(fileName)