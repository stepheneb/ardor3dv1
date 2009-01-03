/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.input.logical;

import com.ardor3d.annotation.Immutable;
import com.ardor3d.input.InputState;

/**
 * Wrapper class to make it possible to use {@link com.google.common.base.Predicate}-based conditions for triggering
 * actions based on user input.
 */
@Immutable
public final class TwoInputStates {
    private final InputState previous;
    private final InputState current;

    /**
     * Instantiates a new TwoInputStates. It is safe for both or either input state to be null, and it is safe for both
     * parameters to point to the same instance.
     * 
     * @param previous
     *            the previous input state
     * @param current
     *            the current input state
     */
    public TwoInputStates(final InputState previous, final InputState current) {
        this.previous = previous;
        this.current = current;
    }

    public InputState getPrevious() {
        return previous;
    }

    public InputState getCurrent() {
        return current;
    }
}
