/*
ScalaEdit - A text editor for Scala programmers
Copyright (C) 2011  Kjell Winblad (kjellwinblad@gmail.com)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
*/

package me.winsh.scalaedit.gui.editor

import me.winsh.scalaedit.api._
import scala.swing._
import javax.swing._
import javax.swing.text._
import javax.swing.event._
import scala.swing.event._
import org.fife.ui.rtextarea._
import org.fife.ui.rsyntaxtextarea._
import java.awt.event.KeyEvent
import java.awt.event.InputEvent
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import java.io.File
import java.awt.event.FocusListener
import java.awt.event.FocusEvent
import scala.swing.Action
import me.winsh.scalaedit.gui._
import javax.swing.text.BadLocationException
import scala.util.matching._
import javax.swing.KeyStroke
import java.awt.Font

class EditorPanel(val fileBuffer: FileBuffer, val tabComponent: TabComponent) extends BorderPanel with Closeable {

  private val properties = new EditorPanelProperties()

  private val colorProperties = new EditorPanelStandardColorsProperties()

  private val syntaxProperties = new SyntaxHighlightingProperties()

  private var savedVar = true
  private val saveAsIcon = Utils.getIcon("/images/small-icons/actions/filesaveas.png")
  private val saveIcon = Utils.getIcon("/images/small-icons/actions/filesave.png")

  def saved_=(isSaved: Boolean) {
    savedVar = isSaved
    saveAction.enabled = !isSaved
    if (isSaved) {
      changeToSavedIcon()
    } else {
      tabComponent.icon = saveAsIcon
    }
  }

  def saved = savedVar

  fileBuffer.file match {
    case None => saved = false
    case Some(_) => saved = true
  }

  val editorPane = new RSyntaxTextArea() {

    setSyntaxScheme(syntaxProperties.schemeFromProps(properties.font))

    setForeground(colorProperties.defaultTextColor.get)

    setBackground(colorProperties.backgroundColor.get)

    setCurrentLineHighlightColor(colorProperties.currentLineColor.get)

    setSelectionColor(colorProperties.selectionColor.get)

    setHighlightCurrentLine(colorProperties.currentLineColorEnabled.get)

    setCaretColor(colorProperties.caretColor.get)

    override protected def createPopupMenu() = (new PopupMenu() {
      add(new MenuItem(cutAction))
      add(new MenuItem(copyAction))
      add(new MenuItem(pasteAction))
      addSeparator()
      add(new MenuItem(selectAllAction))
    }).peer
  }

  editorPane.setSyntaxEditingStyle(syntaxStyleFromContentType(fileBuffer.contentType))

  editorPane.setTabSize(properties.tabLengthInSpaces.get)

  val scrollPane = new RTextScrollPane(editorPane);

  private class EditorWrapper(val scroller: RTextScrollPane) extends Component {
    override lazy val peer: JScrollPane = scroller
  }
  private val editorWrapper = new EditorWrapper(scrollPane)

  private class EditorAndToolArea extends BorderPanel {

    private var currentComponent: Option[Component] = None

    private var removeAction: Option[Unit => Unit] = None

    def setTool(component: Component, onRemove: Unit => Unit) {
      setTool(component)
      removeAction = Some(onRemove)
    }

    def setTool(component: Component) {
      removeTool()
      add(component, BorderPanel.Position.North)
      peer.revalidate()
      currentComponent = Some(component)
    }
    def removeTool() {

      if (removeAction != None)
        (removeAction.get)()

      currentComponent match {
        case Some(c) => {
          peer.remove(c.peer)
          peer.revalidate()
          currentComponent = None
        }
        case None =>
      }
    }

    add(editorWrapper, BorderPanel.Position.Center)
  }
  private val editorAndToolArea = new EditorAndToolArea()

  add(editorAndToolArea, BorderPanel.Position.Center)

  object toolBar extends JToolBar {
    def add(a: Action) = super.add(a.peer)
    def add(a: ToggleButton) = super.add(a.peer)

    add(saveAction)
    add(saveAsAction)
    add(new JToolBar.Separator)
    add(undoAction)
    add(redoAction)
    add(new JToolBar.Separator)
    add(searchAction)
    add(gotoLineAction)
    //add(new JToolBar.Separator)
    //add(cutAction)
    //add(copyAction)
    //add(pasteAction)
    add(new JToolBar.Separator)
    add(new ToggleButton() {
      action = wrapLinesAction
      if (properties.wrapLines.get) {
        selected = true
        wrapLinesAction()
      } else selected = false

    })
  }

  add(Component.wrap(toolBar), BorderPanel.Position.North)

