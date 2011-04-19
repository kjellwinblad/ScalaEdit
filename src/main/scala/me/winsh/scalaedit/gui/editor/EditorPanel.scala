package me.winsh.scalaedit.gui.editor

import me.winsh.scalaedit.api._
import scala.swing._
import jsyntaxpane._
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

class EditorPanel(val fileBuffer: FileBuffer, val tabComponent: TabComponent) extends BorderPanel with Closeable {

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
    override protected def createPopupMenu() = (new PopupMenu() {
      add(new MenuItem(cutAction))
      add(new MenuItem(copyAction))
      add(new MenuItem(pasteAction))
      addSeparator()
      add(new MenuItem(selectAllAction))
    }).peer
  }

  editorPane.setSyntaxEditingStyle(syntaxStyleFromContentType(fileBuffer.contentType))

  editorPane.setTabSize(2)

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
    add(new ToggleButton() { action = wrapLinesAction })
  }

  add(Component.wrap(toolBar), BorderPanel.Position.North)

  //load the content into the editor 
  try {
    editorPane.setText(fileBuffer.content)
  } catch {
    case _ => {
      Dialog.showMessage(message = "This may not be a text file.", title = "Could Not Read File")
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
    toolTip = "Save (control+s)"
    def apply() {
      fileBuffer.file match {
        case Some(_) => {
          try {
            fileBuffer.content = editorPane.getText
            saved = true
          } catch {
            case _ => Dialog.showMessage(message = "The file might be write protected.", title = "Could Not Save File")
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

    toolTip = "<html>Undo <i>(control+z)</i>"

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

  object cutAction extends Action("<html>Cut <i>(control+x)</i>") {

    toolTip = name

    icon = Utils.getIcon("/images/small-icons/cut-to-clipboard.png")

    def apply() = editorPane.cut()

  }

  object copyAction extends Action("<html>Copy <i>(control+c)</i>") {

    toolTip = name

    icon = Utils.getIcon("/images/small-icons/copy-to-clipboard.png")

    def apply() = editorPane.copy()
  }

  object pasteAction extends Action("<html>Paste <i>(control+v)</i>") {

    toolTip = name

    icon = Utils.getIcon("/images/small-icons/paste-from-clipboard.png")

    def apply() = editorPane.paste()

  }

  object selectAllAction extends Action("<html>Select All <i>(control+a)</i>") {

    toolTip = name

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

    toolTip = "Search or Search and Replace <i>(control+f)</i>"

    icon = Utils.getIcon("/images/small-icons/find.png")

    var focusComponent: Component = null

    def apply() {
      editorAndToolArea.setTool(new BoxPanel(Orientation.Vertical) {

        private var resultIterator: Regex.MatchIterator = null
        private var lastSearchString: Option[String] = None
        private var currentSelectionSize = 0
        private var selectionChangeDueToReplacement = 0
        private val find = new Action("Find") {

          mnemonic = KeyEvent.VK_F

          private def initResultIterator() {
            currentSelectionSize = 0
            selectionChangeDueToReplacement = 0

            lastSearchString = Some(findField.text)

            val sensitive = caseSensitiveCheckBox.selected

            val findRegExp = ((if (sensitive) "" else ("(?i)")) + findField.text).r

            resultIterator = findRegExp.findAllIn(editorPane.getText)

          }

          def apply() {

            if (lastSearchString != Some(findField.text))
              initResultIterator()

            if (!resultIterator.hasNext) {
              initResultIterator()
              infoLabel.text = "End reached!"
            } else {
              editorPane.select(resultIterator.start + selectionChangeDueToReplacement,
                resultIterator.end + selectionChangeDueToReplacement)
              currentSelectionSize = resultIterator.end - resultIterator.start
              resultIterator.next()
            }
          }
        }

        private val close = new Action("Close") {

          mnemonic = KeyEvent.VK_O
          def apply() {

            editorAndToolArea.removeTool
            editorPane.setEditable(true)
            editorPane.requestFocus()

          }
        }
        private val findField: TextField = new TextField(20) {
          action = find
        }

        private val caseSensitiveCheckBox = new CheckBox("Case sensitive") {
          selected = false
        }
        private val infoLabel = new Label("<html><i>(Tip: Java regexp)</i>")
        contents += new FlowPanel(FlowPanel.Alignment.Left)() {

          contents += findField
          focusComponent = findField
          contents += new Button(find)
          contents += new Button(close)
          contents += caseSensitiveCheckBox
          contents += infoLabel

        }

        contents += new FlowPanel(FlowPanel.Alignment.Left)() {

          private val replace = new Action("Replace") {
            def apply() {

              mnemonic = KeyEvent.VK_P

              if (currentSelectionSize != 0) {

                selectionChangeDueToReplacement = selectionChangeDueToReplacement +
                  (replaceField.text.size - currentSelectionSize)
                val selStart = editorPane.getSelectionStart
                editorPane.replaceSelection(replaceField.text)
                if (selStart >= 0) {
                  editorPane.select(selStart, selStart + replaceField.text.size)
                  currentSelectionSize = replaceField.text.size
                }
              }
            }
          }

          private val replaceAndFind = new Action("Replace/Find") {
            mnemonic = KeyEvent.VK_E

            def apply() {

              replace()

              find()
            }
          }

          private val replaceAll = new Action("Replace All") {

            mnemonic = KeyEvent.VK_A
            def apply() {

              val sensitive = caseSensitiveCheckBox.selected

              val findRegExp = ((if (sensitive) "" else ("(?i)")) + findField.text).r

              val replacedText = findRegExp.replaceAllIn(editorPane.getText, replaceField.text)

              editorPane.setText(replacedText)

              close()
            }
          }

          private val replaceField: TextField = new TextField(20) {
            action = replaceAndFind
          }
          contents += replaceField

          contents += new Button(replace)
          contents += new Button(replaceAndFind)
          contents += new Button(replaceAll)

        }

      }, Unit=>{editorPane.setEditable(true)})

      focusComponent.requestFocus()
      editorPane.setEditable(false)

    }
  }

  object gotoLineAction extends Action("") {

    toolTip = "Goto Line <i>(control+l)</i>"

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
    case Error(_, line, _) => editorPane.addLineHighlight(line - 1, java.awt.Color.RED)
    case Warning(_, line, _) => editorPane.addLineHighlight(line, java.awt.Color.YELLOW)
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

}