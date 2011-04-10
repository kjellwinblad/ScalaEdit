package me.winsh.scalaedit.gui.console

import scala.swing._
import javax.swing.JOptionPane
import javax.swing.SwingUtilities
import javax.swing.event.CaretListener
import javax.swing.event.CaretEvent
import java.awt.event._
import java.io.PrintWriter
import java.io.OutputStream
import java.io.BufferedReader
import java.io.Reader
import java.io.PipedReader
import java.io.PipedWriter
import java.io.BufferedWriter
import scala.tools.nsc.Interpreter
import scala.io.Source
import scala.tools.nsc.InterpreterLoop
import scala.tools.nsc.InterpreterResults
import scala.tools.nsc.Settings
import scala.collection.mutable.ArrayBuffer
import org.apache.tools.ant.util.ReaderInputStream
import me.winsh.scalaedit.gui._

class ScalaConsolePanel extends ConsolePanel { 

  val terminalStdOut = new OutputStream {

    def write(b: Int) {

      Utils.swingInvokeLater(() => {
        val textTmp = editorPane.getText

        val text = if (textTmp == null) "" else textTmp

        editorPane.setText(text + b.toChar)
      })

    }

  }

  val historyBuffer = ArrayBuffer("")

  var historyPos = 0

  val pipedConsoleWriter = new PipedWriter()

  val consoleWriter = new BufferedWriter(pipedConsoleWriter)

  val consoleType = ScalaConsole

  private var inputPos = 0

  private var lastLine = new StringBuffer("")

  private val advancedPane = new EditorPaneWrapper()

  val editorPane = advancedPane.editorPane

  val scrollPane = advancedPane.peer

  add(advancedPane, BorderPanel.Position.Center)

  editorPane.setContentType("text/scala")

  editorPane.setEditable(false)

  editorPane.setComponentPopupMenu((new PopupMenu() {
    add(new MenuItem(new Action("Copy") {

      icon = Utils.getIcon("/images/small-icons/copy-to-clipboard.png")

      def apply() {
        val text = editorPane.getSelectedText()
        if (text != null)
          Utils.clipboardContents = text
      }
    }))
    add(new MenuItem(new Action("Paste") {

      icon = Utils.getIcon("/images/small-icons/paste-from-clipboard.png")

      def apply() = writeToConsole(Utils.clipboardContents)

    }))
  }).peer)

  editorPane.getCaret.setVisible(true)

  var interpreterLoop = createInterpreter()

  def close() = {
    val line = "\n\n\n:quit\n"
    consoleWriter.write(line, 0, line.length)
    consoleWriter.flush()

    true
  }

  def writeToConsole(toWrite: String) {

    val endsWithNewLine = toWrite.toString.length == 0 && toWrite.toString.endsWith("\n")

    val components = Source.fromString(toWrite).getLines().toList

    val finalRows = if (endsWithNewLine) components else components.reverse.tail.reverse

    def insertLastInConsole(string: String) = Utils.swingInvokeLater(() => {
      val text = editorPane.getText
      editorPane.setText(text + string)
    })

    if (finalRows.size > 0) {
      finalRows.foreach { (line) =>
        insertLastInConsole(line + "\n")
        consoleWriter.write(line + "\n", 0, line.length + 1)
        consoleWriter.flush()
        Thread.sleep(30)

      }
    }

    if (!endsWithNewLine) {
      lastLine = new StringBuffer(components.last)
      inputPos = 0

      insertLastInConsole(lastLine.toString)
    }
  }

