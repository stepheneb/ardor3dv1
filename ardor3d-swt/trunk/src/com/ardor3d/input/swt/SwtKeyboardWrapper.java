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

import java.util.LinkedList;

import org.eclipse.swt.events.KeyListener;

import com.ardor3d.annotation.GuardedBy;
import com.ardor3d.annotation.ThreadSafe;
import com.ardor3d.framework.swt.SwtCanvas;
import com.ardor3d.input.KeyEvent;
import com.ardor3d.input.KeyState;
import com.ardor3d.input.KeyboardWrapper;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.PeekingIterator;

/**
 * Keyboard wrapper for SWT input.
 */
@ThreadSafe
public class SwtKeyboardWrapper implements KeyboardWrapper, KeyListener {
    @GuardedBy("this")
    private final LinkedList<KeyEvent> upcomingEvents;

    private final SwtCanvas canvas;

    @GuardedBy("this")
    private SwtKeyboardIterator currentIterator = null;
    @GuardedBy("this")
    private int lastKeyPressedCode = -1;

    public SwtKeyboardWrapper(final SwtCanvas canvas) {
        upcomingEvents = new LinkedList<KeyEvent>();
        this.canvas = checkNotNull(canvas, "canvas");
    }

    public void init() {
        canvas.addKeyListener(this);
    }

    public synchronized PeekingIterator<KeyEvent> getEvents() {
        if (currentIterator == null || !currentIterator.hasNext()) {
            currentIterator = new SwtKeyboardIterator();
        }

        return currentIterator;
    }

    public synchronized void keyPressed(final org.eclipse.swt.events.KeyEvent event) {
        // System.out.println("keyPressed(" + SwtKey.findByCode(event.keyCode) + ")");
        if (event.keyCode == lastKeyPressedCode) {
            // ignore if this is a repeat event
            return;
        }

        if (lastKeyPressedCode != -1) {
            // if this is a different key to the last key that was pressed, then
            // add an 'up' even for the previous one - SWT doesn't send an 'up' event for the
            // first key in the below scenario:
            // 1. key 1 down
            // 2. key 2 down
            // 3. key 1 up
            upcomingEvents.add(new KeyEvent(SwtKey.findByCode(lastKeyPressedCode), KeyState.UP));
        }

        lastKeyPressedCode = event.keyCode;
        upcomingEvents.add(new KeyEvent(SwtKey.findByCode(event.keyCode), KeyState.DOWN));
    }

    public synchronized void keyReleased(final org.eclipse.swt.events.KeyEvent event) {
        // System.out.println("keyReleased(" + SwtKey.findByCode(event.keyCode) + ")");
        upcomingEvents.add(new KeyEvent(SwtKey.findByCode(event.keyCode), KeyState.UP));
        lastKeyPressedCode = -1;
    }

    private class SwtKeyboardIterator extends AbstractIterator<KeyEvent> implements PeekingIterator<KeyEvent> {

        @Override
        protected KeyEvent computeNext() {
            synchronized (SwtKeyboardWrapper.this) {
                if (upcomingEvents.isEmpty()) {
                    return endOfData();
                }

                return upcomingEvents.poll();
            }
        }
    }
}
