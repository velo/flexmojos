package info.rvin.flexmojo.test;

import info.rvin.mojo.flexmojo.AbstractIrvinMojo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.MessageFormat;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * 
 * based on:
 * http://weblogs.macromedia.com/pmartin/archives/2007/09/flexunit_for_an_2.cfm
 * 
 * @goal test-run
 * @requiresDependencyResolution
 * @phase test
 * 
 */
public class FlexUnitMojo extends AbstractIrvinMojo {

	private static final String END_OF_TEST_RUN = "<endOfTestRun/>";
	private static final String END_OF_TEST_SUITE = "</testsuite>";
	private static final String END_OF_TEST_ACK = "<endOfTestRunAck/>";
	private static final char NULL_BYTE = '\u0000';

	private static final String POLICY_FILE_REQUEST = "<policy-file-request/>";
	final static String DOMAIN_POLICY = "<cross-domain-policy><allow-access-from domain=\"*\" to-ports=\"{0}\" /></cross-domain-policy>";

	private boolean failures = false;
	private boolean complete;
	private boolean verbose = true;

	// attributes from ant task def
	private int port = 1024;
	private int socketTimeout = 60000; // milliseconds
	private boolean failOnTestFailure = true;
	private File swf;

	private MojoExecutionException executionError; // BAD IDEA

