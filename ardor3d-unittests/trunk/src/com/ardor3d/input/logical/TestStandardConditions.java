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

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.EnumMap;
import java.util.EnumSet;

import org.junit.Test;

import com.ardor3d.input.ButtonState;
import com.ardor3d.input.InputState;
import com.ardor3d.input.Key;
import com.ardor3d.input.KeyboardState;
import com.ardor3d.input.MouseButton;
import com.ardor3d.input.MouseState;

public class TestStandardConditions {
    final KeyboardState ks = new KeyboardState(EnumSet.noneOf(Key.class));
    final MouseState ms = new MouseState(0, 0, 0, 0, 0, MouseButton.makeMap(ButtonState.UP, ButtonState.UP,
            ButtonState.UP));
    InputState is1, is2, is3, is4, is5;

    KeyboardState aDown = new KeyboardState(EnumSet.of(Key.A));
    KeyboardState bDown = new KeyboardState(EnumSet.of(Key.B));

    EnumMap<MouseButton, ButtonState> bothUp = MouseButton.makeMap(ButtonState.UP, ButtonState.UP, ButtonState.UP);
    EnumMap<MouseButton, ButtonState> upDown = MouseButton.makeMap(ButtonState.UP, ButtonState.DOWN, ButtonState.UP);
    EnumMap<MouseButton, ButtonState> downUp = MouseButton.makeMap(ButtonState.DOWN, ButtonState.UP, ButtonState.UP);
    EnumMap<MouseButton, ButtonState> bothDown = MouseButton
            .makeMap(ButtonState.DOWN, ButtonState.DOWN, ButtonState.UP);

    @Test
    public void testKeyHeld1() throws Exception {
        final KeyHeldCondition kh = new KeyHeldCondition(Key.A);

        is1 = new InputState(ks, ms);
        is2 = new InputState(aDown, ms);
        is3 = new InputState(bDown, ms);

        assertFalse("not down", kh.apply(new TwoInputStates(is1, is1)));
        assertTrue("down", kh.apply(new TwoInputStates(is1, is2)));
        assertFalse("not down", kh.apply(new TwoInputStates(is1, is3)));
        assertFalse("not down", kh.apply(new TwoInputStates(is2, is3)));
        assertTrue("not down", kh.apply(new TwoInputStates(is2, is2)));

        assertFalse("nulls1", kh.apply(new TwoInputStates(null, null)));
        assertFalse("nulls2", kh.apply(new TwoInputStates(is1, null)));
        assertFalse("nulls3", kh.apply(new TwoInputStates(null, is1)));
        assertTrue("nulls4", kh.apply(new TwoInputStates(null, is2)));
    }

    @Test
    public void testKeyPressed() throws Exception {
        final KeyPressedCondition kh = new KeyPressedCondition(Key.A);

        is1 = new InputState(ks, ms);
        is2 = new InputState(aDown, ms);
        is3 = new InputState(bDown, ms);

        assertFalse("not down", kh.apply(new TwoInputStates(is1, is1)));
        assertTrue("down", kh.apply(new TwoInputStates(is1, is2)));
        assertFalse("not down", kh.apply(new TwoInputStates(is1, is3)));
        assertFalse("not down", kh.apply(new TwoInputStates(is2, is3)));
        assertFalse("not down", kh.apply(new TwoInputStates(is2, is2)));

        assertFalse("nulls1", kh.apply(new TwoInputStates(null, null)));
        assertFalse("nulls2", kh.apply(new TwoInputStates(is1, null)));
        assertFalse("nulls3", kh.apply(new TwoInputStates(null, is1)));
        assertFalse("nulls4", kh.apply(new TwoInputStates(null, is2)));
    }

    @Test
    public void testKeyReleased() throws Exception {
        final KeyReleasedCondition kh = new KeyReleasedCondition(Key.A);

        is1 = new InputState(ks, ms);
        is2 = new InputState(aDown, ms);
        is3 = new InputState(bDown, ms);

        assertFalse("not down", kh.apply(new TwoInputStates(is1, is1)));
        assertFalse("not down", kh.apply(new TwoInputStates(is1, is2)));
        assertFalse("not down", kh.apply(new TwoInputStates(is1, is3)));
        assertTrue("not down", kh.apply(new TwoInputStates(is2, is3)));
        assertFalse("not down", kh.apply(new TwoInputStates(is2, is2)));

        assertFalse("nulls1", kh.apply(new TwoInputStates(null, null)));
        assertFalse("nulls2", kh.apply(new TwoInputStates(is1, null)));
        assertFalse("nulls3", kh.apply(new TwoInputStates(null, is1)));
        assertFalse("nulls4", kh.apply(new TwoInputStates(null, is2)));
    }

