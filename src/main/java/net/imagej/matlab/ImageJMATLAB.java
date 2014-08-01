/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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

import java.lang.reflect.Method;

import net.imagej.ImageJ;
import net.imagej.Main;
import net.imagej.legacy.DefaultLegacyService;

import org.scijava.Context;
import org.scijava.app.App;
import org.scijava.plugins.scripting.matlab.MATLABService;

/**
 * Utility class for properly starting up ImageJ from within MATLAB.
 *
 * @author Mark Hiner
 */
public class ImageJMATLAB {

	private static Context context = null;
	private static MATLABService matlabService = null;
	private static App ijmApp = null;
	private static boolean verbose = true;

	private static final String HELP = "-- Core ImageJ MATLAB commands --\n"
		+ "Usage: ImageJMATLAB.[command]\n"
		+ "\tversion - return the ImageJ-MATLAB version\n"
		+ "\tstart - starts ImageJ-MATLAB\n"
		+ "\thelp - give a brief description of the ImageJ-MATLAB methods\n\n";

	/**
	 * Get the version of ImageJMATLAB.
	 *
	 * @return The version number.
	 */
	public static String version() {
		if (ijmApp != null) {
			return ijmApp.getVersion();
		}
		return "Unknown";
	}

	/**
	 * Starts a new instance of ImageJ from MATLAB.
	 */
	public static void start() {
		start(true);
	}

	/**
	 * Starts a new instance of ImageJ from MATLAB with or without verbose mode.
	 * In verbose mode, status information will be printed to the MATLAB console.
	 *
	 * @param v indicate the verbose mode
	 */
	public static void start(final boolean v) {
		verbose = v;
		start(v, new String[0]);
	}

	/**
	 * Starts a new instance of ImageJ from MATLAB. Verbose mode can be specified,
	 * as well as arguments for the ImageJ startup.
	 *
	 * @see Main#launch(String...)
	 * @param v indicate the verbose mode
	 * @param args arguments to pass to ImageJ
	 */
	public static void start(final boolean v, final String... args) {
		verbose = v;
		launch(args);
	}

	public static String help() {
		return HELP + matlabService.commandHelp();
	}

	public static Context context() {
		return context;
	}

	/**
	 * Starts new instance of ImageJ from MATLAB using command-line arguments
	 */
	private static void launch(String... myargs) {
		// Attempt to resolve any classloader issues before starting ImageJ
		fixContextClassloader();

		// Print ImageJ arguments
		if (myargs == null) myargs = new String[0];
		if (verbose) {
			if (myargs.length > 0) {
				System.out.println("ImageJ> Arguments:");
				for (int i = 0; i < myargs.length; i++)
					System.out.println(myargs[i]);
			}
		}

		// disable the prefservice to avoid conflict with MATLAB.
		ImageJMATLABPrefService.setEnabled(false);

		// Launch ImageJ
		final ImageJ imagej = net.imagej.Main.launch(myargs);
		context = imagej.getContext();

		printStartupInfo();

		// If we have an IJ 1.x, ensure it doesn't exit on quitting
		disableIJExit();

		matlabService = imagej.get(MATLABService.class);

		// Install any available commands
		matlabService.initializeCommands();

		// Print available commands
		System.out.println(help());

		// Print legacy MIJ command usage, if available
		printMIJCommands();

		if (verbose) {
			System.out
				.println("--------------------------------------------------------------");
			System.out.println("Status> ImageJ is running.");
			System.out
				.println("--------------------------------------------------------------");
		}
	}

	/**
	 * Helper method to print MIJ usage if it's present on the classpath.
	 */
	private static void printMIJCommands() {
		Class<?> mij = null;
		try {
			mij = Class.forName("MIJ");
		}
		catch (final ClassNotFoundException e) {
			// No MIJ found, that's ok
			return;
		}

		System.out.println("--- MIJ commands ---\n");
		System.out.println("For backwards compatibility, you can use MIJ to interact with ImageJ 1.x data structures. This is deprecated functionality.");
		Method method;
		try {
			method = mij.getMethod("help");
			final Object mijHelpMsg = method.invoke(null);
			System.out.println(mijHelpMsg.toString());
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Helper method to ensure ImageJ 1.x does not call {@link System#exit(int)}
	 * when it's shutting down, as we do not want to do this when shutting down
	 * within MATLAB.
	 */
	private static void disableIJExit() {
		if (context != null) {
			final DefaultLegacyService legacyService =
				context.getService(DefaultLegacyService.class);

			if (legacyService != null) {
				legacyService.getIJ1Helper().getIJ().exitWhenQuitting(false);
			}
		}
	}

	/**
	 * Helper method to print basic ImageJ-MATLAB startup information.
	 */
	private static void printStartupInfo() {
		if (verbose) {
			System.out
				.println("--------------------------------------------------------------");
			System.out.println("ImageJ-MATLAB " + version() +
				": MATLAB to ImageJ Interface");
			System.out
				.println("--------------------------------------------------------------");
			System.out.println("Help: ImageJMATLAB.help");
			final Runtime runtime = Runtime.getRuntime();
			System.out.println("JVM> " + version());
			System.out.println("JVM> Version: " + System.getProperty("java.version"));
			System.out.println("JVM> Total amount of memory: " +
				Math.round(runtime.totalMemory() / 1024) + " Kb");
			System.out.println("JVM> Amount of free memory: " +
				Math.round(runtime.freeMemory() / 1024) + " Kb");
		}
	}

	/**
	 * Helper method to ensure the appropriate {@link ClassLoader} is used.
	 */
	private static void fixContextClassloader() {
		final ClassLoader threadCL = Thread.currentThread().getContextClassLoader();
		final ClassLoader imagejMATLABCL = ImageJMATLAB.class.getClassLoader();

		// Check if the ImageJ class loader is in the current thread's class
		// loader's ancestry. If so, we can return and everything is OK
		for (ClassLoader loader = threadCL; loader != null; loader =
			loader.getParent())
		{
			if (imagejMATLABCL == loader) return;
		}

		// The ImageJ class loader wasn't known to this thread, so we will have to
		// override this thread's contextClassLoader. We should check if this
		// thread's class loader is in the ancestry of the ImageJ class loader.
		// If not, then classes may/will be lost. All we can do is warn though.
		boolean ok = false;
		for (ClassLoader loader = imagejMATLABCL; loader != null; loader =
			loader.getParent())
		{
			if (threadCL == loader) {
				ok = true;
				break;
			}
		}

		if (!ok) {
			System.err.println("Warning: replacing context classloader with "
				+ "incompatible classloader because ImageJ was not found.");
		}

		Thread.currentThread().setContextClassLoader(imagejMATLABCL);
	}
}
