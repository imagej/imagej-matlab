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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scijava.Context;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.prefs.DefaultPrefService;
import org.scijava.prefs.PrefService;
import org.scijava.service.Service;

/**
 * A {@link PrefService} implementation specific for use within MATLAB. MATLAB
 * has known bugs regarding {@code java.util.prefs.Preferences}, so we need the
 * ability to disable their use, via a {@link #setEnabled(boolean)} method.
 *
 * @author Mark Hiner
 */
@Plugin(type = Service.class, priority = Priority.HIGH_PRIORITY)
public class ImageJMATLABPrefService extends DefaultPrefService {

	private static boolean enabled = true;

	// -- Static Methods --

	/**
	 * Sets whether or not this {@link PrefService} is enabled. If so, it will
	 * behave like a {@link DefaultPrefService}. If not, all methods will return
	 * empty objects, 0 values, or be NO-OPs.
	 * <p>
	 * NB: this method needs to be static so that it can be called before a
	 * {@link Context} is created.
	 * </p>
	 */
	public static void setEnabled(final boolean enabled) {
		ImageJMATLABPrefService.enabled = enabled;
	}

	// -- PrefService API --

	@Override
	public String get(final String name) {
		return enabled ? super.get(name) : "";
	}

	@Override
	public String get(final String name, final String defaultValue) {
		return enabled ? super.get(name, defaultValue) : "";
	}

	@Override
	public boolean getBoolean(final String name, final boolean defaultValue) {
		return enabled ? super.getBoolean(name, defaultValue) : false;
	}

	@Override
	public double getDouble(final String name, final double defaultValue) {
		return enabled ? super.getDouble(name, defaultValue) : 0;
	}

	@Override
	public float getFloat(final String name, final float defaultValue) {
		return enabled ? super.getFloat(name, defaultValue) : 0;
	}

	@Override
	public int getInt(final String name, final int defaultValue) {
		return enabled ? super.getInt(name, defaultValue) : 0;
	}

	@Override
	public long getLong(final String name, final long defaultValue) {
		return enabled ? super.getLong(name, defaultValue) : 0;
	}

	@Override
	public void put(final String name, final String value) {
		if (enabled) super.put(name, value);
	}

	@Override
	public void put(final String name, final boolean value) {
		if (enabled) super.put(name, value);
	}

	@Override
	public void put(final String name, final double value) {
		if (enabled) super.put(name, value);
	}

	@Override
	public void put(final String name, final float value) {
		if (enabled) super.put(name, value);
	}

	@Override
	public void put(final String name, final int value) {
		if (enabled) super.put(name, value);
	}

	@Override
	public void put(final String name, final long value) {
		if (enabled) super.put(name, value);
	}

	@Override
	public String get(final Class<?> c, final String name) {
		return enabled ? super.get(c, name) : "";
	}

	@Override
	public String get(final Class<?> c, final String name,
		final String defaultValue)
	{
		return enabled ? super.get(c, name, defaultValue) : "";
	}

	@Override
	public boolean getBoolean(final Class<?> c, final String name,
		final boolean defaultValue)
	{
		return enabled ? super.getBoolean(c, name, defaultValue) : false;
	}

	@Override
	public double getDouble(final Class<?> c, final String name,
		final double defaultValue)
	{
		return enabled ? super.getDouble(c, name, defaultValue) : 0;
	}

	@Override
	public float getFloat(final Class<?> c, final String name,
		final float defaultValue)
	{
		return enabled ? super.getFloat(c, name, defaultValue) : 0;
	}

	@Override
	public int
		getInt(final Class<?> c, final String name, final int defaultValue)
	{
		return enabled ? super.getInt(c, name, defaultValue) : 0;
	}

	@Override
	public long getLong(final Class<?> c, final String name,
		final long defaultValue)
	{
		return enabled ? super.getLong(c, name, defaultValue) : 0;
	}

	@Override
	public void put(final Class<?> c, final String name, final String value) {
		if (enabled) super.put(c, name, value);
	}

	@Override
	public void put(final Class<?> c, final String name, final boolean value) {
		if (enabled) super.put(c, name, value);
	}

	@Override
	public void put(final Class<?> c, final String name, final double value) {
		if (enabled) super.put(c, name, value);
	}

	@Override
	public void put(final Class<?> c, final String name, final float value) {
		if (enabled) super.put(c, name, value);
	}

	@Override
	public void put(final Class<?> c, final String name, final int value) {
		if (enabled) super.put(c, name, value);
	}

	@Override
	public void put(final Class<?> c, final String name, final long value) {
		if (enabled) super.put(c, name, value);
	}

	@Override
	public void clear(final Class<?> c) {
		if (enabled) super.clear(c);
	}

	@Override
	public void clearAll() {
		if (enabled) super.clearAll();
	}

	@Override
	public void clear(final String key) {
		if (enabled) super.clear(key);
	}

	@Override
	public void clear(final Class<?> prefClass, final String key) {
		if (enabled) super.clear(prefClass, key);
	}

	@Override
	public void remove(final Class<?> prefClass, final String key) {
		if (enabled) super.remove(prefClass, key);
	}

	@Override
	public void putMap(final Map<String, String> map, final String key) {
		if (enabled) super.putMap(map, key);
	}

	@Override
	public void putMap(final Class<?> prefClass, final Map<String, String> map,
		final String key)
	{
		if (enabled) super.putMap(prefClass, map, key);
	}

	@Override
	public void putMap(final Class<?> prefClass, final Map<String, String> map) {
		if (enabled) super.putMap(prefClass, map);
	}

	@Override
	public Map<String, String> getMap(final String key) {
		return enabled ? super.getMap(key) : new HashMap<String, String>();
	}

	@Override
	public Map<String, String> getMap(final Class<?> prefClass, final String key)
	{
		return enabled ? super.getMap(prefClass, key)
			: new HashMap<String, String>();
	}

	@Override
	public Map<String, String> getMap(final Class<?> prefClass) {
		return enabled ? super.getMap(prefClass) : new HashMap<String, String>();
	}

	@Override
	public void putList(final List<String> list, final String key) {
		if (enabled) super.putList(list, key);
	}

	@Override
	public void putList(final Class<?> prefClass, final List<String> list,
		final String key)
	{
		if (enabled) super.putList(prefClass, list, key);
	}

	@Override
	public void putList(final Class<?> prefClass, final List<String> list) {
		if (enabled) super.putList(prefClass, list);
	}

	@Override
	public List<String> getList(final String key) {
		return enabled ? super.getList(key) : new ArrayList<String>();
	}

	@Override
	public List<String> getList(final Class<?> prefClass, final String key) {
		return enabled ? super.getList(prefClass, key) : new ArrayList<String>();
	}

	@Override
	public List<String> getList(final Class<?> prefClass) {
		return enabled ? super.getList(prefClass) : new ArrayList<String>();
	}
}