  //load the content into the editor 
  try {

    val car = editorPane.getCaret().asInstanceOf[DefaultCaret]
    val policy = car.getUpdatePolicy()
    car.setUpdatePolicy(DefaultCaret.NEVER_UPDATE)
    val content = fileBuffer.content
    editorPane.setText(content)
    car.setUpdatePolicy(policy)
  } catch {
    case e => {
      Dialog.showMessage(message = "Error when loading file:\n" +
        "Error: " + e.getMessage,
        title = "Could Not Read File")
      editorPane.setEnabled(false)
    }
  }
  //Listen for changes
  editorPane.getDocument().addDocumentListener(new DocumentListener() {
    def changedUpdate(e: DocumentEvent) { saved = false }
    def insertUpdate(e: DocumentEvent) { changedUpdate(e) }
    def removeUpdate(e: DocumentEvent) { changedUpdate(e) }
  });

  //Listen for save action etc
  object saveAction extends Action("Save") {
    icon = saveIcon
    toolTip = "<html>Save <i>(control+s)</i>"
    def apply() {
      fileBuffer.file match {
        case Some(_) => {
          try {
            fileBuffer.content = editorPane.getText
            saved = true
          } catch {
            case e => Dialog.showMessage(message = "Error when trying to save file.\nError: %s".format(e.getMessage), title = "Could Not Save File")
          }
        }
        case None => {
          saveAsAction()
        }
      }
    }
  }

  object saveAsAction extends Action("Save As...") {

    toolTip = "Save As..."

    icon = saveAsIcon

    def apply() {

      val (dirSuggestion, fileSuggestion) = fileBuffer.file match {
        case Some(f) => (f.getParentFile, f.getName)
        case None => (Utils.bestFileChooserDir, "")
      }

      //Display file chooser
      val chooser = new FileChooser(dirSuggestion) {
        title = "Save File As"
        multiSelectionEnabled = false
        if (fileSuggestion != "")
          selectedFile = new File(dirSuggestion, fileSuggestion)

        fileSelectionMode = FileChooser.SelectionMode.FilesOnly
      }

      chooser.showSaveDialog(null)

      val sFile = if (chooser.selectedFile == null) None else Some(chooser.selectedFile)

      sFile.foreach(file => {
        fileBuffer.file = Some(file)
        tabComponent.name = file.getName
        saveAction()
        editorPane.setSyntaxEditingStyle(syntaxStyleFromContentType(fileBuffer.contentType))
      })

    }
  }
  //REDO_ACTION, ROUNDED_SELECTION_PROPERTY, SELECT_ALL_ACTION, UNDO_ACTION

  object undoAction extends Action("Undo") {

    toolTip = "<html>Undo <i>(control+Z)</i>"

    icon = Utils.getIcon("/images/small-icons/undo.png")

    def apply() {
      editorPane.undoLastAction()
    }
  }

  object redoAction extends Action("Redo") {

    toolTip = "<html>Redo <i>(control+y)</i>"

    icon = Utils.getIcon("/images/small-icons/redo.png")

    def apply() {
      editorPane.redoLastAction()
    }
  }

  object cutAction extends Action("Cut") {

    toolTip = name

    accelerator = Some(KeyStroke.getKeyStroke('X', InputEvent.CTRL_DOWN_MASK))

    icon = Utils.getIcon("/images/small-icons/cut-to-clipboard.png")

    def apply() = editorPane.cut()

  }

  object copyAction extends Action("Copy") {

    toolTip = name

    accelerator = Some(KeyStroke.getKeyStroke('C', InputEvent.CTRL_DOWN_MASK))

    icon = Utils.getIcon("/images/small-icons/copy-to-clipboard.png")

    def apply() = editorPane.copy()
  }

  object pasteAction extends Action("Paste") {

    toolTip = name

    accelerator = Some(KeyStroke.getKeyStroke('V', InputEvent.CTRL_DOWN_MASK))

    icon = Utils.getIcon("/images/small-icons/paste-from-clipboard.png")

    def apply() = editorPane.paste()

  }

  object selectAllAction extends Action("Select All") {

    toolTip = name

    accelerator = Some(KeyStroke.getKeyStroke('A', InputEvent.CTRL_DOWN_MASK))

    icon = Utils.getIcon("/images/small-icons/select-all.png")

    def apply() = editorPane.selectAll()

  }

  object wrapLinesAction extends Action("") {

    toolTip = "Wrap Lines"

    var wrapLines = false

    icon = Utils.getIcon("/images/small-icons/wrap-lines.png")

    def apply() {
      wrapLines = !wrapLines
      editorPane.setLineWrap(wrapLines)
    }
  }

