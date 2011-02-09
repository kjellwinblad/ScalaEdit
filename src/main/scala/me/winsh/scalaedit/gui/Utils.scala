package me.winsh.scalaedit.gui

import javax.swing._
import java.awt._
import java.awt.datatransfer._

object Utils {

  def clipboardContents_=(contentToSet: String) {
    val stringSelection = new StringSelection(contentToSet);
    val clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents(stringSelection, new ClipboardOwner() {
      def lostOwnership(clipboard: Clipboard, contents: Transferable) { /*Nothing needs to be done*/ }
    });
  }

  def clipboardContents = {
    var result = "";
    val clipboard = Toolkit.getDefaultToolkit().getSystemClipboard()

    val contents = clipboard.getContents(this)

    val hasTransferableText =
      (contents != null) &&
        contents.isDataFlavorSupported(DataFlavor.stringFlavor)

    if (hasTransferableText) {
      try {
        result = contents.getTransferData(DataFlavor.stringFlavor).toString
      } catch {
        case e => {
          println(e)
          e.printStackTrace()
        }
      }

    }
    result
  }

  def getImage(classPathPath: String) = getIcon(classPathPath).getImage()

  def getIcon(classPathPath: String) = new ImageIcon(this.getClass.getResource(classPathPath))

  def swingInvokeLater(fun: () => Unit) {
    SwingUtilities.invokeLater(new Runnable {
      def run {
        fun()
      }
    })
  }

  def swingInvokeAndWait(fun: () => Unit) {
    SwingUtilities.invokeAndWait(new Runnable {
      def run {
        fun()
      }
    })
  }

  def runInNewThread(fun: () => Unit) {
    new Thread(new Runnable() {
      def run {
        fun()
      }
    }).start()
  }

}