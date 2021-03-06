/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.math;

import java.util.Random;

import com.ardor3d.math.type.ReadOnlyVector3;

public class MathUtils {

    /** A "close to zero" double epsilon value for use */
    public static final double EPSILON = 2.220446049250313E-16d;

    /** A "close to zero" double epsilon value for use */
    public static final double ZERO_TOLERANCE = 0.0001;

    public static final double ONE_THIRD = 1.0 / 3.0;

    /** The value PI as a double. (180 degrees) */
    public static final double PI = Math.PI;

    /** The value 2PI as a double. (360 degrees) */
    public static final double TWO_PI = 2.0 * PI;

    /** The value PI/2 as a double. (90 degrees) */
    public static final double HALF_PI = 0.5 * PI;

    /** The value PI/4 as a double. (45 degrees) */
    public static final double QUARTER_PI = 0.25 * PI;

    /** The value 1/PI as a double. */
    public static final double INV_PI = 1.0 / PI;

    /** The value 1/(2PI) as a double. */
    public static final double INV_TWO_PI = 1.0 / TWO_PI;

    /** A value to multiply a degree value by, to convert it to radians. */
    public static final double DEG_TO_RAD = PI / 180.0;

    /** A value to multiply a radian value by, to convert it to degrees. */
    public static final double RAD_TO_DEG = 180.0 / PI;

    /** A precreated random object for random numbers. */
    public static final Random rand = new Random(System.currentTimeMillis());

    /**
     * Fast Trig functions for x86. This forces the trig functiosn to stay within the safe area on the x86 processor
     * (-45 degrees to +45 degrees) The results may be very slightly off from what the Math and StrictMath trig
     * functions give due to rounding in the angle reduction but it will be very very close.
     * 
     * note: code from wiki posting on java.net by jeffpk
     */
    private static double reduceSinAngle(double radians) {
        radians %= TWO_PI; // put us in -2PI to +2PI space
        if (Math.abs(radians) > PI) { // put us in -PI to +PI space
            radians = radians - (TWO_PI);
        }
        if (Math.abs(radians) > HALF_PI) {// put us in -PI/2 to +PI/2 space
            radians = PI - radians;
        }

        return radians;
    }

    /**
     * Returns sine of a value.
     * 
     * note: code from wiki posting on java.net by jeffpk
     * 
     * @param dValue
     *            The value to sine, in radians.
     * @return The sine of dValue.
     * @see java.lang.Math#sin(double)
     */
    public static double sin(double dValue) {
        dValue = reduceSinAngle(dValue); // limits angle to between -PI/2 and +PI/2
        if (Math.abs(dValue) <= QUARTER_PI) {
            return Math.sin(dValue);
        }

        return Math.cos(HALF_PI - dValue);
    }

    /**
     * Returns cos of a value.
     * 
     * @param dValue
     *            The value to cosine, in radians.
     * @return The cosine of dValue.
     * @see java.lang.Math#cos(double)
     */
    public static double cos(final double dValue) {
        return sin(dValue + HALF_PI);
    }

    /**
     * Converts a point from Spherical coordinates to Cartesian (using positive Y as up) and stores the results in the
     * store var.
     */
    public static Vector3 sphericalToCartesian(final ReadOnlyVector3 sphereCoords, final Vector3 store) {
        final double a = sphereCoords.getX() * cos(sphereCoords.getZ());
        final double x = a * cos(sphereCoords.getY());
        final double y = sphereCoords.getX() * sin(sphereCoords.getZ());
        final double z = a * sin(sphereCoords.getY());

        return store.set(x, y, z);
    }

    /**
     * Converts a point from Cartesian coordinates (using positive Y as up) to Spherical and stores the results in the
     * store var. (Radius, Azimuth, Polar)
     */
    public static Vector3 cartesianToSpherical(final ReadOnlyVector3 cartCoords, final Vector3 store) {
        final double cartX = Double.compare(cartCoords.getX(), 0.0) == 0 ? EPSILON : cartCoords.getX();
        final double cartY = cartCoords.getY();
        final double cartZ = cartCoords.getZ();

        final double x = Math.sqrt((cartX * cartX) + (cartY * cartY) + (cartZ * cartZ));
        final double y = Math.atan(cartZ / cartX) + (Double.compare(cartX, 0.0) < 0 ? PI : 0);
        final double z = Math.asin(cartY / x);
        return store.set(x, y, z);
    }

