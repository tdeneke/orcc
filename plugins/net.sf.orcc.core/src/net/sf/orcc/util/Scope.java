/*
 * Copyright (c) 2009, IETR/INSA of Rennes
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   * Neither the name of the IETR/INSA of Rennes nor the names of its
 *     contributors may be used to endorse or promote products derived from this
 *     software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package net.sf.orcc.util;

import net.sf.orcc.OrccRuntimeException;
import net.sf.orcc.ir.Location;

/**
 * This class defines a scope as an ordered map extended with the notion of
 * hierarchy.
 * 
 * @author Matthieu Wipliez
 * 
 */
public class Scope<K, V> extends OrderedMap<K, V> {

	/**
	 * if <code>true</code>, a variable is allowed to override a variable with
	 * the same name
	 */
	private boolean allowOverride;

	/**
	 * parent scope
	 */
	private Scope<K, V> parent;

	/**
	 * Creates a top-level scope.
	 */
	public Scope() {
		this(null, true);
	}

	/**
	 * Creates a scope with the given parent.
	 * 
	 * @param parent
	 *            the parent scope
	 * @param allowOverride
	 *            if <code>true</code>, a variable is allowed to override a
	 *            variable with the same name
	 */
	public Scope(Scope<K, V> parent, boolean allowOverride) {
		this.parent = parent;
		this.allowOverride = allowOverride;
	}

	/**
	 * Returns the object that has the given name. If the object is not found in
	 * the current scope, the parent scope is queried.
	 * 
	 * @param name
	 *            the name of an object.
	 * @return the object that has the given name, or <code>null</code>
	 */
	@Override
	public V get(K name) {
		return get(name, true);
	}

	/**
	 * Returns the object that has the given name. If the object is not found in
	 * the current scope and checkParent is <code>true</code>, the parent scope
	 * is queried.
	 * 
	 * @param name
	 *            the name of an object.
	 * @param checkParent
	 *            if <code>true</code>, the parent is checked
	 * @return the object that has the given name, or <code>null</code>
	 */
	public V get(K name, boolean checkParent) {
		V object = super.get(name);
		if (object == null) {
			if (parent != null && checkParent) {
				// query parent
				return parent.get(name);
			} else {
				return null;
			}
		} else {
			// object found
			return object;
		}
	}

	/**
	 * Returns the parent of this scope
	 * 
	 * @return a scope, or <code>null</code> if this scope has no parent
	 */
	public Scope<K, V> getParent() {
		return parent;
	}

	/**
	 * Returns <code>true</code> if a variable is allowed to override a variable
	 * with the same name.
	 * 
	 * @return <code>true</code> if a variable is allowed to override a variable
	 *         with the same name
	 */
	public boolean isOverrideAllowed() {
		return allowOverride;
	}

	/**
	 * Adds an object to this ordered map with the given name. The file and
	 * location information are only used for error reporting if the object is
	 * already present in the map and {@link #isOverrideAllowed()} is
	 * <code>true</code>.
	 * 
	 * @param file
	 *            the file where the object located
	 * @param location
	 *            the location of the object
	 * @param name
	 *            the name of an object
	 * @param object
	 *            an object
	 * @throws OrccRuntimeException
	 *             if the object is already defined
	 */
	@Override
	public void put(String file, Location location, K name, V object)
			throws OrccRuntimeException {
		if (allowOverride) {
			// a variable is allowed to override a variable defined in a
			// different scope
			super.put(file, location, name, object);
		} else {
			// check if there already is a variable with the same name in this
			// scope or parent scopes
			V existingObject = super.get(name);
			if (existingObject == null) {
				// no existing variable in this scope, check parent's
				if (parent != null) {
					existingObject = parent.get(name, false);
					if (existingObject != null) {
						throw new OrccRuntimeException(file, location, "\""
								+ name + "\" already defined in parent scope");
					}
				}
			} else {
				throw new OrccRuntimeException(file, location, "\"" + name
						+ "\" already defined in this scope");
			}

			// no existing object in this scope, nor in parent's => we're cool
			putNoCheck(name, object);
		}
	}

	@Override
	public String toString() {
		String res = super.toString();
		if (parent != null) {
			res += "\n" + parent;
		}
		return res;
	}

}
