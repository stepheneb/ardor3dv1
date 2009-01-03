/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.input.swt;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.EnumMap;
import java.util.LinkedList;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseWheelListener;

import com.ardor3d.annotation.GuardedBy;
import com.ardor3d.annotation.ThreadSafe;
import com.ardor3d.framework.swt.SwtCanvas;
import com.ardor3d.input.ButtonState;
import com.ardor3d.input.MouseButton;
import com.ardor3d.input.MouseState;
import com.ardor3d.input.MouseWrapper;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.PeekingIterator;

/**
 * A mouse wrapper for use with SWT.
 */
@ThreadSafe
public class SwtMouseWrapper implements MouseWrapper, MouseListener, MouseMoveListener, MouseWheelListener {
    @GuardedBy("this")
    private final LinkedList<MouseState> upcomingEvents = new LinkedList<MouseState>();

    private final SwtCanvas canvas;

    @GuardedBy("this")
    private SwtMouseIterator currentIterator = null;

    @GuardedBy("this")
    private MouseState lastState = null;

    // private Control canvas;

    public SwtMouseWrapper(final SwtCanvas canvas) {
        this.canvas = checkNotNull(canvas, "canvas");
    }

    public void init() {
        canvas.addMouseListener(this);
        canvas.addMouseMoveListener(this);
        canvas.addMouseWheelListener(this);
    }

    public synchronized PeekingIterator<MouseState> getEvents() {
        if (currentIterator == null || !currentIterator.hasNext()) {
            currentIterator = new SwtMouseIterator();
        }

        return currentIterator;
    }

    public synchronized void mouseDoubleClick(final MouseEvent mouseEvent) {
    // TODO: ignoring this for now, not sure if that is correct behavior
    }

    public synchronized void mouseDown(final MouseEvent e) {
        initState(e);

        final EnumMap<MouseButton, ButtonState> buttons = lastState.getButtonStates().clone();

        setStateForButton(e, buttons, ButtonState.DOWN);

        addNewState(e, 0, buttons);
    }

    public synchronized void mouseUp(final MouseEvent e) {
        initState(e);

        final EnumMap<MouseButton, ButtonState> buttons = lastState.getButtonStates().clone();

        setStateForButton(e, buttons, ButtonState.UP);

        addNewState(e, 0, buttons);
    }

    private void setStateForButton(final MouseEvent e, final EnumMap<MouseButton, ButtonState> buttons,
            final ButtonState buttonState) {
        MouseButton button;
        switch (e.button) { // ordering is different than swt
            case 1:
                button = MouseButton.LEFT;
                break;
            case 3:
                button = MouseButton.RIGHT;
                break;
            case 2:
                button = MouseButton.MIDDLE;
                break;
            default:
                throw new RuntimeException("unknown button: " + e.button);
        }
        buttons.put(button, buttonState);
    }

    public synchronized void mouseMove(final MouseEvent mouseEvent) {
        initState(mouseEvent);

        final EnumMap<MouseButton, ButtonState> buttons = lastState.getButtonStates().clone();

        addNewState(mouseEvent, 0, buttons);
    }

    public synchronized void mouseScrolled(final MouseEvent mouseEvent) {
        initState(mouseEvent);

        final EnumMap<MouseButton, ButtonState> buttons = lastState.getButtonStates().clone();

        addNewState(mouseEvent, mouseEvent.count, buttons);
    }

    private void initState(final MouseEvent mouseEvent) {
        if (lastState == null) {
            lastState = new MouseState(mouseEvent.x, canvas.getSize().y - mouseEvent.y, 0, 0, 0, null);
        }
    }

    private void addNewState(final MouseEvent mouseEvent, final int mouseDX,
            final EnumMap<MouseButton, ButtonState> buttons) {

        // changing the y value, since for SWT, y = 0 at the top of the screen
        final int fixedY = canvas.getSize().y - mouseEvent.y;

        final MouseState newState = new MouseState(mouseEvent.x, fixedY, mouseEvent.x - lastState.getX(), fixedY
                - lastState.getY(), mouseDX, buttons);

        upcomingEvents.add(newState);
        lastState = newState;
    }

    // public void listenTo(final SwtCanvas canvas) {
    // if (this.canvas != null) {
    // throw new IllegalStateException("Already listening to a canvas");
    // }
    //
    // this.canvas = canvas;
    //
    // canvas.addMouseListener(this);
    // canvas.addMouseMoveListener(this);
    // canvas.addMouseWheelListener(this);
    // }

    private class SwtMouseIterator extends AbstractIterator<MouseState> implements PeekingIterator<MouseState> {
        @Override
        protected MouseState computeNext() {
            synchronized (SwtMouseWrapper.this) {
                if (upcomingEvents.isEmpty()) {
                    return endOfData();
                }

                return upcomingEvents.poll();
            }
        }
    }
}
