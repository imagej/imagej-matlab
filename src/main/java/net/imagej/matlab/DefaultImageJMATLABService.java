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

import java.lang.reflect.Array;

import matlabcontrol.extensions.MatlabNumericArray;
import matlabcontrol.extensions.MatlabNumericArray.DoubleArrayType;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.Cursor;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.DoubleType;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;
import org.scijava.util.ClassUtils;

/**
 * Default {@link ImageJMATLABService} implementation.
 *
 * @author Mark Hiner
 */
@Plugin(type = Service.class)
public class DefaultImageJMATLABService extends AbstractService implements
	ImageJMATLABService
{

	@Parameter
	private DatasetService datasetService;

	@Override
	public MatlabNumericArray getArray(final Dataset dataset) {
		return convertToArray(dataset);
	}

	@Override
	public Dataset getDataset(final MatlabNumericArray array) {
		return convertToDataset(array);
	}

	// -- Helper methods: to array --

	/**
	 * Helper method to convert a {@link Dataset} to a {@link MatlabNumericArray}.
	 */
	private MatlabNumericArray convertToArray(final Dataset dataset) {
		// For tracking cursor position
		final int[] pos = new int[dataset.numDimensions()];

		// We need to use reflection to create nested double arrays, so these
		// cache the order of the array classes and lengths.
		final int[] arrayLengths = new int[dataset.numDimensions()];
		final Class<?>[] arrayClasses = new Class[pos.length];

		// Base component type == double
		Class<?> c = double.class;

		// We want the "outermost" class at the start of the class array, so build
		// backwards
		arrayClasses[pos.length - 1] = c;
		arrayLengths[0] = (int) dataset.dimension(0);

		// Generate nested array classes
		for (int d = 1; d < pos.length; d++) {
			c = ClassUtils.getArrayClass(c);
			arrayClasses[pos.length - 1 - d] = c;
			arrayLengths[d] = (int) dataset.dimension(d);
		}

		// Construct our base array
		final Object arrays = Array.newInstance(arrayClasses[0], arrayLengths[0]);

		// Populate each array position
		final Cursor<RealType<?>> cursor = dataset.localizingCursor();
		while (cursor.hasNext()) {
			final RealType<?> next = cursor.next();
			cursor.localize(pos);
			populate(arrays, arrayClasses, arrayLengths, pos, next);
		}

		// Get the MatlabControl array type
		@SuppressWarnings("rawtypes")
		final DoubleArrayType type =
			MatlabNumericArray.DoubleArrayType.getInstance(arrays.getClass());

		// Construct and return a new MatlabNumericArray
		@SuppressWarnings("unchecked")
		final MatlabNumericArray result =
			new MatlabNumericArray(type, arrays, null);

		return result;
	}

	/**
	 * Sets the given value at the indicated position within the baseArray.
	 * Creates intermediate arrays if needed using the array class and length
	 * lists.
	 */
	private void populate(final Object baseArray, final Class<?>[] arrayClasses,
		final int[] arrayLengths, final int[] position, final RealType<?> value)
	{
		Object currentArray = baseArray;
		// Each position index corresponds to one dimension in the dataset
		for (int i = 0; i < position.length; i++) {
			final int index = position[i];
			// The last position takes the actual value we were given
			if (i == position.length - 1) {
				Array.set(currentArray, index, value.getRealDouble());
			}
			else {
				// For intermediate dimensions, we descend the nested array structure,
				// building new arrays if needed.
				if (Array.get(currentArray, index) == null) {
					// NB: i is the CURRENT index into these arrays
					final int nextArrayIndex = i + 1;
					final Object nextArray =
						Array.newInstance(arrayClasses[nextArrayIndex],
							arrayLengths[nextArrayIndex]);
					Array.set(currentArray, index, nextArray);
				}
				// Update the currentArray pointer.
				currentArray = Array.get(currentArray, index);
			}
		}
	}

	// -- Helper methods: to dataset --

	/**
	 * Helper method to convert a {@link MatlabNumericArray} to a {@link Dataset}.
	 */
	private Dataset convertToDataset(final MatlabNumericArray array) {
		final int[] lengths = array.getLengths();

		final long[] dims = new long[lengths.length];
		for (int i=0; i<dims.length; i++) {
			dims[i] = (lengths[i]);
		}

		final AxisType[] axes = new AxisType[lengths.length];

		// Populate AxisType array
		// In MATLAB, first two axes are X,Y. Subsequent axes are "pages"
		for (int i=0; i<axes.length; i++) {
			if (i == 0) axes[i] = Axes.X;
			else if (i == 1) axes[i] = Axes.Y;
			else axes[i] = Axes.get("Page " + (i - 2), false);
		}

		final Dataset dataset =
			datasetService.create(new DoubleType(), dims, null, axes);

		// Copy the data
		final Cursor<RealType<?>> cursor = dataset.localizingCursor();
		int pos = 0;
		while (cursor.hasNext()) {
			cursor.fwd();
			cursor.get().setReal(array.getRealValue(pos++));
		}

		return dataset;
	}

}
