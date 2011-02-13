package me.winsh.scalaedit.gui

import scala.swing._
import me.winsh.scalaedit.api.Closeable

abstract class ConsolePanel extends BorderPanel with Closeable{

  abstract class ConsoleType

  case object ScalaConsole extends ConsoleType

  case object SSHConsole extends ConsoleType

  val consoleType: ConsoleType

}