    /**
     * Converts a point from Spherical coordinates to Cartesian (using positive Z as up) and stores the results in the
     * store var.
     */
    public static Vector3 sphericalToCartesianZ(final ReadOnlyVector3 sphereCoords, final Vector3 store) {
        final double a = sphereCoords.getX() * cos(sphereCoords.getZ());
        final double x = a * cos(sphereCoords.getY());
        final double y = a * sin(sphereCoords.getY());
        final double z = sphereCoords.getX() * sin(sphereCoords.getZ());

        return store.set(x, y, z);
    }

    /**
     * Converts a point from Cartesian coordinates (using positive Z as up) to Spherical and stores the results in the
     * store var. (Radius, Azimuth, Polar)
     */
    public static Vector3 cartesianZToSpherical(final ReadOnlyVector3 cartCoords, final Vector3 store) {
        final double cartX = Double.compare(cartCoords.getX(), 0.0) == 0 ? EPSILON : cartCoords.getX();
        final double cartY = cartCoords.getY();
        final double cartZ = cartCoords.getZ();

        final double x = Math.sqrt((cartX * cartX) + (cartY * cartY) + (cartZ * cartZ));
        final double y = Math.asin(cartY / x);
        final double z = Math.atan(cartZ / cartX) + (Double.compare(cartX, 0.0) < 0 ? PI : 0);
        return store.set(x, y, z);
    }

    /**
     * Returns true if the number is a power of 2 (2,4,8,16...)
     * 
     * A good implementation found on the Java boards. note: a number is a power of two if and only if it is the
     * smallest number with that number of significant bits. Therefore, if you subtract 1, you know that the new number
     * will have fewer bits, so ANDing the original number with anything less than it will give 0.
     * 
     * @param number
     *            The number to test.
     * @return True if it is a power of two.
     */
    public static boolean isPowerOfTwo(final int number) {
        return (number > 0) && (number & (number - 1)) == 0;
    }

    /**
     * @param number
     * @return the closest power of two to the given number.
     */
    public static int nearestPowerOfTwo(final int number) {
        return (int) Math.pow(2, Math.ceil(Math.log(number) / Math.log(2)));
    }

    /**
     * @param value
     * @param base
     * @return the logarithm of value with given base, calculated as log(value)/log(base) such that pow(base,
     *         return)==value
     */
    public static double log(final double value, final double base) {
        return Math.log(value) / Math.log(base);
    }

    /**
     * Sets the seed to use for "random" operations. The default is the current system milliseconds.
     * 
     * @param seed
     */
    public static void setRandomSeed(final long seed) {
        rand.setSeed(seed);
    }

    /**
     * Returns a random double between 0 and 1.
     * 
     * @return A random double between <tt>0.0</tt> (inclusive) to <tt>1.0</tt> (exclusive).
     */
    public static double nextRandomDouble() {
        return rand.nextDouble();
    }

    /**
     * Returns a random float between 0 and 1.
     * 
     * @return A random float between <tt>0.0f</tt> (inclusive) to <tt>1.0f</tt> (exclusive).
     */
    public static float nextRandomFloat() {
        return rand.nextFloat();
    }

    /**
     * @return A random int between Integer.MIN_VALUE and Integer.MAX_VALUE.
     */
    public static int nextRandomInt() {
        return rand.nextInt();
    }

    /**
     * Returns a random int between min and max.
     * 
     * @return A random int between <tt>min</tt> (inclusive) to <tt>max</tt> (inclusive).
     */
    public static int nextRandomInt(final int min, final int max) {
        return (int) (nextRandomFloat() * (max - min + 1)) + min;
    }

    /**
     * 
     * @param percent
     * @param startValue
     * @param endValue
     * @return
     */
    public static float lerp(final float percent, final float startValue, final float endValue) {
        if (startValue == endValue) {
            return startValue;
        }
        return ((1 - percent) * startValue) + (percent * endValue);
    }

