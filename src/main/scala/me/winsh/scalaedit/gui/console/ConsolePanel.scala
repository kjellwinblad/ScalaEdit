package me.winsh.scalaedit.gui.console

import scala.swing._
import me.winsh.scalaedit.api.Closeable

abstract class ConsolePanel extends BorderPanel with Closeable{

  abstract class ConsoleType

  case object ScalaConsole extends ConsoleType

  case object StandAloneScalaConsole extends ConsoleType
  
  case object SBTConsole extends ConsoleType

  case object SSHConsole extends ConsoleType

  val consoleType: ConsoleType

	preferredSize = new Dimension(700,200)

}