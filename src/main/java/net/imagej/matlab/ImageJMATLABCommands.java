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

import matlabcontrol.extensions.MatlabNumericArray;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplayService;

import org.scijava.log.LogService;
import org.scijava.plugin.AbstractRichPlugin;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugins.scripting.matlab.MATLABCommands;
import org.scijava.plugins.scripting.matlab.MATLABService;

/**
 * {@link MATLABCommands} suite for converting ImageJ data structures to MATLAB
 * structures.
 *
 * @author Mark Hiner
 */
@Plugin(type = MATLABCommands.class, name = ImageJMATLABCommands.NAME)
public class ImageJMATLABCommands extends AbstractRichPlugin implements
	MATLABCommands
{

	@Parameter(required = false)
	private ImageDisplayService imageDisplayService;

	@Parameter
	private ImageJMATLABService ijmService;

	@Parameter
	private MATLABService matlabService;

	@Parameter
	private LogService logService;

	public static final String NAME = "IJM";

	// -- ImageJ-MATLAB commands --

	/**
	 * Converts the active {@link Dataset} to a MATLAB matrix, stored in a
	 * variable using {@link Dataset#getName()}.
	 */
	public void importDataset() {
		final Dataset activeDataset = imageDisplayService.getActiveDataset();

		importDataset(activeDataset.getName(), activeDataset);
	}

	/**
	 * As {@link #importDataset()}, using the specified variable name.
	 */
	public void importDataset(final String name) {
		final Dataset activeDataset = imageDisplayService.getActiveDataset();

		importDataset(name, activeDataset);
	}

	// -- MATLABCommands methods --

	@Override
	public String usage() {
		final String usage =
			"-- ImageJ MATLAB commands --\n" + "Usage: IJM.[command]\n"
				+ "\timportDataset - creates a MATLAB matrix from the active dataset\n"
				+ "\timportDataset(name) - creates a MATLAB matrix with the given"
				+ " name from the active dataset";
		return usage;
	}

	// -- Helper methods --

	/**
	 * Helper method to perform {@link Dataset} conversion, and set the variable
	 * within MATLAB.
	 */
	private void importDataset(final String name, final Dataset dataset) {
		if (dataset == null) {
			logService.error("No active dataset to import to MATLAB.");
		}

		// Convert the active dataset to a MATLAB-compatible array.
		final MatlabNumericArray matrix = ijmService.getArray(dataset);

		matlabService.makeMATLABVariable(name, matrix);
	}

}
