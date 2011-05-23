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
import java.awt.event.InputEvent
import java.awt.event.FocusListener
import java.awt.event.FocusEvent

trait VT320ConsoleBase extends ConsolePanel {

  var echoInput = false

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
      if (echoInput) {
        //Replace the byte 10 or 13 with 10,13
        val echoString = b.map((i) => if (i == 10 || i == 13) Array(10.toChar, 13.toChar, ' ') else Array(i.toChar)).flatten.mkString("")
        putString(echoString)
      }
      //Thread.sleep(1000)
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

      def apply() = inOutSource.output.write(Utils.clipboardContents.replaceAll("\t", "  ").map(_.toByte).toArray)

    }))

    addSeparator()

    val standardInput = new RadioMenuItem("") {
      action = new Action("Standard Input Method") {
        def apply() {
          setToStandardInput()
        }
      }
    }

    val multilineInput = new RadioMenuItem("") {
      action = new Action("Multiline Input Method") {
        def apply() {
          setToMultilineInput()
        }
      }
    }

    val inputMethodSelect = new ButtonGroup(standardInput, multilineInput)

    inputMethodSelect.select(standardInput)

    add(standardInput)
    add(multilineInput)

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
    preferredSize = new Dimension(450, 700)
  }

  def setToStandardInput() {
    add(scrollPane, BorderPanel.Position.Center)
    revalidate()
  }

  def setToMultilineInput() {

    val inputPanel = new BorderPanel {

      val inputArea: TextArea = new TextArea() {
        peer.addFocusListener(new FocusListener() {
          def focusGained(e: FocusEvent) {
            peer.getKeymap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK), enterAction.peer)
          }

          def focusLost(e: FocusEvent) {}
        })
      }

      val enterAction = new Action("Enter") {
        mnemonic = KeyEvent.VK_E
        accelerator = Some(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK))
        toolTip = "<html>Send to to terminal <i>(control+ENTER)</i>"
        def apply() {
          emulation.write((inputArea.text.replaceAll("\t", "  ") + "\n").map(_.toByte).toArray)
          inputArea.text = ""
          inputArea.requestFocus()
        }
      }

      add(new ScrollPane(inputArea), BorderPanel.Position.Center)

      add(new Button() {
        action = enterAction
        //preferredSize = new Dimension(450,20)
      }, BorderPanel.Position.East)
    }
    val splitPane = new SplitPane(Orientation.Horizontal, left = scrollPane, right = inputPanel)

    splitPane.resizeWeight = 1.0

    splitPane.preferredSize = new Dimension(300, 300)

    splitPane.dividerLocation = 0.9

    add(splitPane, BorderPanel.Position.Center)

    revalidate()

    scrollPane.verticalScrollBar.value = scrollPane.verticalScrollBar.maximum

    inputPanel.requestFocus()

  }

  if (System.getProperty("os.name").toLowerCase.contains("windows"))
    setToMultilineInput()
  else
    setToStandardInput()

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

  def putStringAndWait(str: String) = Utils.swingInvokeAndWait(() => {
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

  for (_ <- 0 to (linesInEmulator + 100)) { putLine("") }

  putLine(" Loading...")

  putLine("")

  if (System.getProperty("os.name").toLowerCase.contains("windows")) {
    putLine("OBS! Direct input to the terminal is known to work badly in Windows.")
    putLine("Windows users are therefore recommended to use the multiline input method.")
    putLine("This is because of a problem with JLine which is used in Scala and sbt.")
  }

  putLine("")

  putString(" ")

  //Disable arrow keys in scroll pane

  def disableArrowKeys(im: InputMap) {

    List(KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT).foreach(keyStrokName => {
      im.put(KeyStroke.getKeyStroke(keyStrokName, 0), "none");
    })

  }
  disableArrowKeys(scrollPane.peer.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT));

}