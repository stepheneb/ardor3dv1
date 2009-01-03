/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.input.lwjgl;

import java.util.EnumMap;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;

import com.ardor3d.input.ButtonState;
import com.ardor3d.input.MouseButton;
import com.ardor3d.input.MouseState;
import com.ardor3d.input.MouseWrapper;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Maps;
import com.google.common.collect.PeekingIterator;

/**
 * Wrapper over the {@link org.lwjgl.input.Mouse} mouse interface class.
 */
public class LwjglMouseWrapper implements MouseWrapper {
    private LwjglMouseIterator currentIterator = null;

    public void init() {
        try {
            Mouse.create();
        } catch (final LWJGLException e) {
            throw new RuntimeException(e);
        }
    }

    public PeekingIterator<MouseState> getEvents() {
        // only create a new iterator if there isn't an existing, valid, one.
        if (currentIterator == null || !currentIterator.hasNext()) {
            currentIterator = new LwjglMouseIterator();
        }

        return currentIterator;
    }

    private static class LwjglMouseIterator extends AbstractIterator<MouseState> implements PeekingIterator<MouseState> {

        @Override
        protected MouseState computeNext() {
            if (!Mouse.next()) {
                return endOfData();
            }

            final EnumMap<MouseButton, ButtonState> buttons = Maps.newEnumMap(MouseButton.class);

            if (Mouse.getButtonCount() > 0 && Mouse.isButtonDown(0)) {
                buttons.put(MouseButton.LEFT, Mouse.isButtonDown(0) ? ButtonState.DOWN : ButtonState.UP);
            }
            if (Mouse.getButtonCount() > 1 && Mouse.isButtonDown(1)) {
                buttons.put(MouseButton.RIGHT, Mouse.isButtonDown(1) ? ButtonState.DOWN : ButtonState.UP);
            }
            if (Mouse.getButtonCount() > 2 && Mouse.isButtonDown(2)) {
                buttons.put(MouseButton.MIDDLE, Mouse.isButtonDown(2) ? ButtonState.DOWN : ButtonState.UP);
            }

            return new MouseState(Mouse.getEventX(), Mouse.getEventY(), Mouse.getEventDX(), Mouse.getEventDY(), Mouse
                    .getEventDWheel(), buttons);
        }
    }
}
