package gui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * This class demonstrates JFace's ProgressMonitorDialog class
 */
public class Progress extends ApplicationWindow {
	/**
	 * ShowProgress constructor
	 */
	public Progress() {
		super(null);
	}

	/**
	 * Runs the application
	 */
	public void run() {
		// Don't return from open() until window closes
		setBlockOnOpen(true);

		// Open the main window
		open();

		// Dispose the display
		Display.getCurrent().dispose();
	}

	/**
	 * Configures the shell
	 * 
	 * @param shell
	 *            the shell
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);

		// Set the title bar text
		shell.setText("Show Progress");
	}

	/**
	 * Creates the main window's contents
	 * 
	 * @param parent
	 *            the main window
	 * @return Control
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, true));

		// Create the indeterminate checkbox
		final Button indeterminate = new Button(composite, SWT.CHECK);
		indeterminate.setText("Indeterminate");

		// Create the ShowProgress button
		Button showProgress = new Button(composite, SWT.NONE);
		showProgress.setText("Show Progress");

		final Shell shell = parent.getShell();

		// Display the ProgressMonitorDialog
		showProgress.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				try {
					new ProgressMonitorDialog(shell).run(true, true, new LongRunningOperation(indeterminate.getSelection()));
				} catch (InvocationTargetException e) {
					MessageDialog.openError(shell, "Error", e.getMessage());
				} catch (InterruptedException e) {
					MessageDialog.openInformation(shell, "Cancelled", e.getMessage());
				}
			}
		});

		parent.pack();
		return composite;
	}

	/**
	 * The application entry point
	 * 
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		new Progress().run();
	}
}

/**
 * This class represents a long running operation
 */
class LongRunningOperation implements IRunnableWithProgress {
	private static final int total = 0;
	private static final int current = 0;
	private boolean indeterminate;
	
	public 
	
	public LongRunningOperation(boolean indeterminate) {
		this.indeterminate = indeterminate;
	}

	/**
	 * Runs the long running operation
	 * 
	 * @param monitor
	 *            the progress monitor
	 */
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		monitor.beginTask("Running long running operation", indeterminate ? IProgressMonitor.UNKNOWN : TOTAL_TIME);
		for (int total = 0; total < TOTAL_TIME && !monitor.isCanceled(); total += INCREMENT) {
			Thread.sleep(INCREMENT);
			monitor.worked(INCREMENT);
			if (total == TOTAL_TIME / 2)
				monitor.subTask("Doing second half");
		}
		monitor.done();
		if (monitor.isCanceled())
			throw new InterruptedException("The long running operation was cancelled");
	}
}