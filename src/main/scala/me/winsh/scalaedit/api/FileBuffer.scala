package me.winsh.scalaedit.api
import java.io.File


class FileBuffer(var file:Option[File]) {
	
	val name = file match {
		case None => "New File"
		case Some(file) => file.getName
	}

}