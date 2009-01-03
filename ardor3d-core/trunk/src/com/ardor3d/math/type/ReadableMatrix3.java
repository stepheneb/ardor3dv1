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

import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;

public interface ReadableMatrix3 {

    public double getValue(final int row, final int column);

    public float getValuef(final int row, final int column);

    public boolean isIdentity();

    public Vector3 getColumn(final int index, final Vector3 store);

    public Vector3 getRow(final int index, final Vector3 store);

    public DoubleBuffer toDoubleBuffer(final DoubleBuffer store);

    public DoubleBuffer toDoubleBuffer(final DoubleBuffer store, final boolean rowMajor);

    public FloatBuffer toFloatBuffer(final FloatBuffer store);

    public FloatBuffer toFloatBuffer(final FloatBuffer store, final boolean rowMajor);

    public double[] toArray(final double[] store);

    public double[] toArray(final double[] store, final boolean rowMajor);

    public Matrix3 multiply(final ReadableMatrix3 matrix, final Matrix3 store);

    public Vector3 applyPre(final ReadableVector3 vec, final Vector3 store);

    public Vector3 applyPost(final ReadableVector3 vec, final Vector3 store);

    public Matrix3 multiplyDiagonalPre(final ReadableVector3 vec, final Matrix3 store);

    public Matrix3 multiplyDiagonalPost(final ReadableVector3 vec, final Matrix3 store);

    public Matrix3 add(final ReadableMatrix3 matrix, final Matrix3 store);

    public Matrix3 scale(final ReadableVector3 scale, final Matrix3 store);

    public Matrix3 transpose(final Matrix3 store);

    public Matrix3 invert(final Matrix3 store);

    public Matrix3 adjugate(final Matrix3 store);

    public double determinant();
}
