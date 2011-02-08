package me.winsh.scalaedit.gui

import scala.swing._

abstract class ConsolePanel extends BorderPanel {

  abstract class ConsoleType

  case object ScalaConsole extends ConsoleType

  case object SSHConsole extends ConsoleType

  val consoleType: ConsoleType

}