	/**
	 * @parameter default-value="false" expression="maven.test.skip"
	 */
	private boolean skipTests;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		File testFolder = new File(build.getTestSourceDirectory());
		if (skipTests) {
			// getLog().warn("Skipping test phase.");
		} else if (!testFolder.exists()) {
			// getLog().warn("Test folder not found" + testFolder);
		} else {
			super.execute();
		}
	}

	/**
	 * Called by Ant to execute the task.
	 */
	@Override
	protected void setUp() throws MojoExecutionException, MojoFailureException {
		swf = new File(build.getTestOutputDirectory(), "TestRunner.swf");

		// Start a thread that receives the FlexUnit results.
		receiveFlexUnitResults();
	}

	/**
	 * Create a server socket for receiving the test reports from FlexUnit. We
	 * read the test reports inside of a Thread.
	 */
	private void receiveFlexUnitResults() throws MojoExecutionException {
		// Start a thread to accept a client connection.
		final Thread thread = new Thread() {
			private ServerSocket serverSocket = null;
			private Socket clientSocket = null;
			private InputStream in = null;
			private OutputStream out = null;

			public void run() {
				try {
					openServerSocket();
					openClientSocket();

					StringBuffer buffer = new StringBuffer();
					int bite = -1;

					while ((bite = in.read()) != -1) {
						final char chr = (char) bite;

						if (chr == NULL_BYTE) {
							final String data = buffer.toString();
							buffer = new StringBuffer();

							if (data.equals(POLICY_FILE_REQUEST)) {
								sendPolicyFile();
								closeClientSocket();
								openClientSocket();
							} else if (data.endsWith(END_OF_TEST_SUITE)) {
								saveTestReport(data);
							} else if (data.equals(END_OF_TEST_RUN)) {
								sendAcknowledgement();
							}
						} else {
							buffer.append(chr);
						}
					}
				} catch (MojoExecutionException be) {
					executionError = be;

					try {
						sendAcknowledgement();
					} catch (IOException e) {
						// ignore
					}
				} catch (SocketTimeoutException e) {
					executionError = new MojoExecutionException(
							"timeout waiting for flexunit report", e);
				} catch (IOException e) {
					executionError = new MojoExecutionException(
							"error receiving report from flexunit", e);
				} finally {
					// always stop the server loop
					complete = true;

					closeClientSocket();
					closeServerSocket();
				}
			}

			private void sendPolicyFile() throws IOException {
				out.write(MessageFormat.format(DOMAIN_POLICY,
						new Object[] { Integer.toString(port) }).getBytes());

				out.write(NULL_BYTE);

				if (verbose) {
					log("sent policy file");
				}
			}

			private void saveTestReport(final String report)
					throws MojoExecutionException {
				writeTestReport(report);

				if (verbose) {
					log("end of test");
				}
			}

			private void sendAcknowledgement() throws IOException {
				out.write(END_OF_TEST_ACK.getBytes());
				out.write(NULL_BYTE);

				if (verbose) {
					log("end of test run");
				}
			}

			private void openServerSocket() throws IOException {
				serverSocket = new ServerSocket(port);
				serverSocket.setSoTimeout(socketTimeout);

				if (verbose) {
					log("opened server socket");
				}
			}

			private void closeServerSocket() {
				if (serverSocket != null) {
					try {
						serverSocket.close();
					} catch (IOException e) {
						// ignore
					}
				}
			}

			private void openClientSocket() throws IOException {
				// This method blocks until a connection is made.
				clientSocket = serverSocket.accept();

				if (verbose) {
					log("accepting data from client");
				}

				in = clientSocket.getInputStream();
				out = clientSocket.getOutputStream();
			}

			private void closeClientSocket() {
				// Close the output stream.
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						// ignore
					}
				}

				// Close the input stream.
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						// ignore
					}
				}

				// Close the client socket.
				if (clientSocket != null) {
					try {
						clientSocket.close();
					} catch (IOException e) {
						// ignore
					}
				}
			}
		};

		thread.start();
	}

	/**
	 * Write a test report to disk.
	 * 
	 * @param report
	 *            the report to write.
	 * @throws MojoExecutionException
	 */
	private void writeTestReport(final String report)
			throws MojoExecutionException {
		try {
			// Parse the report.
			final Document document = DocumentHelper.parseText(report);

			// Get the test attributes.
			final Element root = document.getRootElement();
			final String name = root.valueOf("@name");
			final int numFailures = Integer.parseInt(root.valueOf("@failures"));

			if (verbose)
				log("Running " + name);
			if (verbose)
				log(formatLogReport(root));

			// Get the output file name.
			final File file = new File(build.getTestOutputDirectory(),
					"TestReport.xml");

			// Pretty print the document to disk.
			final OutputFormat format = OutputFormat.createPrettyPrint();
			final XMLWriter writer = new XMLWriter(new FileOutputStream(file),
					format);
			writer.write(document);
			writer.close();

			// First write the report, then fail the build if the test failed.
			if (numFailures > 0) {
				failures = true;

				if (verbose) {
					log("flexunit test " + name + " failed.");
				}

				if (failOnTestFailure) {
					throw new MojoExecutionException("flexunit test " + name
							+ " failed.");
				}
			}

		} catch (MojoExecutionException be) {
			throw be;
		} catch (Exception e) {
			throw new MojoExecutionException("error writing report to disk", e);
		}
	}

	/**
	 * crafts a simple junit type log message.
	 */
	private String formatLogReport(final Element root) {
		int numFailures = Integer.parseInt(root.valueOf("@failures"));
		int numErrors = Integer.parseInt(root.valueOf("@errors"));
		int numTests = Integer.parseInt(root.valueOf("@tests"));
		int time = Integer.parseInt(root.valueOf("@time"));

		final StringBuffer msg = new StringBuffer();
		msg.append("Tests run: ");
		msg.append(numTests);
		msg.append(", Failures: ");
		msg.append(numFailures);
		msg.append(", Errors: ");
		msg.append(numErrors);
		msg.append(", Time Elapsed: ");
		msg.append(time);
		msg.append(" sec");

		return msg.toString();
	}

	/**
	 * Failure handling. Write ant property for use later in build script.
	 * 
	 * @throws MojoExecutionException
	 */
	private void handleFailures() throws MojoExecutionException {
		if (failures) {
			throw new MojoExecutionException("Some tests fail");
		}
	}

	public void log(final String message) {
		System.out.println(message);
	}

	@Override
	protected void run() throws MojoExecutionException, MojoFailureException {
		// Start the browser and run the FlexUnit tests.
		final FlexUnitLauncher browser = new FlexUnitLauncher();
		try {
			browser.runTests(swf);
		} catch (Exception e) {
			throw new MojoExecutionException(
					"Error launching the test runner.", e);
		}

		// Wait until the tests are complete.
		while (!complete) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new MojoExecutionException(e.getMessage(), e);
			}
		}

	}

	@Override
	protected void tearDown() throws MojoExecutionException,
			MojoFailureException {

		if (executionError != null) {
			throw executionError;
		}

		handleFailures();
	}
}
