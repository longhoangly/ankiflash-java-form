package gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import generator.Generator;

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

	// Labels for the button
	private static final String RUN = "Press to Run";
	private static final String IS_RUNNING = "Running...";

	/**
	 * Runs the application
	 */
	public void run() {
		Display display = new Display();
		Shell parentShell = new Shell(display);
		final Shell shell = new Shell(parentShell, SWT.SHELL_TRIM & (~SWT.RESIZE) & (~SWT.MAX));
		shell.setText("Flashcards Generator");

		createContents(shell);
		shell.pack();

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Point frameSize = shell.getSize();
		int x = (screenSize.width - frameSize.x) / 2;
		int y = (screenSize.height - frameSize.y) / 2;
		shell.setLocation(x, y);

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

		shell.setLayout(new GridLayout(3, true));

		/* Button Open */
		Button open = new Button(shell, SWT.PUSH);
		open.setText("Open...");
		GridData data = new GridData(GridData.FILL_BOTH);
		open.setLayoutData(data);

		/* Text contains file paths */
		final Text fileName = new Text(shell, SWT.BORDER);
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		fileName.setLayoutData(data);

		/* Label input word list */
		new Label(shell, SWT.NONE).setText("Input Word List");

		/* Label input count */
		Label inputCountLabel = new Label(shell, SWT.NONE);
		inputCountLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		inputCountLabel.setText("Input Words");

		/* Text contains number of input words */
		final Text inputCount = new Text(shell, SWT.BORDER);
		data = new GridData(GridData.FILL_BOTH);
		inputCount.setLayoutData(data);
		inputCount.setText("0");

		/* Text contains input words */
		final Text inputList = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.ALL);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 200;
		data.verticalSpan = 3;
		data.horizontalSpan = 3;
		inputList.setLayoutData(data);

		/* Button Generate */
		final Button generate = new Button(shell, SWT.PUSH);
		generate.setText("Press to Run");
		data = new GridData(GridData.FILL_BOTH);
		generate.setLayoutData(data);

		/* Text contains output cards */
		final Text outputListHiden = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.READ_ONLY);
		outputListHiden.setVisible(false);

		/* Two empty labels */
		new Label(shell, SWT.NONE);

		/* Label output card list */
		new Label(shell, SWT.NONE).setText("Output Cards List");

		/* Label output count */
		Label outputCountLabel = new Label(shell, SWT.NONE);
		outputCountLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		outputCountLabel.setText("Output Cards");

		/* Text contains number of output cards */
		final Text outputCount = new Text(shell, SWT.BORDER);
		data = new GridData(GridData.FILL_BOTH);
		outputCount.setLayoutData(data);
		outputCount.setText("0");

		/* Text contains output cards */
		final Text outputList = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
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

		/* One empty labels */
		new Label(shell, SWT.NONE);

		/* Check box use proxy */
		final Button useProxy = new Button(shell, SWT.CHECK);
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
						buf.setLength(buf.length() - 2);
						fileName.setText(buf.toString());

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
					inputCount.setText("" + inputList.getLineCount());

					outputList.setText("");
					outputListHiden.setText("");
					outputCount.setText("0");
				}
			}
		});

		/* Monitor and handle Generate events */
		generate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// Change the button's text
				generate.setText(IS_RUNNING);
				
				// User has selected to generate flash cards
				Generator generator = new Generator();
				String proxyStr = "";
				useProxy.setGrayed(true);
				if (proxyIpAddress.getText().contains(":"))
					proxyStr = proxyIpAddress.getText();
				//System.out.println("proxy String: " + proxyStr);

				String input = inputList.getText();
				String[] wordList = input.split(separator, -1);

				for (String word : wordList) {
					//System.out.println("INPUT: " + word);
					try {
						String ankiDeck = generator.generateFlashCards(word, proxyStr);
						if (!ankiDeck.contains("THIS WORD DOES NOT EXIST")) {
							outputListHiden.append(ankiDeck);
							outputList.append(generator.wrd + "\t" + generator.wordType + "\t" + generator.phonetic + "\t" + generator.pro_uk + "\t" + generator.pro_us + "\n");
							outputCount.setText("" + (outputList.getLineCount() - 1));
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				// Change the button's text
				generate.setText(RUN);
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
				if (outputListHiden.getText() != "") {
					try {
						File file = new File(fileName);
						FileWriter writer = new FileWriter(file);
						writer.write(outputListHiden.getText());
						writer.close();
					} catch (IOException e) {
						//System.out.println("Exception occured: File not saved!");
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
				//System.out.println("useProxy: " + checkBox.getSelection());

				if (checkBox.getSelection() == false) {
					proxyIpAddress.setText("");
				}

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
