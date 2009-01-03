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

import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Matrix4;
import com.ardor3d.math.Quaternion;
import com.ardor3d.math.Vector3;

public interface ReadableQuaternion {

    public double getX();

    public double getY();

    public double getZ();

    public double getW();

    public float getXf();

    public float getYf();

    public float getZf();

    public float getWf();

    public double[] toArray(final double[] store);

    public double[] toAngles(final double[] store);

    public Matrix3 toRotationMatrix(final Matrix3 store);

    public Matrix4 toRotationMatrix(final Matrix4 store);

    public Vector3 getRotationColumn(final int index, final Vector3 store);

    public double toAngleAxis(final Vector3 axisStore);

    public Quaternion normalize(final Quaternion store);

    public Quaternion invert(final Quaternion store);

    public Quaternion add(final ReadableQuaternion quat, final Quaternion store);

    public Quaternion subtract(final ReadableQuaternion quat, final Quaternion store);

    public Quaternion multiply(final double scalar, final Quaternion store);

    public Quaternion multiply(final ReadableQuaternion quat, Quaternion store);

    public Vector3 apply(final ReadableVector3 vec, Vector3 store);

    public void toAxes(final Vector3 axes[]);

    public Quaternion slerp(final ReadableQuaternion endQuat, final double changeAmnt, final Quaternion store);

    public double magnitudeSquared();

    public double magnitude();

    public double dot(final double x, final double y, final double z, final double w);

    public double dot(final ReadableQuaternion quat);

    public boolean isIdentity();
}
