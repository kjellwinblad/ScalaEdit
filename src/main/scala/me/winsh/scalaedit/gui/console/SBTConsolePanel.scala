/*
ScalaEdit - A text editor for Scala programmers
Copyright (C) 2011  Kjell Winblad (kjellwinblad@gmail.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
*/

package me.winsh.scalaedit.gui.console

import java.awt.Font
import scala.swing._
import javax.swing.JToolBar
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
import scala.collection.JavaConversions._
import javax.swing.KeyStroke
import java.awt.event.InputEvent

class SBTConsolePanel(sbtVersion: String = "0.7.7") extends VT320ConsoleBase {

  val consoleType = SBTConsole

  private class SBTProcess extends InOutSource {

    val properties = new SBTConsolePanelProperties()

    terminal.setFont(new Font(terminal.getFont().getName(),
      terminal.getFont().getStyle(),
      properties.textSize.get))

    val process = {
      //Check if SBT is in properties dir

      val sbtJarName = "sbt-launch-" + sbtVersion + ".jar"

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

        echoInput = properties.echoInput.get

        val args: java.util.List[String] =
          javaPath ::
            properties.javaVMArguments.get.split("""\s""").toList.filter(_.size > 0) :::
            "-cp" ::
            sbtJarFile.getAbsolutePath ::
            "xsbt.boot.Boot" ::
            (if (properties.arguments.get.size == 0) List[String]()
            else properties.arguments.get.split(" ").toList) ::: Nil

        val pb = new ProcessBuilder(args)

        pb.directory(properties.startingDir.get)

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

      Utils.runInNewThread(() => try { readFromStream(in) } catch { case _ => })
      Utils.runInNewThread(() => try { readFromStream(err) } catch { case _ => })

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

    } catch {
      case _ => //Ignore, this could be that the stream is closed or similar
    }
    finally {
      //In case it has not terminated with the exit command
      sbtProcess.process.destroy()
    }
    true
  }

  def run() {
    emulation.write("\n\nrun\n".map(_.toByte).toArray)
  }

  object toolBar extends JToolBar {

    def add(a: Action) = super.add(a.peer)

    add(new Action("") {
      accelerator = Some(KeyStroke.getKeyStroke('R', InputEvent.CTRL_DOWN_MASK))
      toolTip = "<html>SBT Run Action (<i>F8, Use F7 to select the toolbar</i>)"
      icon = Utils.getIcon("/images/small-icons/play.png")
      def apply() {
        run()
      }
    })

    add(new Action("") {
      toolTip = "SBT Compile Action"
      icon = Utils.getIcon("/images/completions/class.png")
      def apply() {
        emulation.write("\n\ncompile\n".map(_.toByte).toArray)
      }
    })

    add(new Action("") {
      toolTip = "SBT Test Action"
      icon = Utils.getIcon("/images/small-icons/illustrations/test.png")
      def apply() {
        emulation.write("\n\ntest\n".map(_.toByte).toArray)
      }
    })

    add(new JToolBar.Separator)

    add(new Action("") {
      toolTip = "SBT Continuous Compile Action"
      icon = Utils.getIcon("/images/small-icons/illustrations/compilecontinus.png")
      def apply() {
        emulation.write("\n\n~compile\n".map(_.toByte).toArray)
      }
    })

    add(new Action("") {
      toolTip = "SBT Console Action"
      icon = Utils.getIcon("/images/small-icons/illustrations/scala-terminal.png")
      def apply() {
        emulation.write("\n\nconsole\n".map(_.toByte).toArray)
      }
    })

    add(new JToolBar.Separator)

    add(new Action("") {
      toolTip = "SBT Help Action"
      icon = Utils.getIcon("/images/small-icons/illustrations/help.png")
      def apply() {
        emulation.write("\n\nhelp\n".map(_.toByte).toArray)
      }
    })

    override def requestFocusInWindow() =
      getComponentAtIndex(0).requestFocusInWindow()

  }

  add(Component.wrap(toolBar), BorderPanel.Position.North)
}