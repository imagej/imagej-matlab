/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2015 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package net.imagej.matlab;

import org.scijava.script.ScriptModule;
import org.scijava.script.ScriptService;
import org.scijava.thread.ThreadService;

/**
 * ImageJ-MATLAB integration tests. These need to be run from within MATLAB.
 * This is due to the inability to transfer complex objects back and forth
 * between a test JVM and MATLAB's (which would potentially present class
 * loading issues anyway).
 *
 * @author Mark Hiner
 */
public class InternalMATLABIT {

	/**
	 * Verify that parameters are being passed correctly to MATLAB scripts, and
	 * that we can use complex SciJava or ImageJ classes from within MATLAB.
	 */
	public void testParameters() {
		ImageJMATLAB.start();
		final ScriptService scriptService =
			ImageJMATLAB.context().getService(ScriptService.class);
		final ThreadService threadService =
			ImageJMATLAB.context().getService(ThreadService.class);

		// Ensure the test is run on a separate Thread. When a class is called
		// from within MATLAB it is like executing a function, and thus blocks
		// until it returns. In this case, that will prevent actual use of MATLAB
		// operations, since they will deadlock with this method itself.
		threadService.run(new Runnable() {

			@Override
			public void run() {
				// The goal of this script is to pass in a ScriptService object to
				// MATLAB and use its getLanguageByName method from within MATLAB. Then
				// we can check the results from the same method called from this test.
				final String script =
					""
						+ "% @ScriptService scriptService\n"
						+ "% @OUTPUT String language\n"
						+ "language = scriptService.getLanguageByName('MATLAB').getLanguageName";
				try {
					final ScriptModule m =
						scriptService.run("hello.m", script, true).get();
					final Object actual = m.getOutput("language");
					final String expected =
						scriptService.getLanguageByName("MATLAB").getLanguageName();

					// Since this test is running from within MATLAB, we need to send
					// our success and failure messages to the MATLAB console. So
					// we just run scripts to print the appropriate message.
					if (expected.equals(actual)) {
						final String success = "disp('Parameters test passed!')";
						scriptService.run("success.m", success, true);
						return;
					}
					final String success =
						"disp('Test failed, results are not equal:')\n" +
							"disp('Expected: " + expected + "')\n" + "disp('Actual: " +
							actual.toString() + "')";
					scriptService.run("success.m", success, true);
				}
				catch (final Exception e) {
					System.err.println("MATLABScriptingIT#testParameters failed: " +
						e.getMessage());
				}
			}
		});
	}

}
