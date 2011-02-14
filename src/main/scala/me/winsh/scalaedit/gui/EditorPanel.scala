package me.winsh.scalaedit.gui

import me.winsh.scalaedit.api._
import scala.swing._
import jsyntaxpane._
import javax.swing._
import org.fife.ui.rtextarea._;
import org.fife.ui.rsyntaxtextarea._;

class EditorPanel(val fileBuffer: FileBuffer) extends BorderPanel with Closeable {

  val editorPane = new RSyntaxTextArea();

  editorPane.setSyntaxEditingStyle(syntaxStyleFromContentType(fileBuffer.contentType));

  editorPane.setTabSize(2)

  val scrollPane = new RTextScrollPane(editorPane);

  private class EditorWrapper(val scroller: RTextScrollPane) extends Component {
    override lazy val peer: JScrollPane = scroller
  }
  add(new EditorWrapper(scrollPane), BorderPanel.Position.Center)

  //editorPane.setContentType(fileBuffer.contentType)
  //load the content into the editor 
  editorPane.setText(fileBuffer.content)

  def close() = {
    false
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