    @Test
    public void testMouseMove() throws Exception {
        final MouseMovedCondition mm = TriggerConditions.mouseMoved();

        final MouseState ms2 = new MouseState(1, 0, 1, 0, 0, bothUp);
        final MouseState ms3 = new MouseState(1, 0, 0, 0, 0, bothDown);
        final MouseState ms4 = new MouseState(3, 1, 2, 1, 0, bothDown);
        final MouseState ms5 = new MouseState(3, 0, 0, -1, 0, bothDown);

        is1 = new InputState(ks, ms);
        is2 = new InputState(ks, ms2);
        is3 = new InputState(ks, ms3);
        is4 = new InputState(ks, ms4);
        is5 = new InputState(ks, ms5);

        assertFalse("mm1", mm.apply(new TwoInputStates(is1, is1)));
        assertTrue("mm2", mm.apply(new TwoInputStates(is1, is2)));
        assertFalse("mm3", mm.apply(new TwoInputStates(is2, is3)));
        assertTrue("mm4", mm.apply(new TwoInputStates(is3, is4)));
        assertTrue("mm5", mm.apply(new TwoInputStates(is4, is5)));
        assertFalse("mm6", mm.apply(new TwoInputStates(is2, is2)));

        assertFalse("nulls1", mm.apply(new TwoInputStates(null, null)));
        assertFalse("nulls2", mm.apply(new TwoInputStates(is1, null)));
        assertFalse("nulls3", mm.apply(new TwoInputStates(null, is1)));
        assertTrue("nulls4", mm.apply(new TwoInputStates(null, is2)));
    }

    @Test
    public void testMouseButton1() throws Exception {
        final MouseButtonCondition mm = TriggerConditions.leftButtonDown();

        final MouseState ms2 = new MouseState(1, 0, 1, 0, 0, bothUp);
        final MouseState ms3 = new MouseState(1, 0, 0, 0, 0, bothDown);
        final MouseState ms4 = new MouseState(3, 1, 2, 1, 0, upDown);
        final MouseState ms5 = new MouseState(3, 0, 0, -1, 0, downUp);

        is1 = new InputState(ks, ms);
        is2 = new InputState(ks, ms2);
        is3 = new InputState(ks, ms3);
        is4 = new InputState(ks, ms4);
        is5 = new InputState(ks, ms5);

        assertFalse("mm1", mm.apply(new TwoInputStates(is1, is1)));
        assertFalse("mm2", mm.apply(new TwoInputStates(is1, is2)));
        assertTrue("mm3", mm.apply(new TwoInputStates(is2, is3)));
        assertFalse("mm4", mm.apply(new TwoInputStates(is3, is4)));
        assertTrue("mm5", mm.apply(new TwoInputStates(is4, is5)));

        assertFalse("nulls1", mm.apply(new TwoInputStates(null, null)));
        assertFalse("nulls2", mm.apply(new TwoInputStates(is1, null)));
        assertFalse("nulls3", mm.apply(new TwoInputStates(null, is1)));
        assertTrue("nulls4", mm.apply(new TwoInputStates(null, is3)));
    }

    @Test
    public void testMouseButton2() throws Exception {
        final MouseButtonCondition mm = TriggerConditions.rightButtonDown();

        final MouseState ms2 = new MouseState(1, 0, 1, 0, 0, bothUp);
        final MouseState ms3 = new MouseState(1, 0, 0, 0, 0, bothDown);
        final MouseState ms4 = new MouseState(3, 1, 2, 1, 0, upDown);
        final MouseState ms5 = new MouseState(3, 0, 0, -1, 0, downUp);

        is1 = new InputState(ks, ms);
        is2 = new InputState(ks, ms2);
        is3 = new InputState(ks, ms3);
        is4 = new InputState(ks, ms4);
        is5 = new InputState(ks, ms5);

        assertFalse("mm1", mm.apply(new TwoInputStates(is1, is1)));
        assertFalse("mm2", mm.apply(new TwoInputStates(is1, is2)));
        assertTrue("mm3", mm.apply(new TwoInputStates(is2, is3)));
        assertTrue("mm4", mm.apply(new TwoInputStates(is3, is4)));
        assertFalse("mm5", mm.apply(new TwoInputStates(is4, is5)));

        assertFalse("nulls1", mm.apply(new TwoInputStates(null, null)));
        assertFalse("nulls2", mm.apply(new TwoInputStates(is1, null)));
        assertFalse("nulls3", mm.apply(new TwoInputStates(null, is1)));
        assertTrue("nulls4", mm.apply(new TwoInputStates(null, is3)));
    }
}