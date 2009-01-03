/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.input;

import java.util.EnumMap;
import java.util.EnumSet;

import com.ardor3d.annotation.Immutable;
import com.google.common.collect.Maps;

/**
 * Describes the mouse state at some point in time.
 */
@Immutable
public class MouseState {
    private final int x;
    private final int y;
    private final int dx;
    private final int dy;
    private final int dwheel;
    private final EnumMap<MouseButton, ButtonState> buttonStates = Maps.newEnumMap(MouseButton.class);
    public static final MouseState NOTHING = new MouseState(0, 0, 0, 0, 0, null);

    /**
     * Constructs a new MouseState instance.
     * 
     * @param x
     *            the mouse's x position
     * @param y
     *            the mouse's y position
     * @param dx
     *            the delta in the mouse's x position since the last update
     * @param dy
     *            the delta in the mouse's y position since the last update
     * @param dwheel
     *            the delta in the mouse's wheel movement since the last update
     * @param buttonStates
     *            the states of the various given buttons.
     */
    public MouseState(final int x, final int y, final int dx, final int dy, final int dwheel,
            final EnumMap<MouseButton, ButtonState> buttonStates) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.dwheel = dwheel;
        if (buttonStates != null) {
            this.buttonStates.putAll(buttonStates);
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    public int getDwheel() {
        return dwheel;
    }

    /**
     * Returns all the buttons' states. It could be easier for most classes to use the {@link #getLeftButtonState()} and
     * {@link #getRightButtonState()} methods, and that also results in less object creation.
     * 
     * @return a defensive copy of the states of all the buttons at this point in time.
     */
    public EnumMap<MouseButton, ButtonState> getButtonStates() {
        return buttonStates.clone();
    }

    public ButtonState getButtonState(final MouseButton button) {
        if (buttonStates.containsKey(button)) {
            return buttonStates.get(button);
        }

        return ButtonState.UP;
    }

    public EnumSet<MouseButton> getButtonsReleasedSince(final MouseState previous) {
        final EnumSet<MouseButton> result = EnumSet.noneOf(MouseButton.class);
        for (final MouseButton button : MouseButton.values()) {
            if (previous.getButtonState(button) == ButtonState.DOWN) {
                if (getButtonState(button) != ButtonState.DOWN) {
                    result.add(button);
                }
            }
        }

        return result;
    }

    public EnumSet<MouseButton> getButtonsPressedSince(final MouseState previous) {
        final EnumSet<MouseButton> result = EnumSet.noneOf(MouseButton.class);
        for (final MouseButton button : MouseButton.values()) {
            if (getButtonState(button) == ButtonState.DOWN) {
                if (previous.getButtonState(button) != ButtonState.DOWN) {
                    result.add(button);
                }
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return "MouseState{" + "x=" + x + ", y=" + y + ", dx=" + dx + ", dy=" + dy + ", dwheel=" + dwheel
                + ", buttonStates=" + buttonStates.toString() + '}';
    }
}
