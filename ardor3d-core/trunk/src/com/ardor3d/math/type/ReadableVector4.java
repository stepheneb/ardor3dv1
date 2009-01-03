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

import com.ardor3d.math.Vector4;

public interface ReadableVector4 {

    public double getX();

    public double getY();

    public double getZ();

    public double getW();

    public float getXf();

    public float getYf();

    public float getZf();

    public float getWf();

    public double getValue(final int index);

    public Vector4 add(final double x, final double y, final double z, final double w, final Vector4 store);

    public Vector4 add(final ReadableVector4 source, final Vector4 store);

    public Vector4 subtract(final double x, final double y, final double z, final double w, final Vector4 store);

    public Vector4 subtract(final ReadableVector4 source, final Vector4 store);

    public Vector4 multiply(final double scalar, final Vector4 store);

    public Vector4 multiply(final ReadableVector4 scale, final Vector4 store);

    public Vector4 divide(final double scalar, final Vector4 store);

    public Vector4 divide(final ReadableVector4 scale, final Vector4 store);

    public Vector4 scaleAdd(final double scale, final ReadableVector4 add, final Vector4 store);

    public Vector4 negate(final Vector4 store);

    public Vector4 normalize(final Vector4 store);

    public Vector4 lerp(final ReadableVector4 endVec, final double scalar, final Vector4 store);

    public double length();

    public double lengthSquared();

    public double distanceSquared(final double x, final double y, final double z, final double w);

    public double distanceSquared(final ReadableVector4 destination);

    public double distance(final double x, final double y, final double z, final double w);

    public double distance(final ReadableVector4 destination);

    public double dot(final double x, final double y, final double z, final double w);

    public double dot(final ReadableVector4 vec);

    public double determinant(final double x, final double y, final double z, final double w);

    public double determinant(final ReadableVector4 vec);
}
