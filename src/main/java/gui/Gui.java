package gui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * This program auto generates flash cards.
 *
 * Author: Long Lee Website: flashcardsgenerator.com Last modified: August 2015
 */

public class Gui {

	// These filter names are displayed to the user in the file dialog. Note
	// that the inclusion of the actual extension in parentheses is optional,
	// and doesn't have any effect on which files are displayed.
	private static final String[] FILTER_NAMES = { "Plan Text Files (*.txt)", "Comma Separated Values Files (*.csv)", "Open Office Spreadsheet Files (*.sxc)", "Microsoft Excel Spreadsheet Files (*.xls)", "All Files (*.*)" };

	// These filter extensions are used to filter which files are displayed.
	private static final String[] FILTER_EXTS = { "*.txt", "*.csv", "*.sxc", "*.xls", "*.*" };

	/**
	 * Runs the application
	 */
	public void run() {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("Flashcards Generator");

		createContents(shell);
		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	/**
	 * Creates the contents for the window
	 * 
	 * @param shell
	 *            the parent shell
	 */
	public void createContents(final Shell shell) {
		final String separator = System.lineSeparator();

		shell.setLayout(new GridLayout(7, true));

		/* Label file names */
		new Label(shell, SWT.NONE).setText("File Name : ");

		/* Text contains file paths */
		final Text fileName = new Text(shell, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 5;
		fileName.setLayoutData(data);

		/* Button Open */
		Button open = new Button(shell, SWT.PUSH);
		open.setText("Open...");
		data = new GridData(GridData.FILL_BOTH);
		open.setLayoutData(data);

		/* Label input word list */
		new Label(shell, SWT.NONE).setText("Input Word List");
		;

		/* Label input count */
		Label inputCountLabel = new Label(shell, SWT.NONE);
		inputCountLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		inputCountLabel.setText("Input Words");

		/* Text contains number of input words */
		final Text inputCount = new Text(shell, SWT.BORDER);
		data = new GridData(GridData.FILL_BOTH);
		inputCount.setLayoutData(data);
		inputCount.setText("0");

		/* Browser to show flash cards */
		Browser browser = new Browser(shell, SWT.BORDER);
		data = new GridData(GridData.FILL_BOTH);
		data.verticalSpan = 9;
		data.horizontalSpan = 4;
		browser.setLayoutData(data); 

		/* Text contains input words */
		final Text inputList = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 200;
		data.verticalSpan = 3;
		data.horizontalSpan = 3;
		inputList.setLayoutData(data);

		/* Button Generate */
		Button generate = new Button(shell, SWT.PUSH);
		generate.setText("Generate...");
		data = new GridData(GridData.FILL_BOTH);
		generate.setLayoutData(data);

		/* Two empty labels */
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);

		/* Label output card list */
		new Label(shell, SWT.NONE).setText("Output Cards List");

		/* Label output count */
		Label outputCountLabel = new Label(shell, SWT.NONE);
		outputCountLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		outputCountLabel.setText("Output Cards");

		/* Text contains number of output cards */
		Text outputCount = new Text(shell, SWT.BORDER);
		data = new GridData(GridData.FILL_BOTH);
		outputCount.setLayoutData(data);
		outputCount.setText("0");

		/* Text contains output cards */
		final Text outputList = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 200;
		data.verticalSpan = 3;
		data.horizontalSpan = 3;
		outputList.setLayoutData(data);

		/* Button Save */
		Button save = new Button(shell, SWT.PUSH);
		data = new GridData(GridData.FILL_BOTH);
		save.setLayoutData(data);
		save.setText("Save...");

		/* Two empty labels */
		new Label(shell, SWT.NONE);
		new Label(shell, SWT.NONE);

		/* Check box use proxy */
		Button useProxy = new Button(shell, SWT.CHECK);
		useProxy.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		useProxy.setText("use proxy");

		/* Label Proxy */
		final Label proxyLabel = new Label(shell, SWT.NONE);
		proxyLabel.setText("Proxy IP Address");
		proxyLabel.setVisible(false);
		proxyLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		/* Text box contains proxy IP address */
		final Text proxyIpAddress = new Text(shell, SWT.BORDER);
		proxyIpAddress.setText("");
		proxyIpAddress.setVisible(false);
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		proxyIpAddress.setLayoutData(data);
		
		
		/* Monitor and handle Open events */
		open.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// User has selected to open multiple files
				FileDialog dlg = new FileDialog(shell, SWT.MULTI);
				dlg.setFilterNames(FILTER_NAMES);
				dlg.setFilterExtensions(FILTER_EXTS);
				String fn = dlg.open();
				if (fn != null) {
					// Append all the selected files. Since getFileNames()
					// returns only the names, and not the path, prepend the
					// path,
					// normalizing if necessary
					StringBuffer buf = new StringBuffer();
					String totalContent = "";
					String[] files = dlg.getFileNames();

					for (int i = 0, n = files.length; i < n; i++) {
						buf.append(dlg.getFilterPath());
						String dlgPath = dlg.getFilterPath();

						if (buf.charAt(buf.length() - 1) != File.separatorChar) {
							buf.append(File.separatorChar);
							dlgPath = dlg.getFilterPath() + File.separatorChar;
						}
						buf.append(files[i]);
						buf.append(" ; ");

						String filePath = dlgPath + files[i];
						File wordListFile = new File(filePath);
						String fileContent = null;
						try {
							fileContent = FileUtils.readFileToString(wordListFile, "UTF-8");
						} catch (IOException e) {
							e.printStackTrace();
						}

						fileContent += separator;
						totalContent += fileContent;
					}

					if (totalContent.endsWith(separator))
						totalContent = totalContent.substring(0, totalContent.length() - 1);
					inputList.setText(totalContent);

					buf.setLength(buf.length() - 2);
					fileName.setText(buf.toString());

					inputCount.setText("" + inputList.getLineCount());
				}
			}
		});

		/* Monitor and handle Generate events */
		generate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// User has selected to generate flash cards
				String input = inputList.getText();
				String[] wordList = input.split(separator, -1);
				for (String w : wordList) {
					System.out.println("input word: " + w);

				}
			}
		});

		/* Monitor and handle Save events */
		save.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// User has selected to save a file
				FileDialog dlg = new FileDialog(shell, SWT.SAVE);
				dlg.setFilterNames(FILTER_NAMES);
				dlg.setFilterExtensions(FILTER_EXTS);
				String fileName = saveFileName(dlg);
				if (outputList != null) {
					try {
						File file = new File(fileName);
						FileWriter writer = new FileWriter(file);
						writer.write(outputList.getText());
						writer.close();
					} catch (IOException e) {
						System.err.println("Exception occured: File not saved!");
						e.printStackTrace();
					}
				}
			}
		});

		/* Monitor and handle use proxy events */
		useProxy.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// User has selected use proxy
				Button checkBox = (Button) event.getSource();
				System.out.println(checkBox.getSelection());
				GridData data = new GridData(GridData.FILL_BOTH);
				data.exclude = checkBox.getSelection();
				proxyLabel.setVisible(data.exclude);
				proxyIpAddress.setVisible(data.exclude);
				shell.layout(false);
			}
		});

		/* Validate input contains only digits */
		proxyIpAddress.addListener(SWT.Verify, new Listener() {
			public void handleEvent(Event event) {
				String string = event.text;
				char[] chars = new char[string.length()];
				string.getChars(0, chars.length, chars, 0);
				for (int i = 0; i < chars.length; i++) {
					if (!(('0' <= chars[i] && chars[i] <= '9') || chars[i] == '.' || chars[i] == ':') || proxyIpAddress.getText().length() > 19) {
						event.doit = false;
						return;
					}
				}
			}
		});

	}

	public String saveFileName(FileDialog dlg) {
		// We store the selected file name in fileName
		String fileName = null;

		// The user has finished when one of the following happens:
		// 1) The user dismisses the dialog by pressing Cancel
		// 2) The selected file name does not exist
		// 3) The user agrees to overwrite existing file
		boolean done = false;

		while (!done) {
			// Open the File Dialog
			fileName = dlg.open();
			if (fileName == null) {
				// User has cancelled, so quit and return
				done = true;
			} else {
				// User has selected a file; see if it already exists
				File file = new File(fileName);
				if (file.exists()) {
					// The file already exists; asks for confirmation
					MessageBox mb = new MessageBox(dlg.getParent(), SWT.ICON_WARNING | SWT.YES | SWT.NO);

					// We really should read this string from a
					// resource bundle
					mb.setMessage(fileName + " already exists. Do you want to replace it?");

					// If they click Yes, we're done and we drop out. If
					// they click No, we redisplay the File Dialog
					done = mb.open() == SWT.YES;
				} else {
					// File does not exist, so drop out
					done = true;
				}
			}
		}
		return fileName;
	}

	/**
	 * The application entry point
	 * 
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		new Gui().run();
	}
}