  object searchAction extends Action("") {

    toolTip = "<html>Search or Search and Replace <i>(control+F)</i>"

    icon = Utils.getIcon("/images/small-icons/find.png")

    var focusComponent: TextField = null

    val replaceField: TextField = new TextField(20)

    val replace = new Action("Replace") {
      mnemonic = KeyEvent.VK_P
      def apply() = if (editorPane.getSelectedText != null && editorPane.getSelectedText.size != 0) {
        editorPane.replaceSelection(replaceField.text)
      }
    }

    val replaceAndFind = new Action("Replace/Find") {
      mnemonic = KeyEvent.VK_E

      def apply() {
        replace()
        find()
      }
    }

    replaceField.action = replaceAndFind

    case class FindInfo(find: String, replace: String, forward: Boolean, sensitive: Boolean)

    def findInfo = {
      val forward = forwardSearchCheckBox.selected

      val sensitive = caseSensitiveCheckBox.selected

      val findRegExp = ((if (sensitive) "" else ("(?i)")) + findField.text)

      FindInfo(findRegExp, replaceField.text, forward, sensitive)
    }

    val find = new Action("Find") {

      mnemonic = KeyEvent.VK_F

      def apply() {

        val info = findInfo

        val found = SearchEngine.find(editorPane, info.find, info.forward, true, false, true)

        if (!found) {
          infoLabel.text = """<html><font color="RED"><b>End reached!</b></font> (Starting from beginning)"""
          editorPane.setCaretPosition(0)
        }
      }
    }

    val findField: TextField = new TextField(20) {
      action = find
    }

    val caseSensitiveCheckBox = new CheckBox("Match case") {
      mnemonic = Key.H
      selected = false
    }

    val forwardSearchCheckBox = new CheckBox("Forward") {
      mnemonic = Key.D
      selected = true
    }

    val infoLabel = new Label("<html><i>(Tip: Java regexp)</i>")

    def apply() {
      editorAndToolArea.setTool(new BoxPanel(Orientation.Vertical) {

        private val close = new Action("Close") {
          mnemonic = KeyEvent.VK_O
          def apply() {

            editorAndToolArea.removeTool
            editorPane.requestFocus()

          }
        }

        contents += new FlowPanel(FlowPanel.Alignment.Left)() {

          contents += findField
          focusComponent = findField
          contents += new Button(find)
          contents += new Button(close)
          contents += caseSensitiveCheckBox
          contents += forwardSearchCheckBox
          contents += infoLabel

        }

        contents += new FlowPanel(FlowPanel.Alignment.Left)() {

          private val replaceAll = new Action("Replace All") {

            mnemonic = KeyEvent.VK_A
            def apply() {

              val info = findInfo

              SearchEngine.replaceAll(editorPane, info.find, info.replace, true, false, true)

              close()
            }
          }

          contents += replaceField

          contents += new Button(replace)
          contents += new Button(replaceAndFind)
          contents += new Button(replaceAll)

        }

      }, Unit => Unit)

      EditorPanel.this.peer.revalidate()
      focusComponent.requestFocus()
      focusComponent.selectAll
    }
  }

  object gotoLineAction extends Action("") {

    toolTip = "<html>Goto Line <i>(control+L)</i>"

    var wrapLines = false

    icon = Utils.getIcon("/images/small-icons/goto-line.png")

    var focusComponent: Component = null

    def apply() {
      editorAndToolArea.setTool(new FlowPanel(FlowPanel.Alignment.Left)() {

        private val gotoLine = new Action("Goto Line") {

          mnemonic = KeyEvent.VK_G

          def apply() {
            try {
              val selectedLineNumber: Int = lineNumberField.text.toInt match {
                case n if (n >= 1) => n
                case n => 1
              }
              editorPane.setCaretPosition(editorPane.getLineStartOffset(selectedLineNumber - 1))

            } catch {
              case e: BadLocationException => {
                //A line that does not exist. Set current pos to last line
                editorPane.setCaretPosition(editorPane.getLineStartOffset(editorPane.getText().lines.toList.size - 1))
              }
              case _ => //Ignore 
            }
            finally {
              editorAndToolArea.removeTool()
            }
          }
        }

        private val lineNumberField: TextField = new TextField(10) {
          action = gotoLine
        }
        contents += lineNumberField
        focusComponent = lineNumberField
        contents += new Button(gotoLine)
      })
      focusComponent.requestFocus()

    }
  }

  private def changeToSavedIcon() {

    tabComponent.icon = Utils.iconFromContentType(fileBuffer.contentType)
  }

