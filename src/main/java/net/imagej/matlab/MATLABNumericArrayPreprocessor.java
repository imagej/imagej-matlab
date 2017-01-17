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

import matlabcontrol.extensions.MatlabNumericArray;
import net.imagej.Dataset;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.process.SingleInputPreprocessor;

import org.scijava.Priority;
import org.scijava.module.process.PreprocessorPlugin;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * {@link PreprocessorPlugin} implementation that converts the active
 * {@link Dataset}s to a {@link MatlabNumericArray}.
 *
 * @author Mark Hiner
 */
@Plugin(type = PreprocessorPlugin.class, priority = Priority.VERY_HIGH_PRIORITY)
public class MATLABNumericArrayPreprocessor extends SingleInputPreprocessor<MatlabNumericArray> {

	@Parameter(required = false)
	private ImageDisplayService imageDisplayService;

	@Parameter(required = false)
	private ImageJMATLABService ijmService;

	public MATLABNumericArrayPreprocessor() {
		super(MatlabNumericArray.class);
	}

	@Override
	public MatlabNumericArray getValue() {
		if (imageDisplayService == null || ijmService == null) return null;
		final Dataset activeDataset = imageDisplayService.getActiveDataset();
		MatlabNumericArray matrix = null;

		if (activeDataset != null) {
			// Convert the active dataset to a MATLAB-compatible array.
			matrix = ijmService.getArray(activeDataset);
		}

		return matrix;
	}

}
