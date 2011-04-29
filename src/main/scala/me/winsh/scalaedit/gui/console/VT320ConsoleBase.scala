package me.winsh.scalaedit.gui.console

import java.io.InputStream
import java.io.OutputStream
import de.mud.terminal.vt320
import de.mud.terminal.SwingTerminal
import scala.swing._
import javax.swing.JPanel
import javax.swing.JScrollPane
import java.awt.BorderLayout
import me.winsh.scalaedit.gui._
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.InputMap
import javax.swing.KeyStroke
import javax.swing.JComponent
import java.awt.event.KeyEvent

trait VT320ConsoleBase extends ConsolePanel {

  trait InOutSource {
    def input: InputStream
    def output: OutputStream
  }

  private var invokeOnConsoleOutput = List[Int => Unit]()

  def addInvokeOnConsoleOutput(func: Int => Unit) {
    invokeOnConsoleOutput = func :: invokeOnConsoleOutput
  }

  def inOutSource: InOutSource

  private val linesInEmulator = 100

  private val emulation: vt320 = new vt320(80, linesInEmulator) {
    def write(b: Array[Byte]) {
      inOutSource.output.write(b)
    }
    setLocalEcho(false)
  }

  private val terminal = new SwingTerminal(emulation) {
    setResizeStrategy(SwingTerminal.RESIZE_SCREEN)
  }

  //Add popup menu with copy paste

  val popupMenu = (new PopupMenu() {
    add(new MenuItem(new Action("Copy") {

      icon = Utils.getIcon("/images/small-icons/copy-to-clipboard.png")

      def apply() {
        val text = terminal.getSelection
        if (text != null)
          Utils.clipboardContents = text
      }
    }))
    add(new MenuItem(new Action("Paste") {

      icon = Utils.getIcon("/images/small-icons/paste-from-clipboard.png")

      def apply() = inOutSource.output.write(Utils.clipboardContents.replaceAll("\t","  ").map(_.toByte).toArray)

    }))
  }).peer

  terminal.addMouseListener(new MouseAdapter() {
    override def mouseClicked(me: MouseEvent) {
      // if right mouse button clicked (or me.isPopupTrigger())
      if (javax.swing.SwingUtilities.isRightMouseButton(me)) {

        popupMenu.show(terminal, me.getX(), me.getY());
      }
    }
  })


  val scrollPane = new ScrollPane(Component.wrap(new JPanel() {
    setLayout(new BorderLayout())
    add(terminal, BorderLayout.CENTER)
  })) {
    horizontalScrollBarPolicy = ScrollPane.BarPolicy.Never
  }

  add(scrollPane, BorderPanel.Position.Center)

  private var running = false

  private def start() {

    running = true;

    Utils.runInNewThread(() => {

      while (running) {

        val b = inOutSource.input.read();

			  Utils.swingInvokeLater(() => invokeOnConsoleOutput.foreach(func => func(b)))
        //scrollPane.verticalScrollBar.value = scrollPane.verticalScrollBar.maximum
        b match {
          case -1 => running = false
          case 10 => {
            putString("" + 10.toChar + 13.toChar + " ")
            //scrollPane.horizontalScrollBar.value = scrollPane.horizontalScrollBar.maximum
            //scrollPane.verticalScrollBar.value = scrollPane.verticalScrollBar.maximum
          }
          case b => putString("" + b.toChar)
        }

      }
    })

  }

  start()

  /**
   * Stops the terminal
   */
  def stop() {

    inOutSource.output.write(-1)

    running = false;

  }

  /**
   * puts a string to the terminal and move the input position to the next line
   * @param str
   */
  def putString(str: String) = Utils.swingInvokeLater(() => {
    emulation.putString(str)
    scrollPane.verticalScrollBar.value = scrollPane.verticalScrollBar.maximum
  })

  /**
   * puts a string to the terminal and move the input position to the next line
   * @param str
   */
  def putLine(str: String) {
  	putString(str + 10.toChar + 13.toChar) 
  }

	putLine(" Loading...")

  for (_ <- 0 to (linesInEmulator - 8)) { putLine("") }
  
  putLine(" Loading...")

	putLine("")
  
  putString(" ")

  //Disable arrow keys in scroll pane

  def disableArrowKeys(im: InputMap) {
	  
    List(KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT).foreach(keyStrokName => {
      im.put(KeyStroke.getKeyStroke(keyStrokName,0), "none");
    })

  }
  disableArrowKeys(scrollPane.peer.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT));

}