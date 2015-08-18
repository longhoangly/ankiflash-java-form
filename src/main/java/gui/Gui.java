package gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.FileUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import generator.Generator;

/**
 * This program auto generates flash cards.
 *
 * Author: Long Lee | Website: flashcardsgenerator.com | Last modified: August
 * 2015
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

	// Proxy Connection String
	private String proxyStr = "";
	boolean isUseProxy = false;

	// All of wrong spelling words
	String wrongSpellingWords = "";

	// Get system separator
	final String separator = System.lineSeparator();

	// Declare Gui elements
	private Button open = null;
	private Text fileName = null;
	private Label inputCountLabel = null;
	private Text inputCount = null;
	private Text inputList = null;
	private Button generate = null;
	private Button cancel = null;
	private ProgressBar bar = null;
	private Label outputCountLabel = null;
	private Text outputCount = null;
	private Text outputList = null;
	private Button save = null;
	private Button useProxy = null;
	private Label proxyLabel = null;
	private Text proxyIpAddress = null;
	Text outputListHiden = null;

	// Thread to generate flash cards
	private Thread getOxfFlsCards = null;

	/**
	 * Runs the application
	 */
	public void run() {
		Display display = new Display();
		Shell shell = new Shell(display, SWT.SHELL_TRIM & (~SWT.RESIZE) & (~SWT.MAX));
		shell.setText("Flashcards Generator");

		InputStream stream = Gui.class.getResourceAsStream("favicon.ico");
		Image imgTrayIcon = new Image(display, stream);
		shell.setImage(imgTrayIcon);

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
	 * The method to create components on UI
	 */
	public void createContents(final Shell shell) {

		shell.setLayout(new GridLayout(3, true));

		/* Button Open */
		open = new Button(shell, SWT.PUSH);
		open.setText("Open...");
		GridData data = new GridData(GridData.FILL_BOTH);
		open.setLayoutData(data);

		/* Text contains file paths */
		fileName = new Text(shell, SWT.BORDER | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 2;
		fileName.setLayoutData(data);

		/* Label input word list */
		new Label(shell, SWT.NONE).setText("Input Word List");

		/* Label input count */
		inputCountLabel = new Label(shell, SWT.NONE);
		inputCountLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		inputCountLabel.setText("Input Words");

		/* Text contains number of input words */
		inputCount = new Text(shell, SWT.BORDER | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_BOTH);
		inputCount.setLayoutData(data);
		inputCount.setText("0");

		/* Text contains input words */
		inputList = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.ALL);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 200;
		data.verticalSpan = 3;
		data.horizontalSpan = 3;
		inputList.setLayoutData(data);

		/* Button Generate */
		generate = new Button(shell, SWT.PUSH);
		generate.setText(RUN);
		data = new GridData(GridData.FILL_BOTH);
		generate.setLayoutData(data);

		/* Text contains output cards */
		outputListHiden = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.READ_ONLY);
		outputListHiden.setVisible(false);

		/* Button Generate */
		cancel = new Button(shell, SWT.PUSH);
		cancel.setText("Cancel");
		data = new GridData(GridData.FILL_BOTH);
		cancel.setLayoutData(data);
		cancel.setEnabled(false);

		/* Status bar */
		bar = new ProgressBar(shell, SWT.SMOOTH);
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 3;
		bar.setLayoutData(data);

		/* Label output card list */
		new Label(shell, SWT.NONE).setText("Output Cards List");

		/* Label output count */
		outputCountLabel = new Label(shell, SWT.NONE);
		outputCountLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		outputCountLabel.setText("Output Cards");

		/* Text contains number of output cards */
		outputCount = new Text(shell, SWT.BORDER | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_BOTH);
		outputCount.setLayoutData(data);
		outputCount.setText("0");

		/* Text contains output cards */
		outputList = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 200;
		data.verticalSpan = 3;
		data.horizontalSpan = 3;
		outputList.setLayoutData(data);

		/* Button Save */
		save = new Button(shell, SWT.PUSH);
		data = new GridData(GridData.FILL_BOTH);
		save.setLayoutData(data);
		save.setText("Save...");

		/* One empty labels */
		new Label(shell, SWT.NONE);

		/* Check box use proxy */
		useProxy = new Button(shell, SWT.CHECK);
		useProxy.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		useProxy.setText("use proxy");

		/* Label Proxy */
		proxyLabel = new Label(shell, SWT.NONE);
		proxyLabel.setText("Proxy IP Address");
		proxyLabel.setEnabled(false);
		proxyLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

		/* Text box contains proxy IP address */
		proxyIpAddress = new Text(shell, SWT.BORDER);
		proxyIpAddress.setText("");
		proxyIpAddress.setEnabled(false);
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
							System.err.println("Exception occured...\n" + e.getMessage());
						}

						fileContent += separator;
						totalContent += fileContent;
					}

					// Remove new line character from total content of files
					// Update input list and input count
					if (totalContent.endsWith(separator))
						totalContent = totalContent.substring(0, totalContent.length() - 1);
					inputList.setText(totalContent);
					inputCount.setText("" + inputList.getLineCount());
					bar.setMaximum(Integer.parseInt(inputCount.getText()));

					// Every time re-input, to clear previous result
					outputList.setText("");
					outputListHiden.setText("");
					outputCount.setText("0");
				}
			}
		});

		/* Monitor and handle input list events */
		inputList.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent event) {
				inputCount.setText("" + inputList.getLineCount());
				bar.setMaximum(Integer.parseInt(inputCount.getText()));
			}
		});

		/* Monitor and handle Generate events */
		generate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// Error Message Box
				int style = SWT.ICON_ERROR;
				final MessageBox messageBox = new MessageBox(shell, style);

				// Reset result
				outputList.setText("");
				outputListHiden.setText("");
				outputCount.setText("0");

				bar.setSelection(0);
				
				// Check if user input any word
				if (inputList.getText().equals("")) {
					messageBox.setMessage("There is no word to generate flash cards.");
					messageBox.open();
					return;
				}

				// User has selected to generate flash cards
				final Generator generator = new Generator();

				String input = inputList.getText();
				final String[] wordList = input.split(separator, -1);

				if (proxyIpAddress.isEnabled() && proxyIpAddress.getText().contains(":")) {
					proxyStr = proxyIpAddress.getText();
					System.out.println("proxy String: " + proxyStr);
				} else if (proxyIpAddress.isEnabled() && !proxyIpAddress.getText().contains(":")) {
					messageBox.setMessage("Proxy connection string is not correct.\n" + "Proxy connection string should be like this: 10.10.10.10:8080");
					messageBox.open();
					return;
				}

				// Change the button's text
				generate.setText(IS_RUNNING);

				// Disable elements
				generate.setEnabled(false);
				open.setEnabled(false);
				save.setEnabled(false);

				useProxy.setEnabled(false);
				isUseProxy = proxyLabel.isEnabled() && proxyIpAddress.isEnabled();
				if (isUseProxy) {
					proxyLabel.setEnabled(false);
					proxyIpAddress.setEnabled(false);
				}

				// From here, allow to cancel
				cancel.setEnabled(true);

				// Get content from background thread
				getOxfFlsCards = new Thread(new Runnable() {
					public void run() {
						for (int i = 0; i < wordList.length; i++) {
							final String word = wordList[i];
							if (Thread.currentThread().isInterrupted())
								return;
							System.out.println("INPUT: " + word);
							try {
								final String ankiDeck = generator.generateFlashCards(word, proxyStr);
								if (ankiDeck.contains("Please check your connection")) {
									Display.getDefault().asyncExec(new Runnable() {
										public void run() {
											messageBox.setMessage("Please check your connection...\n" + "Cannot get oxford dictionnary's content.");
											messageBox.open();

											// Change the button's text
											generate.setText(RUN);

											// Change the button's text
											cancel.setEnabled(false);

											// Enable elements
											open.setEnabled(true);
											save.setEnabled(true);
											generate.setEnabled(true);

											useProxy.setEnabled(true);
											if (isUseProxy) {
												proxyLabel.setEnabled(true);
												proxyIpAddress.setEnabled(true);
											}
										}
									});
									return;
								} else if (ankiDeck.contains("THIS WORD DOES NOT EXIST")) {
									wrongSpellingWords += "Line " + (i + 1) + ": " + word + "\n";
								} else {
									Display.getDefault().asyncExec(new Runnable() {
										public void run() {
											outputListHiden.append(ankiDeck);
											outputList.append(generator.wrd + "\t" + generator.wordType + "\t" + generator.phonetic + "\t" + generator.pro_uk + "\t" + generator.pro_us + "\n");
											outputCount.setText("" + (outputList.getLineCount() - 1));

											bar.setSelection(Integer.parseInt(outputCount.getText()));
										}
									});
								}
							} catch (IOException e) {
								System.err.println("Exception occured...\n" + e.getMessage());
								Display.getDefault().asyncExec(new Runnable() {
									public void run() {
										messageBox.setMessage("Please check your connection.\n" + "Maybe proxy connection string is not correct.");
										messageBox.open();

										// Change the button's text
										generate.setText(RUN);

										// Change the button's text
										cancel.setEnabled(false);

										// Enable elements
										open.setEnabled(true);
										save.setEnabled(true);
										generate.setEnabled(true);

										useProxy.setEnabled(true);
										if (isUseProxy) {
											proxyLabel.setEnabled(true);
											proxyIpAddress.setEnabled(true);
										}
									}
								});
								return;
							}
						}

						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								// Change the button's text
								generate.setText(RUN);

								// Message "completed"
								int style = SWT.ICON_INFORMATION;
								MessageBox messageBox = new MessageBox(shell, style);

								messageBox.setMessage("Completed.");
								messageBox.open();

								// Change the button's text
								cancel.setEnabled(false);

								// Enable elements
								open.setEnabled(true);
								save.setEnabled(true);
								generate.setEnabled(true);

								useProxy.setEnabled(true);
								if (isUseProxy) {
									proxyLabel.setEnabled(true);
									proxyIpAddress.setEnabled(true);
								}

								if (Integer.parseInt(outputCount.getText()) < Integer.parseInt(inputCount.getText())) {
									wrongSpellingWords = wrongSpellingWords.substring(0, wrongSpellingWords.length() - 2);
									messageBox.setMessage("Cannot generate flash cards for following words:\n" + wrongSpellingWords);
									messageBox.open();
								}
							}
						});

					}
				});

				getOxfFlsCards.start();
			}
		});

		/* Monitor and handle Save events */
		save.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent event) {

				if (outputList.getText() == "") {
					int style = SWT.ICON_ERROR;
					MessageBox messageBox = new MessageBox(shell, style);
					messageBox.setMessage("There is no card to save. Please generate flash cards first!");
					messageBox.open();
					return;
				}

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
						System.err.println("Exception occured...\n" + e.getMessage());
					} catch (NullPointerException e) {
						System.err.println("Exception occured...\n" + e.getMessage());
					}
				}
			}
		});

		/* Monitor and handle cancel events */
		cancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// interrupt the getOxfFlsCards thread
				getOxfFlsCards.interrupt();

				// Change the button's text
				generate.setText(RUN);

				// Enable elements
				generate.setEnabled(true);
				open.setEnabled(true);
				save.setEnabled(true);

				useProxy.setEnabled(true);
				if (isUseProxy) {
					proxyLabel.setEnabled(true);
					proxyIpAddress.setEnabled(true);
				}

				// Cannot cancel two times for one run
				cancel.setEnabled(false);

				int style = SWT.ICON_INFORMATION;
				MessageBox messageBox = new MessageBox(shell, style);
				messageBox.setMessage("Canceled.");
				messageBox.open();
			}
		});

		/* Monitor and handle use proxy events */
		useProxy.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				// User has selected use proxy
				Button checkBox = (Button) event.getSource();
				System.out.println("useProxy: " + checkBox.getSelection());

				if (checkBox.getSelection() == false) {
					proxyIpAddress.setText("");
				}

				boolean isSelected = checkBox.getSelection();
				proxyLabel.setEnabled(isSelected);
				proxyIpAddress.setEnabled(isSelected);
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

		// End of create Contents of Gui Class
	}

	/**
	 * The dialog to save result
	 */
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

					// We really should read this string from a resource bundle
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
	 * The main method to run app
	 */
	public static void main(String[] args) {
		new Gui().run();
	}

}
