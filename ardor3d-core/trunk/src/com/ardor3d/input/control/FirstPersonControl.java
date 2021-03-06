/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.input.control;

import com.ardor3d.framework.Canvas;
import com.ardor3d.input.InputState;
import com.ardor3d.input.Key;
import com.ardor3d.input.KeyboardState;
import com.ardor3d.input.MouseState;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TriggerConditions;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class FirstPersonControl {

    private final Vector3 _upAxis = new Vector3();
    private double _mouseRotateSpeed = .005;
    private double _moveSpeed = 25;
    private double _keyRotateSpeed = 2.25;
    private final Matrix3 workerMatrix = new Matrix3();
    private final Vector3 workerStoreA = new Vector3();
    private final Vector3 workerStoreB = new Vector3();

    public FirstPersonControl(final ReadOnlyVector3 upAxis) {
        _upAxis.set(upAxis);
    }

    public ReadOnlyVector3 getUpAxis() {
        return _upAxis;
    }

    public void setUpAxis(final ReadOnlyVector3 upAxis) {
        _upAxis.set(upAxis);
    }

    public double getMouseRotateSpeed() {
        return _mouseRotateSpeed;
    }

    public void setMouseRotateSpeed(final double speed) {
        _mouseRotateSpeed = speed;
    }

    public double getMoveSpeed() {
        return _moveSpeed;
    }

    public void setMoveSpeed(final double speed) {
        _moveSpeed = speed;
    }

    public double getKeyRotateSpeed() {
        return _keyRotateSpeed;
    }

    public void setKeyRotateSpeed(final double speed) {
        _keyRotateSpeed = speed;
    }

    protected void move(final Camera camera, final KeyboardState kb, final double tpf) {
        // MOVEMENT
        int moveFB = 0, strafeLR = 0;
        if (kb.isDown(Key.W)) {
            moveFB += 1;
        }
        if (kb.isDown(Key.S)) {
            moveFB -= 1;
        }
        if (kb.isDown(Key.A)) {
            strafeLR += 1;
        }
        if (kb.isDown(Key.D)) {
            strafeLR -= 1;
        }

        final Vector3 loc = workerStoreA.set(camera.getLocation());
        if (moveFB != 0) {
            loc.addLocal(workerStoreB.set(camera.getDirection()).multiplyLocal(moveFB * _moveSpeed * tpf));
        }
        if (strafeLR != 0) {
            loc.addLocal(workerStoreB.set(camera.getLeft()).multiplyLocal(strafeLR * _moveSpeed * tpf));
        }
        camera.setLocation(loc);

        // ROTATION
        int rotX = 0, rotY = 0;
        if (kb.isDown(Key.UP)) {
            rotY -= 1;
        }
        if (kb.isDown(Key.DOWN)) {
            rotY += 1;
        }
        if (kb.isDown(Key.LEFT)) {
            rotX += 1;
        }
        if (kb.isDown(Key.RIGHT)) {
            rotX -= 1;
        }
        if (rotX != 0 || rotY != 0) {
            rotate(camera, rotX * (_keyRotateSpeed / _mouseRotateSpeed) * tpf, rotY
                    * (_keyRotateSpeed / _mouseRotateSpeed) * tpf);
        }
    }

    protected void rotate(final Camera camera, final double dx, final double dy) {

        if (dx != 0) {
            workerMatrix.fromAngleNormalAxis(_mouseRotateSpeed * dx, _upAxis != null ? _upAxis : camera.getUp());
            workerMatrix.applyPost(camera.getLeft(), workerStoreA);
            camera.setLeft(workerStoreA);
            workerMatrix.applyPost(camera.getDirection(), workerStoreA);
            camera.setDirection(workerStoreA);
            workerMatrix.applyPost(camera.getUp(), workerStoreA);
            camera.setUp(workerStoreA);
        }

        if (dy != 0) {
            workerMatrix.fromAngleNormalAxis(_mouseRotateSpeed * dy, camera.getLeft());
            workerMatrix.applyPost(camera.getLeft(), workerStoreA);
            camera.setLeft(workerStoreA);
            workerMatrix.applyPost(camera.getDirection(), workerStoreA);
            camera.setDirection(workerStoreA);
            workerMatrix.applyPost(camera.getUp(), workerStoreA);
            camera.setUp(workerStoreA);
        }

        camera.normalize();
    }

    /**
     * @param layer
     * @param impl
     * @return
     */
    public static FirstPersonControl setupTriggers(final LogicalLayer layer, final ReadOnlyVector3 upAxis,
            final boolean dragOnly) {

        final FirstPersonControl control = new FirstPersonControl(upAxis);

        // Mouse look
        final Predicate<TwoInputStates> someMouseDown = Predicates.or(TriggerConditions.leftButtonDown(), Predicates
                .or(TriggerConditions.rightButtonDown(), TriggerConditions.middleButtonDown()));
        final Predicate<TwoInputStates> dragged = Predicates.and(TriggerConditions.mouseMoved(), someMouseDown);
        final TriggerAction dragAction = new TriggerAction() {

            public void perform(final Canvas source, final InputState inputState, final double tpf) {
                final MouseState mouse = inputState.getMouseState();
                if (mouse.getDx() != 0 || mouse.getDy() != 0) {
                    control.rotate(source.getCanvasRenderer().getCamera(), -mouse.getDx(), -mouse.getDy());
                }
            }
        };
        layer.registerTrigger(new InputTrigger(dragOnly ? dragged : TriggerConditions.mouseMoved(), dragAction));

        // WASD control
        final Predicate<TwoInputStates> keysHeld = new Predicate<TwoInputStates>() {
            Key[] keys = new Key[] { Key.W, Key.A, Key.S, Key.D, Key.LEFT, Key.RIGHT, Key.UP, Key.DOWN };

            public boolean apply(final TwoInputStates states) {
                for (final Key k : keys) {
                    if (states.getCurrent() != null && states.getCurrent().getKeyboardState().isDown(k)) {
                        return true;
                    }
                }
                return false;
            }
        };
        final TriggerAction moveAction = new TriggerAction() {

            public void perform(final Canvas source, final InputState inputState, final double tpf) {
                control.move(source.getCanvasRenderer().getCamera(), inputState.getKeyboardState(), tpf);
            }
        };
        layer.registerTrigger(new InputTrigger(keysHeld, moveAction));
        return control;
    }
}
