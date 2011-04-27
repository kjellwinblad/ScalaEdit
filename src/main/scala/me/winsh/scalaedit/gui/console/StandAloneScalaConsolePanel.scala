package me.winsh.scalaedit.gui.console

import java.io.InputStream
import java.io.OutputStream
import scala.tools.nsc.interpreter.ILoop
import org.apache.tools.ant.util.ReaderInputStream
import java.io.BufferedReader
import java.io.Reader
import java.io.InputStreamReader
import me.winsh.scalaedit.gui._
import scala.swing._
import javax.swing._
import de.mud.terminal.vt320
import de.mud.terminal.SwingTerminal
import java.awt.BorderLayout
import java.io.InputStreamReader
import java.io.BufferedReader
import de.mud.jta.plugin.Terminal
import de.mud.jta.PluginBus
import de.mud.jta.PluginLoader
import de.mud.jta.FilterPlugin
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import me.winsh.scalaedit.gui._
import me.winsh.scalaedit.gui.editor._
import me.winsh.scalaedit.api._
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.io.File
import java.io.BufferedInputStream
import java.io.FileOutputStream
import scala.util.matching.Regex
import scala.annotation.tailrec


class StandAloneScalaConsolePanel extends VT320ConsoleBase {
  val consoleType = StandAloneScalaConsole

  private class ScalaProcess extends InOutSource {

    val process = {

			try{

				val javaFile = new File(new File(System.getProperty("java.home"), "bin"), "java")

        val javaPath = javaFile.exists match {
          case true => javaFile.getAbsolutePath
          case false => "java"
        }
				
				val classPath = System.getProperty("java.class.path")

        val pb = new ProcessBuilder(javaPath, "-cp", classPath, "scala.tools.nsc.MainInterpreter", "-usejavacp")

        pb.directory(Utils.projectDir)

        pb.start()

      } catch {
        case e => {
          Utils.showErrorMessage(message = "<html>Could not lunch Scala process. Trying to lunch command \"scala\". <br/>" + e.getMessage())
          e.printStackTrace()
          null
        }
      }

    }
    private val in = process.getInputStream
    private val err = process.getErrorStream
    val out = process.getOutputStream
    private val onlyOneStreamLeft = new AtomicBoolean(false)

    private val readQueue = new ArrayBlockingQueue[Int](1024, false)

    val input = new InputStream {

      @tailrec
      def readFromStream(in: InputStream) {

          in.read() match {
            case -1 if (onlyOneStreamLeft.get()) => readQueue.put(-1)
            case -1 => onlyOneStreamLeft.set(true)
            case v => {
              readQueue.put(v)
              readFromStream(in) //Recursive call
            }
          }
      }

      Utils.runInNewThread(() => try{readFromStream(in)}catch{case _ =>})
      Utils.runInNewThread(() => try{readFromStream(err)}catch{case _ =>})

      def read(): Int = readQueue.take()

    }

    val output = new OutputStream {
      def write(toWrite: Int) = { out.write(toWrite); out.flush() }
    }

  }

  private var sbtProcess: ScalaProcess = null

  def inOutSource: InOutSource = {
    if (sbtProcess == null)
      sbtProcess = new ScalaProcess()

    sbtProcess
  }

  def close() = {

    try {
      sbtProcess.out.write("\n\nsys.exit()\n".map(_.toByte).toArray)
      sbtProcess.out.flush()
      stop()
      //In case it has not terminated with the exit command
      sbtProcess.process.destroy()
    } catch {
      case _ => //Ignore, this could be that the stream is closed or similar
    }
    true
  }


}