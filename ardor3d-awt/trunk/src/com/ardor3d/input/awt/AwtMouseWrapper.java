/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.input.awt;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.EnumMap;
import java.util.LinkedList;

import com.ardor3d.annotation.GuardedBy;
import com.ardor3d.input.ButtonState;
import com.ardor3d.input.MouseButton;
import com.ardor3d.input.MouseState;
import com.ardor3d.input.MouseWrapper;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.PeekingIterator;
import com.google.inject.Inject;

/**
 * Mouse wrapper class for use with AWT.
 */
public class AwtMouseWrapper implements MouseWrapper, MouseListener, MouseWheelListener, MouseMotionListener {
    @GuardedBy("this")
    private final LinkedList<MouseState> upcomingEvents = new LinkedList<MouseState>();

    @GuardedBy("this")
    private AwtMouseIterator currentIterator = null;

    @GuardedBy("this")
    private MouseState lastState = null;

    private final Component component;

    @Inject
    public AwtMouseWrapper(final Component component) {
        this.component = component;
    }

    public void init() {
        component.addMouseListener(this);
        component.addMouseMotionListener(this);
    }

    // public void listenTo(final Component component) {
    // this.component = component;
    //
    // }

    public synchronized PeekingIterator<MouseState> getEvents() {
        if (currentIterator == null || !currentIterator.hasNext()) {
            currentIterator = new AwtMouseIterator();
        }

        return currentIterator;
    }

    public synchronized void mouseClicked(final MouseEvent e) {
    // ignore this
    }

    public synchronized void mousePressed(final MouseEvent e) {
        initState(e);

        final EnumMap<MouseButton, ButtonState> buttons = lastState.getButtonStates().clone();

        setStateForButton(e, buttons, ButtonState.DOWN);

        addNewState(e, buttons);
    }

    public synchronized void mouseReleased(final MouseEvent e) {
        initState(e);

        final EnumMap<MouseButton, ButtonState> buttons = lastState.getButtonStates().clone();

        setStateForButton(e, buttons, ButtonState.UP);

        addNewState(e, buttons);
    }

    public synchronized void mouseEntered(final MouseEvent e) {
    // ignore this
    }

    public synchronized void mouseExited(final MouseEvent e) {
    // ignore this
    }

    public synchronized void mouseDragged(final MouseEvent e) {
        mouseMoved(e);
    }

    public synchronized void mouseMoved(final MouseEvent e) {
        initState(e);

        addNewState(e, lastState.getButtonStates());
    }

    public void mouseWheelMoved(final MouseWheelEvent e) {
        initState(e);

        addNewState(e, lastState.getButtonStates());
    }

    private void initState(final MouseEvent mouseEvent) {
        if (component == null) {
            throw new IllegalStateException(
                    "You need to add a component to listen to using the listenTo(Control) method");
        }

        if (lastState == null) {
            lastState = new MouseState(mouseEvent.getX(), component.getHeight() - mouseEvent.getY(), 0, 0, 0, null);
        }
    }

    private void addNewState(final MouseEvent mouseEvent, final EnumMap<MouseButton, ButtonState> enumMap) {
        // changing the y value, since for AWT, y = 0 at the top of the screen
        final int fixedY = component.getHeight() - mouseEvent.getY();

        final MouseState newState = new MouseState(mouseEvent.getX(), fixedY, mouseEvent.getX() - lastState.getX(),
                fixedY - lastState.getY(), (mouseEvent instanceof MouseWheelEvent ? ((MouseWheelEvent) mouseEvent)
                        .getWheelRotation() : 0), enumMap);

        upcomingEvents.add(newState);
        lastState = newState;
    }

    private void setStateForButton(final MouseEvent e, final EnumMap<MouseButton, ButtonState> buttons,
            final ButtonState buttonState) {
        MouseButton button;
        switch (e.getButton()) {
            case MouseEvent.BUTTON1:
                button = MouseButton.LEFT;
                break;
            case MouseEvent.BUTTON2:
                button = MouseButton.RIGHT;
                break;
            case MouseEvent.BUTTON3:
                button = MouseButton.MIDDLE;
                break;
            default:
                throw new RuntimeException("unknown button: " + e.getButton());
        }
        buttons.put(button, buttonState);
    }

    private class AwtMouseIterator extends AbstractIterator<MouseState> implements PeekingIterator<MouseState> {
        @Override
        protected MouseState computeNext() {
            synchronized (AwtMouseWrapper.this) {
                if (upcomingEvents.isEmpty()) {
                    return endOfData();
                }

                return upcomingEvents.poll();
            }

        }
    }
}
