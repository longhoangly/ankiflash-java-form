package gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class Gui2 {
  public static void main(String[] args) {
    Display display = new Display();
    Shell shell = new Shell(display);

    int style = SWT.ICON_ERROR;
    
    //SWT.ICON_QUESTION | SWT.YES | SWT.NO
    
    MessageBox messageBox = new MessageBox(shell, style);
    messageBox.setMessage("Message");
    int rc = messageBox.open();

    switch (rc) {
    case SWT.OK:
      System.out.println("SWT.OK");
      break;
    case SWT.CANCEL:
      System.out.println("SWT.CANCEL");
      break;
    case SWT.YES:
      System.out.println("SWT.YES");
      break;
    case SWT.NO:
      System.out.println("SWT.NO");
      break;
    case SWT.RETRY:
      System.out.println("SWT.RETRY");
      break;
    case SWT.ABORT:
      System.out.println("SWT.ABORT");
      break;
    case SWT.IGNORE:
      System.out.println("SWT.IGNORE");
      break;
    }

    display.dispose();
  }
}