  editorPane.addKeyListener(new KeyAdapter() {

    def modifyCaretPos(e: KeyEvent) {
      editorPane.setCaretPosition(editorPane.getText.length - inputPos)
      e.consume()
      editorPane.getCaret.setVisible(true)
    }

    def modifyTextInTextPane(prevInLineLength: Int) {
      val text = editorPane.getText
      val prevTextSize = prevInLineLength
      val modifiedText = (if (text == null) "" else text).substring(0, text.length - prevTextSize) + lastLine
      editorPane.setText(modifiedText)
    }

    override def keyPressed(e: KeyEvent) {
      import KeyEvent._

      def modifyHistoryPos() {
        val prevLength = lastLine.length
        lastLine = new StringBuffer(historyBuffer(historyPos))
        inputPos = 0
        modifyTextInTextPane(prevLength)
      }

      e.getKeyCode match {
        case VK_UP if (historyPos != 0) => {
          historyPos = historyPos - 1
          modifyHistoryPos()
          modifyCaretPos(e)
        }
        case VK_UP => modifyCaretPos(e)
        case VK_DOWN if (historyPos < historyBuffer.size - 1) => {
          historyPos = historyPos + 1
          modifyHistoryPos()
          modifyCaretPos(e)
        }
        case VK_DOWN => modifyCaretPos(e)
        case VK_LEFT => {

          if (inputPos < lastLine.length)
            inputPos = inputPos + 1
          modifyCaretPos(e)
        }
        case VK_RIGHT => {
          if (inputPos != 0)
            inputPos = inputPos - 1
          modifyCaretPos(e)
        }
        case VK_HOME => {
          inputPos = lastLine.length
          modifyCaretPos(e)
        }
        case VK_END => {
          inputPos = 0
          modifyCaretPos(e)
        }
        case _ => {

        }
      }

    }

    override def keyTyped(e: KeyEvent) {

      val charToInsert = e.getKeyChar

      import KeyEvent._

      val prevLength = lastLine.length

      charToInsert match {
        case VK_ENTER => {
          if (lastLine.length != 0) {
            historyBuffer(historyBuffer.size - 1) = lastLine.toString
            historyBuffer.append("")
            historyPos = historyBuffer.size - 1
          }
          lastLine.append(charToInsert)
          modifyTextInTextPane(prevLength)
          modifyCaretPos(e)

          if (lastLine.toString.trim == ":history") {
            val historyString = historyBuffer.foldLeft("")((sum, next) => sum + "\n" + next)
            editorPane.setText(editorPane.getText + historyString.tail + "\nscala> ")
          } else {
            consoleWriter.write(lastLine.toString, 0, lastLine.length)
            consoleWriter.flush()
          }
          lastLine = new StringBuffer("")
          inputPos = 0

        }
        case VK_BACK_SPACE => {
          val deletePos = lastLine.length - inputPos - 1
          if (deletePos >= 0)
            lastLine.deleteCharAt(deletePos)
          modifyTextInTextPane(prevLength)
          modifyCaretPos(e)
        }
        case VK_DELETE => {
          if (inputPos != 0) {
            lastLine.deleteCharAt(lastLine.length - inputPos)
            inputPos = inputPos - 1
          }
          modifyTextInTextPane(prevLength)
          modifyCaretPos(e)
        }
        case _ => {
          lastLine.insert(lastLine.length - inputPos, charToInsert)
          modifyTextInTextPane(prevLength)
          modifyCaretPos(e)
        }
      }

    }

  })

  editorPane.addMouseListener(new MouseAdapter() {

    def modifyCaretPos() {
      editorPane.setCaretPosition(editorPane.getText.length - inputPos)
      editorPane.getCaret.setVisible(true)
    }

    override def mousePressed(e: MouseEvent) {

      editorPane.getCaret.setVisible(false)

    }
    override def mouseReleased(e: MouseEvent) {

      if (editorPane.getCaret.getMark == editorPane.getCaret.getDot)
        modifyCaretPos()
      else {
        editorPane.getCaret.setVisible(false)
      }
    }

  })

  private def createInterpreter(): InterpreterLoop = {

    def jarPathOfClass(className: String) = {
      val resource = className.split('.').mkString("/", "/", ".class")
      val path = getClass.getResource(resource).getPath
      val indexOfFile = path.indexOf("file:")
      val indexOfSeparator = path.lastIndexOf('!')
      path.substring(indexOfFile, indexOfSeparator)
    }

    val compilerPath = jarPathOfClass("scala.tools.nsc.Interpreter")
    val libPath = jarPathOfClass("scala.ScalaObject")

    val settings = new scala.tools.nsc.Settings()

    settings.usejavacp.value = true

    val origBootclasspath = settings.bootclasspath.value
    val pathList = List(compilerPath, libPath)

    settings.bootclasspath.value = (origBootclasspath :: pathList).mkString(java.io.File.separator)

    var interLoop: InterpreterLoop = null

    Utils.runInNewThread(() => {

      val reader = new BufferedReader(new PipedReader(pipedConsoleWriter))

      interLoop = new InterpreterLoop(reader, new PrintWriter(new OutputStream {
        var initTextCounter = 0
        var initDone = false
        var hasGotInputChar = false

        def write(b: Int) {

          Utils.swingInvokeLater(() => {
            var text = editorPane.getText

            text = if (text == null) "" else text

            if (initDone == false && (text == "" || text.endsWith("\n"))) {

              initTextCounter = initTextCounter + 1

              if (initTextCounter <= 3)
                text = text + "//"

            }

            editorPane.setText(text + b.toChar)
          })

          if (("" + b.toChar) == ">")
            hasGotInputChar = true

          if (initDone == false && hasGotInputChar && ("" + b.toChar) == " ") {

            //interpreterLoop.injectOne("in", new ReaderInputStream(reader))
            interLoop.injectOne("out", terminalStdOut)
            interLoop.interpreter.interpret("Console.setOut(out)")
            interLoop.interpreter.interpret("Console.setErr(out)")
            //interLoop.interpreter.interpret("Console.setIn(in)")
            initDone = true
          }

        }

      }))

      interLoop.main(settings)

    })

    //Thread.sleep(10000)
    interLoop
  }

}