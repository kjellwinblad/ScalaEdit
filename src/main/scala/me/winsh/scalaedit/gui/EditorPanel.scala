package me.winsh.scalaedit.gui

import me.winsh.scalaedit.api._
import scala.swing._
import jsyntaxpane._
import javax.swing._
import org.fife.ui.rtextarea._;
import org.fife.ui.rsyntaxtextarea._;

class EditorPanel(val fileBuffer: FileBuffer) extends BorderPanel with Closeable {

  /*private val advancedPane = new EditorPaneWrapper()

  val editorPane = advancedPane.editorPane

  val scrollPane = advancedPane.peer
*/

  val editorPane = new RSyntaxTextArea();
  
  editorPane.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SCALA);
  
  val scrollPane = new RTextScrollPane(editorPane);

 private class EditorWrapper(val scroller:RTextScrollPane) extends Component {
  override lazy val peer: JScrollPane = scroller
}
  add(new EditorWrapper(scrollPane), BorderPanel.Position.Center)

  //editorPane.setContentType(fileBuffer.contentType)
  //load the content into the editor 
  editorPane.setText(fileBuffer.content)

  def close() = {
    false
  }

}