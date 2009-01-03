/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.math.type;

import com.ardor3d.math.Matrix4;
import com.ardor3d.math.Transform;
import com.ardor3d.math.Vector3;

public interface ReadableTransform {
    public ReadableMatrix3 getMatrix();

    public ReadableVector3 getTranslation();

    public ReadableVector3 getScale();

    public boolean isIdentity();

    public boolean isRotationMatrix();

    public boolean isUniformScale();

    public Vector3 applyForward(final Vector3 point);

    public Vector3 applyForward(final ReadableVector3 point, final Vector3 store);

    public Vector3 applyInverse(final Vector3 point);

    public Vector3 applyInverse(final ReadableVector3 point, final Vector3 store);

    public Vector3 applyForwardVector(final Vector3 vector);

    public Vector3 applyForwardVector(final ReadableVector3 vector, final Vector3 store);

    public Vector3 applyInverseVector(final Vector3 vector);

    public Vector3 applyInverseVector(final ReadableVector3 vector, final Vector3 store);

    public Transform multiply(final ReadableTransform transformBy, final Transform store);

    public Transform invert(final Transform store);

    public Matrix4 getHomogeneousMatrix(final Matrix4 store);

}
