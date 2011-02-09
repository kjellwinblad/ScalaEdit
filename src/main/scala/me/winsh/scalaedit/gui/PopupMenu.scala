package me.winsh.scalaedit.gui

import scala.swing._
import javax.swing._

class PopupMenu extends Component
{
  override lazy val peer : JPopupMenu = new JPopupMenu

  def add(item:MenuItem) : Unit = { peer.add(item.peer) }
  def addSeparator() : Unit = { peer.addSeparator() }
  def setVisible(visible:Boolean) : Unit = { peer.setVisible(visible) }

}