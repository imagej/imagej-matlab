/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2016 Board of Regents of the University of
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

import static org.junit.Assert.assertTrue;
import io.scif.SCIFIOService;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.script.ScriptException;

import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.autoscale.AutoscaleService;
import net.imagej.display.ImageDisplayService;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.type.numeric.RealType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.app.AppService;
import org.scijava.app.StatusService;
import org.scijava.display.Display;
import org.scijava.display.DisplayService;
import org.scijava.options.OptionsService;
import org.scijava.plugins.scripting.matlab.MATLABService;
import org.scijava.script.ScriptModule;
import org.scijava.script.ScriptService;
import org.scijava.widget.WidgetService;

/**
 * Integration tests for ImageJ-MATLAB. Can be run outside of MATLAB, but
 * require a valid MATLAB install.
 *
 * @author Mark Hiner
 */
public class ExternalMATLABIT {

	private Context context;
	private ScriptService scriptService;
	private DatasetService datasetService;
	private DisplayService displayService;
	private MATLABService matlabService;
	private final String threeDims =
		"8bit-signed&pixelType=int8&axes=X,Y,Z&lengths=256,128,3.fake";
	private final String twoDims =
		"8bit-signed&pixelType=int8&axes=X,Y&lengths=16,16.fake";

	private final String sumScript = "% @matrix data\n" //
		+ "% @OUTPUT double[] matrixSum\n" //
		+ "sum(data)\n" //
		+ "sum(ans)\n" //
		+ "matrixSum = sum(ans)"; //

	private final String sumScriptNoImport = "% @OUTPUT double[] matrixSum\n" //
		+ "sum(data)\n" //
		+ "sum(ans)\n" //
		+ "matrixSum = sum(ans)"; //

	final String multScript = "% @matrix A\n" //
		+ "% @matrix B\n" //
		+ "% @OUTPUT double[] matrixSum\n" //
		+ "A*B\n" //
		+ "sum(ans)\n" //
		+ "matrixSum = sum(ans)\n"; //

	@Before
	public void setUp() {
		context =
			new Context(ScriptService.class, DatasetService.class, AppService.class,
				SCIFIOService.class, StatusService.class, ImageDisplayService.class,
				MATLABService.class, DisplayService.class, AutoscaleService.class,
				WidgetService.class, ImageJMATLABService.class, OptionsService.class);
		scriptService = context.getService(ScriptService.class);
		datasetService = context.getService(DatasetService.class);
		displayService = context.getService(DisplayService.class);
		matlabService = context.getService(MATLABService.class);
	}

	@After
	public void tearDown() {
		context.dispose();
		scriptService = null;
		datasetService = null;
		displayService = null;
		matlabService = null;
	}

	/**
	 * Test aliasing of an ImageJ {@link Dataset} to a MATLAB array, for use
	 * within MATLAB.
	 */
	@Test
	public void testParameterAliasing() throws InterruptedException,
		ExecutionException, IOException, ScriptException
	{
		final Dataset dataset = datasetService.open(threeDims);

		final ScriptModule scriptModule =
			scriptService.run("alias.m", sumScript, true, "data", dataset).get();
		final double expected = sum(dataset);
		final Object scriptResult = scriptModule.getOutput("matrixSum");
		final double result = ((double[]) scriptResult)[0];
		assertTrue(0 == Double.compare(expected, result));
	}

	/**
	 * Test {@code @matrix} parameter population via the
	 * {@link MATLABNumericArrayPreprocessor}.
	 */
	@Test
	public void testMatrixPreprocessing() throws InterruptedException,
		ExecutionException, IOException, ScriptException
	{
		final Dataset dataset = datasetService.open(threeDims);

		// Set the dataset as active, to be picked up by preprocessor
		final Display<?> createDisplay = displayService.createDisplay(dataset);
		displayService.setActiveDisplay(createDisplay);

		// NB: no parameters are passed
		final ScriptModule scriptModule =
			scriptService.run("alias.m", sumScript, true).get();
		final double expected = sum(dataset);
		final Object scriptResult = scriptModule.getOutput("matrixSum");
		final double result = ((double[]) scriptResult)[0];
		assertTrue(0 == Double.compare(expected, result));
	}

	/**
	 * Test multiplication of datasets within MATLAB, to ensure the dimensionality
	 * of the data is preserved appropriately.
	 */
	@Test
	public void testMatrixMultiplication() throws InterruptedException,
		ExecutionException, IOException, ScriptException
	{
		final Dataset dataset1 = datasetService.open(twoDims);
		final Dataset dataset2 = datasetService.open(twoDims);

		final ScriptModule scriptModule =
			scriptService.run("multiply.m", multScript, true, "A", dataset1, "B",
				dataset2).get();
		final double expected = multAndSum(dataset1, dataset2);
		final Object scriptResult = scriptModule.getOutput("matrixSum");
		final double result = ((double[]) scriptResult)[0];
		assertTrue(0 == Double.compare(expected, result));
	}

	/**
	 * Test directly setting an ImageJ {@link Dataset} as a variable in a MATLAB
	 * array.
	 */
	@Test
	public void testSettingMatlabVars() throws InterruptedException,
		ExecutionException, IOException, ScriptException
	{
		final Dataset dataset = datasetService.open(threeDims);
		matlabService.getInstances();
		final ImageJMATLABCommands ijCmds =
			matlabService.getInstance(ImageJMATLABCommands.class);

		// Set the dataset as active, to be picked up by preprocessor
		final Display<?> createDisplay = displayService.createDisplay(dataset);
		displayService.setActiveDisplay(createDisplay);

		ijCmds.getDatasetAs("data");

		final ScriptModule scriptModule =
			scriptService.run("noImport.m", sumScriptNoImport, true).get();
		final double expected = sum(dataset);
		final Object scriptResult = scriptModule.getOutput("matrixSum");
		final double result = ((double[]) scriptResult)[0];
		assertTrue(0 == Double.compare(expected, result));
	}

	// -- Helper methods --

	/**
	 * @return sum of matrix multiplication between two datasets
	 */
	private double multAndSum(final Dataset dataset1, final Dataset dataset2) {
		final RandomAccess<RealType<?>> ra1 = dataset1.randomAccess();
		final RandomAccess<RealType<?>> ra2 = dataset2.randomAccess();

		final int[] ra1Pos = new int[2];
		final int[] ra2Pos = new int[2];

		final double[][] raRes =
			new double[(int) dataset1.dimension(0)][(int) dataset2.dimension(1)];

		// compute matrix multiplication
		for (int x = 0; x < dataset1.dimension(0); x++) {
			for (int y = 0; y < dataset2.dimension(1); y++) {
				for (int i = 0; i < dataset1.dimension(1); i++) {
					ra1Pos[0] = x;
					ra1Pos[1] = i;
					ra2Pos[0] = i;
					ra2Pos[1] = y;
					ra1.setPosition(ra1Pos);
					ra2.setPosition(ra2Pos);
					raRes[x][y] +=
						(ra1.get().getRealDouble() * ra2.get().getRealDouble());
				}
			}
		}

		// Sum the result
		double sum = 0;
		for (int i = 0; i < raRes.length; i++) {
			for (int j = 0; j < raRes[0].length; j++) {
				sum += raRes[i][j];
			}
		}

		return sum;
	}

	/**
	 * @return sum of all values in the given dataset
	 */
	private double sum(final Dataset dataset) {
		double sum = 0.0;
		final Cursor<RealType<?>> cursor = dataset.cursor();
		while (cursor.hasNext()) {
			sum += cursor.next().getRealDouble();
		}
		return sum;
	}

}
