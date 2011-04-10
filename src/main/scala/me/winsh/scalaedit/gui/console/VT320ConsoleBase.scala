package me.winsh.scalaedit.gui.console

import java.io.InputStream
import java.io.OutputStream
import de.mud.terminal.vt320
import de.mud.terminal.SwingTerminal
import scala.swing._
import javax.swing.JPanel
import java.awt.BorderLayout
import me.winsh.scalaedit.gui._

trait VT320ConsoleBase extends ConsolePanel {

  trait InOutSource {
    def input: InputStream
    def output: OutputStream
  }
  
  private var invokeOnConsoleOutput = List[Int=>Unit]()

  def addInvokeOnConsoleOutput(func:Int=>Unit){
	  invokeOnConsoleOutput = func::invokeOnConsoleOutput
  }
  
  def inOutSource: InOutSource

  private val emulation: vt320 = new vt320() {
    def write(b: Array[Byte]) {
      inOutSource.output.write(b)
    }
    setLocalEcho(false)
  }

  private val terminal = new SwingTerminal(emulation) {
    setResizeStrategy(SwingTerminal.RESIZE_SCREEN)
  }

  private class TerminalWrapper extends Component {
    private def panel = new JPanel() {
      setLayout(new BorderLayout())
      add(terminal, BorderLayout.CENTER)
    }

    override lazy val peer: JPanel = panel
  }

  add(new TerminalWrapper(), BorderPanel.Position.Center)

  private var running = false

  private def start() {

    running = true;

    Utils.runInNewThread(() => {

      while (running) {

        val b = inOutSource.input.read();

        invokeOnConsoleOutput.foreach(func => func(b))
        
        b match {
          case -1 => running = false
          case 10 => emulation.putString("" + 10.toChar + 13.toChar)
          case b => emulation.putString("" + b.toChar)
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
  def putLine(str:String){
	  emulation.putString(str + 10.toChar + 13.toChar)
  }

}