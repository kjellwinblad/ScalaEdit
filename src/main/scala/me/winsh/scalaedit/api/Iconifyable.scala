package me.winsh.scalaedit.api

import javax.swing.Icon

trait Iconifyable {
	def icon_=(icon:Icon)
	def icon:Icon
}