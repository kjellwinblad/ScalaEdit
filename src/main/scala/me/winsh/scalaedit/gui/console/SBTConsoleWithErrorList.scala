package me.winsh.scalaedit.gui.console

import scala.swing._
import me.winsh.scalaedit.api._
import me.winsh.scalaedit.gui.editor.EditorsPanel
import scala.swing.ListView.Renderer
import javax.swing.border._
import scala.swing.event._
import java.io.File

class SBTConsoleWithErrorList extends ConsolePanel {

  val consoleType = SBTConsole

  private val sbtConsolePanel = new SBTConsolePanel()

  private val errorList = new ListView[CodeNotification]() {

    renderer = new Renderer[CodeNotification] {
    	val cellView =new BoxPanel(Orientation.Vertical) {
    		
    		opaque = true;
    		//border = new LineBorder(java.awt.Color.BLACK, 5, true) 
    		  
    		private val notificationTypeLabel = new TextField()
    		private val lineNumberField = new TextField(){
    			editable = false
    		}
    		private val errorMessageBox = new TextArea(){
    			editable = false
    		}
    		private val fileNameField = new TextField(){
    			editable = false
    		} 
        	contents += new FlowPanel(FlowPanel.Alignment.Left)(){ 
        		opaque = false
        		contents += new Label("Type: ")
        		contents += notificationTypeLabel
        		contents += new Label("Line: ")
        		contents += lineNumberField
        		contents += new Label("File: ")
        		contents += fileNameField       		
        	}

        	contents += new FlowPanel(FlowPanel.Alignment.Left)(){
        		opaque = false
        		contents += new Label("Message: ")
        	}
        	contents += errorMessageBox

        	contents += new Separator()
        	
        	def populateWithNotification(n:CodeNotification) {
        		val (typeColor, notificationType, fileName, lineNumber, message) = n match {
        			case Error(fileName, lineNumber, message) => (java.awt.Color.RED, "error" ,fileName, lineNumber, message)
        			case Warning(fileName, lineNumber, message) => (java.awt.Color.YELLOW, "warning" ,fileName, lineNumber, message)
        		}
        		
        		//notificationTypeLabel.background = typeColor
        		//notificationTypeLabel.foreground = typeColor
        		notificationTypeLabel.text = notificationType
        		lineNumberField.text = lineNumber.toString
        		errorMessageBox.text = message
        		errorMessageBox.background = notificationTypeLabel.background
        		fileNameField.text = fileName
        		
        		
        	} 
        	
        	
    	}
    	
      override def componentFor(list: scala.swing.ListView[_], isSelected: Boolean, focused: Boolean, a: CodeNotification, index: Int): Component = {
    	  
    	if (isSelected) {
            cellView.background = selectionBackground
            cellView.foreground = selectionForeground
        } else {
            cellView.background = background
            cellView.foreground = foreground
        } 
    	  
    	cellView.populateWithNotification(a)
        cellView
      }
    }
    
    listenTo(mouse.clicks)
    
    reactions += {
    	case MouseClicked(_, _, _, _, _) if(peer.getSelectedIndex() >=0 )=>{
    		//Open upp the file
    		
    		val notification = listData(peer.getSelectedIndex())
    		val fileName = notification.fileName
    		val lineNumber = notification.line
    		val editor = EditorsPanel().addFileEditor(FileBuffer(new File(fileName))).editorPane
    		editor.setCaretPosition(editor.getLineStartOffset(lineNumber-1))
    	}
    }
 
  }  

  private val splitPane = new SplitPane() {

    orientation = Orientation.Vertical

    oneTouchExpandable = true

    leftComponent = sbtConsolePanel

    rightComponent = new ScrollPane(errorList)

  }

  add(splitPane, BorderPanel.Position.Center)

  def close() = {
    sbtConsolePanel.close()
  }

  private var toMatchOn = new StringBuffer()

  case object Beginning {

    val le = """(.*Compiling.*sources.*)\n""".r //"""(?s)(.*\[.*info.*]\s*Compiling.*sources\.\.\..*)""".r

    def unapply(line: String) = {

      try {
        val le(c) = line

        Some(Beginning)

      } catch {
        case _ => None
      }
    }
  }
  case object FirstErrorLine {

    val regexpLine1 = """.*error.*0m(.*):(\d*):(.*)\n""" //""".*\[.*e.*r.*r.*o.*r.*\]0m(.*):([^:]*):\s*(.*)\n"""
    val regexpLine2 = "" //""".*\[.*e.*r.*r.*o.*r.*\]\s(.*)\n"""
    val regexpLine3 = "" //""".*\[.*e.*r.*r.*o.*r.*\]\s(.*)\n.*"""

    val le = (regexpLine1 + regexpLine2 + regexpLine3).r

    def unapply(line: String) = {

      try {

        val le(sourceFileName, lineNumber, messagePart1) = line
        Some(sourceFileName, lineNumber.toInt, messagePart1)

      } catch {
        case e => None
      }
    }
  }

  case object FirstWarningLine {

    val regexpLine1 = """.*warn.*0m(.*):(\d*):(.*)\n""" //""".*\[.*e.*r.*r.*o.*r.*\]0m(.*):([^:]*):\s*(.*)\n"""
    val regexpLine2 = "" //""".*\[.*e.*r.*r.*o.*r.*\]\s(.*)\n"""
    val regexpLine3 = "" //""".*\[.*e.*r.*r.*o.*r.*\]\s(.*)\n.*"""

    val le = (regexpLine1 + regexpLine2 + regexpLine3).r

    def unapply(line: String) = {

      try {

        val le(sourceFileName, lineNumber, messagePart1) = line
        Some(sourceFileName, lineNumber.toInt, messagePart1)

      } catch {
        case e => None
      }
    }
  }

  case object End {

    val le = """(.*==\s*compile\s*==.*)\n""".r //"""(?s)(.*\[.*info.*]\s*Compiling.*sources\.\.\..*)""".r

    def unapply(line: String) = {

      try {
        val le(c) = line

        Some(End)

      } catch {
        case _ => None
      }
    }
  }

  var codeNotifications: List[CodeNotification] = Nil
  sbtConsolePanel.addInvokeOnConsoleOutput((in: Int) => {
    toMatchOn.append(in.toChar)

    //See if it matches something biginning, error, warning, end
    if (in == 10) {

      toMatchOn.toString match {
        case Beginning(s) => {

          EditorsPanel().notifyAboutClearCodeInfo()
          toMatchOn = new StringBuffer()
        } case FirstErrorLine(fileName, lineNumber, message) => {

          codeNotifications = Error(fileName, lineNumber, message) :: codeNotifications

          toMatchOn = new StringBuffer()
        } case FirstWarningLine(fileName, lineNumber, message) => {

          codeNotifications = Warning(fileName, lineNumber, message) :: codeNotifications

          toMatchOn = new StringBuffer()
        } case End(s) => {

          errorList.listData = codeNotifications

          EditorsPanel().notifyAboutCodeInfo(codeNotifications)

          codeNotifications = Nil
          toMatchOn = new StringBuffer()
        }
        case _ =>
      }

      toMatchOn = new StringBuffer()

    }
  })

}