  editorPane.addFocusListener(new FocusListener() {
    def focusGained(e: FocusEvent) {
      editorPane.getKeymap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK), cutAction.peer)
      editorPane.getKeymap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK), copyAction.peer)
      editorPane.getKeymap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK), pasteAction.peer)
      editorPane.getKeymap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK), saveAction.peer)
      editorPane.getKeymap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK), searchAction.peer)
      editorPane.getKeymap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK), gotoLineAction.peer)
    }

    def focusLost(e: FocusEvent) {
    }
  })

  def close() = if (!saved) {
    val result = Dialog.showOptions(this,
      message = ("\"" + fileBuffer.file.getOrElse("New File") + "\" has been modified. Save changes?"),
      title = "Save Resource",
      messageType = Dialog.Message.Question,
      optionType = Dialog.Options.YesNoCancel,
      entries = Seq("Yes", "No", "Cancel"),
      initial = 0)

    result match {
      case Dialog.Result.Yes => {
        saveAction()
        true
      }
      case Dialog.Result.No => true
      case _ => false
    }

  } else true

  def notifyAboutCodeInfo(notification: CodeNotification): Unit = notification match {
    case Error(_, line, _) => editorPane.addLineHighlight(line - 1, colorProperties.errorLineColor.get)
    case Warning(_, line, _) => editorPane.addLineHighlight(line - 1, colorProperties.warningLineColor.get)
  }

  def notifyAboutClearCodeInfo() {
    editorPane.removeAllLineHighlights()
  }

  private def syntaxStyleFromContentType(contentType: String) = contentType.toLowerCase match {
    case "text/asm" => SyntaxConstants.SYNTAX_STYLE_ASSEMBLER_X86
    case "text/bbcode" => SyntaxConstants.SYNTAX_STYLE_BBCODE
    case "text/c" => SyntaxConstants.SYNTAX_STYLE_C
    case "text/cpp" => SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS
    case "text/c++" => SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS
    case "text/cs" => SyntaxConstants.SYNTAX_STYLE_CSHARP
    case "text/c#" => SyntaxConstants.SYNTAX_STYLE_CSHARP
    case "text/css" => SyntaxConstants.SYNTAX_STYLE_CSS
    case "text/pas" => SyntaxConstants.SYNTAX_STYLE_DELPHI
    case "text/for" => SyntaxConstants.SYNTAX_STYLE_FORTRAN
    case "text/f" => SyntaxConstants.SYNTAX_STYLE_FORTRAN
    case "text/f77" => SyntaxConstants.SYNTAX_STYLE_FORTRAN
    case "text/f90" => SyntaxConstants.SYNTAX_STYLE_FORTRAN
    case "text/groovy" => SyntaxConstants.SYNTAX_STYLE_GROOVY
    case "text/html" => SyntaxConstants.SYNTAX_STYLE_HTML
    case "text/htm" => SyntaxConstants.SYNTAX_STYLE_HTML
    case "text/java" => SyntaxConstants.SYNTAX_STYLE_JAVA
    case "text/js" => SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT
    case "text/jsp" => SyntaxConstants.SYNTAX_STYLE_JSP
    case "text/lisp" => SyntaxConstants.SYNTAX_STYLE_LISP
    case "text/lsp" => SyntaxConstants.SYNTAX_STYLE_LISP
    case "text/cl" => SyntaxConstants.SYNTAX_STYLE_LISP
    case "text/lua" => SyntaxConstants.SYNTAX_STYLE_LUA
    case "text/makefile" => SyntaxConstants.SYNTAX_STYLE_MAKEFILE
    case "text/pl" => SyntaxConstants.SYNTAX_STYLE_PERL
    case "text/php" => SyntaxConstants.SYNTAX_STYLE_PHP
    case "text/properties" => SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE
    case "text/py" => SyntaxConstants.SYNTAX_STYLE_PYTHON
    case "text/rb" => SyntaxConstants.SYNTAX_STYLE_RUBY
    case "text/sas" => SyntaxConstants.SYNTAX_STYLE_SAS
    case "text/scala" => SyntaxConstants.SYNTAX_STYLE_SCALA
    case "text/sql" => SyntaxConstants.SYNTAX_STYLE_SQL
    case "text/tcl" => SyntaxConstants.SYNTAX_STYLE_TCL
    case "text/sh" => SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL
    case "text/bat" => SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH
    case "text/xml" => SyntaxConstants.SYNTAX_STYLE_XML
    case _ => SyntaxConstants.SYNTAX_STYLE_NONE
  }

  override def requestFocusInWindow() = editorPane.requestFocusInWindow()

}