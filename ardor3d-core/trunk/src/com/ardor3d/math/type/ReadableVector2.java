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

import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;

public interface ReadableVector2 {

    public double getX();

    public double getY();

    public float getXf();

    public float getYf();

    public double getValue(final int index);

    public double[] toArray(double[] store);

    public Vector2 add(final double x, final double y, final Vector2 store);

    public Vector2 add(final ReadableVector2 source, final Vector2 store);

    public Vector2 subtract(final double x, final double y, final Vector2 store);

    public Vector2 subtract(final ReadableVector2 source, final Vector2 store);

    public Vector2 multiply(final double scalar, final Vector2 store);

    public Vector2 multiply(final ReadableVector2 scale, final Vector2 store);

    public Vector2 divide(final double scalar, final Vector2 store);

    public Vector2 divide(final ReadableVector2 scale, final Vector2 store);

    public Vector2 scaleAdd(final double scale, final ReadableVector2 add, final Vector2 store);

    public Vector2 negate(final Vector2 store);

    public Vector2 normalize(final Vector2 store);

    public Vector2 rotateAroundOrigin(double angle, final boolean clockwise, final Vector2 store);

    public Vector2 lerp(final ReadableVector2 endVec, final double scalar, final Vector2 store);

    public double length();

    public double lengthSquared();

    public double distanceSquared(final double x, final double y);

    public double distanceSquared(final ReadableVector2 destination);

    public double distance(final double x, final double y);

    public double distance(final ReadableVector2 destination);

    public double dot(final double x, final double y);

    public double dot(final ReadableVector2 vec);

    public Vector3 cross(final double x, final double y, final Vector3 store);

    public Vector3 cross(final ReadableVector2 vec, final Vector3 store);

    public double determinant(final double x, final double y);

    public double determinant(final ReadableVector2 vec);

    public double getPolarAngle();

    public double angleBetween(final ReadableVector2 otherVector);

    public double smallestAngleBetween(final ReadableVector2 otherVector);

}
