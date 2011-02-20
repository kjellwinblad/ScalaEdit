package me.winsh.scalaedit.gui

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

class EditorPanel(val fileBuffer: FileBuffer, val tabComponent: TabComponent) extends BorderPanel with Closeable {

  private var savedVar = true

  def saved_=(isSaved: Boolean) {
    savedVar = isSaved
    if (isSaved)
      changeToSavedIcon()
    else
      tabComponent.icon = Utils.getIcon("/images/small-icons/actions/filesaveas.png")
  }

  def saved = savedVar

  fileBuffer.file match {
    case None => saved = false
    case Some(_) => saved = true
  }

  val editorPane = new RSyntaxTextArea();

  editorPane.setSyntaxEditingStyle(syntaxStyleFromContentType(fileBuffer.contentType))

  editorPane.setTabSize(2)

  val scrollPane = new RTextScrollPane(editorPane);

  private class EditorWrapper(val scroller: RTextScrollPane) extends Component {
    override lazy val peer: JScrollPane = scroller
  }
  private val editorWrapper = new EditorWrapper(scrollPane)
  add(editorWrapper, BorderPanel.Position.Center)

  //load the content into the editor 
  try {
    editorPane.setText(fileBuffer.content)
  } catch {
    case _ => {
    	Dialog.showMessage(message="This may not be a text file.", title="Could Not Read File")
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

  val saveAction = new AbstractAction() {
    def actionPerformed(actionEvent: ActionEvent): Unit = {

      fileBuffer.file match {
        case Some(_) => {
        	try{
          fileBuffer.content = editorPane.getText
          changeToSavedIcon()
        	}catch{
        		case _ => Dialog.showMessage(message="The file might be write protected.", title="Could Not Save File")
        	}
        }
        case None => {
          saveAsAction.actionPerformed(actionEvent)
        }
      }
    }
  }

  val saveAsAction: AbstractAction = new AbstractAction() {
    def actionPerformed(actionEvent: ActionEvent): Unit = {

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
        saveAction.actionPerformed(actionEvent)
        editorPane.setSyntaxEditingStyle(syntaxStyleFromContentType(fileBuffer.contentType))
      })

    }
  }

  private def changeToSavedIcon() {
    tabComponent.icon = Utils.iconFromContentType(fileBuffer.contentType)
  }

  editorPane.addFocusListener(new FocusListener() {
    def focusGained(e: FocusEvent) {
      editorPane.getKeymap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK), saveAction)
    }

    def focusLost(e: FocusEvent) {
      //editorPane.getKeymap().removeBindings()
    }
  })

  editorPane.getKeymap.addActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK), saveAction)

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
        saveAction.actionPerformed(null)
        true
      }
      case Dialog.Result.No => true
      case _ => false
    }

  } else true

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