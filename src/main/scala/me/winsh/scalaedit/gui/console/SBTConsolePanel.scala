package me.winsh.scalaedit.gui.console

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

class SBTConsolePanel extends VT320ConsoleBase {

  val consoleType = SBTConsole

  private class SBTProcess extends InOutSource {

    val process = {
      //Check if SBT is in properties dir

      val sbtJarName = "sbt-launch-0.7.6.RC0.jar"

      try {
        val javaFile = new File(new File(System.getProperty("java.home"), "bin"), "java")

        val javaPath = javaFile.exists match {
          case true => javaFile.getAbsolutePath
          case false => "java"
        }

        val sbtJarFile = new File(new File(Utils.propertiesDir, "bin"), sbtJarName)
        if (!sbtJarFile.exists) {

          putLine("Extracting " + sbtJarName + " to " + sbtJarFile.getAbsolutePath + " ...")

          new File(Utils.propertiesDir, "bin").mkdirs()
          sbtJarFile.createNewFile()

          val sbtStream = new BufferedInputStream(this.getClass.getResourceAsStream("/bin/" + sbtJarName))
          val outStream = new FileOutputStream(sbtJarFile)

          def readWrite(): Unit = sbtStream.read() match {
            case -1 => outStream.close()
            case r => {
              outStream.write(r)
              readWrite()
            }
          }

          readWrite()
          sbtStream.close()

        }

        val pb = new ProcessBuilder(javaPath, "-cp", sbtJarFile.getAbsolutePath, "xsbt.boot.Boot");

        pb.directory(Utils.projectDir)

        pb.start()

      } catch {
        case e => {
          Utils.showErrorMessage(message = "<html>Could not lunch SBT. Trying to lunch command \"sbt\". <br/>" + e.getMessage())
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

  private var sbtProcess: SBTProcess = null

  def inOutSource: InOutSource = {
    if (sbtProcess == null)
      sbtProcess = new SBTProcess()

    sbtProcess
  }

  def close() = {

    try {
      sbtProcess.out.write("\n\nexit\n".map(_.toByte).toArray)
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