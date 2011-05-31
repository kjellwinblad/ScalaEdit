/*
ScalaEdit - A text editor for Scala programmers
Copyright (C) 2011  Kjell Winblad (kjellwinblad@gmail.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
*/

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
import scala.collection.JavaConversions._

class StandAloneScalaConsolePanel extends VT320ConsoleBase {
  val consoleType = StandAloneScalaConsole

  private class ScalaProcess extends InOutSource {

    val process = {

      val properties = new StandAloneScalaConsolePanelProperties()

      try {

        val javaFile = new File(new File(System.getProperty("java.home"), "bin"), "java")

        val javaPath = javaFile.exists match {
          case true => javaFile.getAbsolutePath
          case false => "java"
        }

        val standardClassPath = System.getProperty("java.class.path")

        def propCPStringToCPString(s: String) = s.split("""\s|:""").filter(_.size > 0).map(_.trim).mkString(":")

        val classPath = (standardClassPath +
          (if (properties.javaClasspath.get.trim == "") "" else ":" + propCPStringToCPString(properties.javaClasspath.get)))

        echoInput = properties.echoInput.get

        val args: java.util.List[String] =
          javaPath ::
            properties.javaVMArguments.get.split("""\s""").toList.filter(_.size > 0) :::
            "-cp" ::
            classPath ::
            "scala.tools.nsc.MainGenericRunner" ::
            "-usejavacp" ::
            (if (properties.arguments.get.size == 0) List[String]()
            else properties.arguments.get.split(" ").toList) ::: Nil

        val pb =
          new ProcessBuilder(args)

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

      Utils.runInNewThread(() => try { readFromStream(in) } catch { case _ => })
      Utils.runInNewThread(() => try { readFromStream(err) } catch { case _ => })

      def read(): Int = readQueue.take()

    }

    val output = new OutputStream {
      def write(toWrite: Int) = { out.write(toWrite); out.flush() }
    }

  }

  private var scalaProcess: ScalaProcess = null

  def inOutSource: InOutSource = {
    if (scalaProcess == null)
      scalaProcess = new ScalaProcess()

    scalaProcess
  }

  def close() = {
    try {
      scalaProcess.out.write("\n\nsys.exit()\n".map(_.toByte).toArray)
      scalaProcess.out.flush()
      stop()
    } catch {
      case _ => //Ignore, this could be that the stream is closed or similar
    }
    finally {
      //Make sure that it really is dead
      scalaProcess.process.destroy()
    }
    true
  }

}