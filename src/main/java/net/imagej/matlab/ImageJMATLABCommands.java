/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
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

import javax.script.ScriptEngine;

import net.imagej.Dataset;
import net.imagej.display.ImageDisplayService;

import org.scijava.log.LogService;
import org.scijava.plugin.AbstractRichPlugin;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugins.scripting.matlab.MATLABCommands;
import org.scijava.plugins.scripting.matlab.MATLABService;
import org.scijava.script.ScriptService;
import org.scijava.ui.UIService;

import matlabcontrol.extensions.MatlabNumericArray;

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

	@Parameter(required = false)
	private UIService uiService;

	@Parameter
	private LogService logService;

	@Parameter
	private ScriptService scriptService;

	@Parameter
	private ImageJMATLABService ijmService;

	@Parameter
	private MATLABService matlabService;

	public static final String NAME = "IJM";

	// -- ImageJ-MATLAB commands --

	/**
	 * Converts the active {@link Dataset} to a MATLAB matrix, stored in a
	 * variable using {@link Dataset#getName()}.
	 */
	public void getDataset() {
		importDataset(null);
	}

	/**
	 * As {@link #getDataset()}, using the specified variable name.
	 */
	public void getDatasetAs(final String name) {
		importDataset(name);
	}

	/**
	 * Take an array variable in MATLAB and attempt to display it as a Dataset
	 * in ImageJ
	 */
	public void show(final String matrix) {
		if (uiService == null) {
			logService.info("No UI available to display array");
			return;
		}

		final ScriptEngine engine =
				scriptService.getLanguageByName("MATLAB").getScriptEngine();
		final Object o = engine.get(matrix);
		MatlabNumericArray array = null;

		if (o instanceof MatlabNumericArray) array = (MatlabNumericArray) o;

		if (array == null) {
			logService.info("Variable of name: " + matrix + " is not an array.");
			return;
		}

		uiService.show(ijmService.getDataset(array));
	}

	// -- MATLABCommands methods --

	@Override
	public String help() {
		final String usage =
			"-- ImageJ MATLAB commands --\n\n" + "Usage: IJM.[command]\n"
				+ "\thelp - prints a brief description of available commands\n"
				+ "\tgetDataset - creates a MATLAB matrix from the active ImageJ image\n"
				+ "\tgetDatasetAs(name) - creates a MATLAB matrix from the active "
				+ "ImageJ image, and assigns it to the specified variable name\n"
				+ "\tshow(name) - takes the MATLAB matrix with the specified name and displays it as an image";
		return usage;
	}

	// -- Helper methods --

	/**
	 * Helper method to perform {@link Dataset} conversion, and set the variable
	 * within MATLAB.
	 */
	private void importDataset(String name) {

		final Dataset activeDataset = imageDisplayService.getActiveDataset();

		if (activeDataset == null) {
			logService.info("No active image. Please open an image in ImageJ first.");
			return;
		}

		if (name == null) name = activeDataset.getName();

		// Convert the active dataset to a MATLAB-compatible array.
		final MatlabNumericArray matrix = ijmService.getArray(activeDataset);

		matlabService.makeMATLABVariable(name, matrix);
	}

}