    /**
     * 
     * @param percent
     * @param startValue
     * @param endValue
     * @return
     */
    public static double lerp(final double percent, final double startValue, final double endValue) {
        if (startValue == endValue) {
            return startValue;
        }
        return ((1 - percent) * startValue) + (percent * endValue);
    }

    /**
     * 
     * @param left
     * @param right
     * @param bottom
     * @param top
     * @param nearZ
     * @param farZ
     * @param store
     */
    public static void matrixFrustum(final double left, final double right, final double bottom, final double top,
            final double nearZ, final double farZ, final Matrix4 store) {
        final double x = (2.0 * nearZ) / (right - left);
        final double y = (2.0 * nearZ) / (top - bottom);
        final double a = (right + left) / (right - left);
        final double b = (top + bottom) / (top - bottom);
        final double c = -(farZ + nearZ) / (farZ - nearZ);
        final double d = -(2.0 * farZ * nearZ) / (farZ - nearZ);

        store.set(x, 0.0, 0.0, 0.0, 0.0, y, 0.0, 0.0, a, b, c, -1.0, 0.0, 0.0, d, 0.0);
    }

    /**
     * 
     * @param left
     * @param right
     * @param bottom
     * @param top
     * @param nearZ
     * @param farZ
     * @param store
     */
    public static void matrixOrtho(final double left, final double right, final double bottom, final double top,
            final double nearZ, final double farZ, final Matrix4 store) {
        store.set(2.0 / (right - left), 0.0, 0.0, 0.0, 0.0, 2.0 / (top - bottom), 0.0, 0.0, 0.0, 0.0, -2.0
                / (farZ - nearZ), 0.0, -(right + left) / (right - left), -(top + bottom) / (top - bottom),
                -(farZ + nearZ) / (farZ - nearZ), 1.0);
    }

    /**
     * 
     * @param fovY
     * @param aspect
     * @param zNear
     * @param zFar
     * @param store
     */
    public static void matrixPerspective(final double fovY, final double aspect, final double zNear, final double zFar,
            final Matrix4 store) {
        final double height = zNear * Math.tan(fovY * 0.5 * DEG_TO_RAD);
        final double width = height * aspect;

        matrixFrustum(-width, width, -height, height, zNear, zFar, store);
    }

    /**
     * 
     * @param position
     * @param target
     * @param up
     * @param store
     */
    public static void matrixLookAt(final ReadOnlyVector3 position, final ReadOnlyVector3 target,
            final ReadOnlyVector3 worldUp, final Matrix4 store) {
        final Vector3 direction = Vector3.fetchTempInstance();
        final Vector3 side = Vector3.fetchTempInstance();
        final Vector3 up = Vector3.fetchTempInstance();

        direction.set(target).subtractLocal(position).normalizeLocal();
        direction.cross(worldUp, side).normalizeLocal();
        side.cross(direction, up);

        store.set(side.getX(), up.getX(), -direction.getX(), 0.0, side.getY(), up.getY(), -direction.getY(), 0.0, side
                .getZ(), up.getZ(), -direction.getZ(), 0.0, side.getX() * -position.getX() + side.getY()
                * -position.getY() + side.getZ() * -position.getZ(), up.getX() * -position.getX() + up.getY()
                * -position.getY() + up.getZ() * -position.getZ(), -direction.getX() * -position.getX()
                + -direction.getY() * -position.getY() + -direction.getZ() * -position.getZ(), 1.0);
    }

    /**
     * 
     * @param position
     * @param target
     * @param up
     * @param store
     */
    public static void matrixLookAt(final ReadOnlyVector3 position, final ReadOnlyVector3 target,
            final ReadOnlyVector3 worldUp, final Matrix3 store) {
        final Vector3 direction = Vector3.fetchTempInstance();
        final Vector3 side = Vector3.fetchTempInstance();
        final Vector3 up = Vector3.fetchTempInstance();

        direction.set(target).subtractLocal(position).normalizeLocal();
        direction.cross(worldUp, side).normalizeLocal();
        side.cross(direction, up);

        store.set(side.getX(), up.getX(), -direction.getX(), side.getY(), up.getY(), -direction.getY(), side.getZ(), up
                .getZ(), -direction.getZ());
    }
}
