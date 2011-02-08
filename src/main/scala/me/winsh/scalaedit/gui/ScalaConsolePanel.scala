package me.winsh.scalaedit.gui

import scala.swing._
import javax.swing.SwingUtilities
import java.awt.event._
import java.io.PrintWriter
import java.io.OutputStream
import java.io.BufferedReader
import java.io.Reader
import java.io.PipedReader
import java.io.PipedWriter
import java.io.BufferedWriter
import scala.tools.nsc.Interpreter
import scala.tools.nsc.InterpreterLoop
import scala.tools.nsc.InterpreterResults
import scala.tools.nsc.Settings
import scala.collection.mutable.ArrayBuffer

class ScalaConsolePanel extends ConsolePanel {

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

  editorPane.getCaret.setVisible(true)

  val interpreter = createInterpreter()

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
        case VK_DOWN if (historyPos < historyBuffer.size -1) => {
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
          historyBuffer(historyBuffer.size - 1) = lastLine.toString
          historyBuffer.append("")
          historyPos = historyBuffer.size - 1

          lastLine.append(charToInsert)
          modifyTextInTextPane(prevLength)
          modifyCaretPos(e)

          if (lastLine.toString.trim == ":history") {
            val historyString = historyBuffer.foldLeft("")((sum, next) => sum + "\n" + next)
            editorPane.setText(editorPane.getText + historyString + "\n\nscala> ")
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

  private def createInterpreter(): Interpreter = {

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

    val interpreterLoop = new InterpreterLoop(new BufferedReader(new PipedReader(pipedConsoleWriter)), new PrintWriter(new OutputStream {

      def write(b: Int) {
        SwingUtilities.invokeLater(new Runnable {
          def run {
            val text = editorPane.getText
            editorPane.setText(text + b.toChar)
          }
        })
      }

    }))

    new Thread(new Runnable {
      def run {

        interpreterLoop.main(settings)
      }
    }).start()

    interpreterLoop.interpreter
  }

}