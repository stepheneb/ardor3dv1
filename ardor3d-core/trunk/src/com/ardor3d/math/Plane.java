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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.ardor3d.math.type.ReadOnlyPlane;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.util.Debug;
import com.ardor3d.util.export.Ardor3DExporter;
import com.ardor3d.util.export.Ardor3DImporter;
import com.ardor3d.util.export.InputCapsule;
import com.ardor3d.util.export.OutputCapsule;
import com.ardor3d.util.export.Savable;
import com.ardor3d.util.pool.ObjectPool;

/**
 * A representation of a mathematical plane using a normal vector and a plane constant (d) whose absolute value
 * represents the distance from the origin to the plane. It is generally calculated by taking a point (X) on the plane
 * and finding its dot-product with the plane's normal vector. iow: d = N dot X
 */
public class Plane implements Cloneable, Savable, Externalizable, ReadOnlyPlane {

    private static final long serialVersionUID = 1L;

    private static final PlanePool PLANE_POOL = new PlanePool(11);

    public static final ReadOnlyPlane XZ = new Plane(Vector3.UNIT_Y, 0);
    public static final ReadOnlyPlane XY = new Plane(Vector3.UNIT_Z, 0);
    public static final ReadOnlyPlane YZ = new Plane(Vector3.UNIT_X, 0);

    protected final Vector3 _normal = new Vector3();
    protected double _constant = 0;

    /**
     * Constructs a new plane with a normal of (0, 1, 0) and a constant value of 0.
     */
    public Plane() {
        this(Vector3.UNIT_Y, 0);
    }

    /**
     * Constructs a new plane using the supplied normal vector and plane constant
     * 
     * @param normal
     * @param constant
     */
    public Plane(final ReadOnlyVector3 normal, final double constant) {
        _normal.set(normal);
        _constant = constant;
    }

    public double getConstant() {
        return _constant;
    }

    /**
     * 
     * @return normal as a readable vector
     */
    public ReadOnlyVector3 getNormal() {
        return _normal;
    }

    /**
     * Sets the constant value of this plane to the given double value.
     * 
     * @param constant
     */
    public void setConstant(final double constant) {
        _constant = constant;
    }

    /**
     * Sets the plane normal to the values of the given vector.
     * 
     * @param normal
     * @throws NullPointerException
     *             if normal is null.
     */
    public void setNormal(final ReadOnlyVector3 normal) {
        _normal.set(normal);
    }

    /**
     * @param point
     * @return the distance from this plane to a provided point. If the point is on the negative side of the plane the
     *         distance returned is negative, otherwise it is positive. If the point is on the plane, it is zero.
     * @throws NullPointerException
     *             if point is null.
     */
    public double pseudoDistance(final ReadOnlyVector3 point) {
        return _normal.dot(point) - _constant;
    }

    /**
     * @param point
     * @return the side of this plane on which the given point lies.
     * @see Side
     * @throws NullPointerException
     *             if point is null.
     */
    public Side whichSide(final ReadOnlyVector3 point) {
        final double dis = pseudoDistance(point);
        if (dis < 0) {
            return Side.Inside;
        } else if (dis > 0) {
            return Side.Outside;
        } else {
            return Side.Neither;
        }
    }

    /**
     * Sets this plane to the plane defined by the given three points.
     * 
     * @param pointA
     * @param pointB
     * @param pointC
     * @return this plane for chaining
     * @throws NullPointerException
     *             if one or more of the points are null.
     */
    public Plane setPlanePoints(final ReadOnlyVector3 pointA, final ReadOnlyVector3 pointB, final ReadOnlyVector3 pointC) {
        _normal.set(pointB).subtractLocal(pointA);
        _normal.crossLocal(pointC.getX() - pointA.getX(), pointC.getY() - pointA.getY(), pointC.getZ() - pointA.getZ())
                .normalizeLocal();
        _constant = _normal.dot(pointA);
        return this;
    }

    /**
     * Check a plane... if it is null or its constant, or the doubles of its normal are NaN or infinite, return false.
     * Else return true.
     * 
     * @param plane
     *            the plane to check
     * @return true or false as stated above.
     */
    public static boolean isValid(final ReadOnlyPlane plane) {
        if (plane == null) {
            return false;
        }
        if (Double.isNaN(plane.getConstant()) || Double.isInfinite(plane.getConstant())) {
            return false;
        }

        return Vector3.isValid(plane.getNormal());
    }

    /**
     * @return the string representation of this plane.
     */
    @Override
    public String toString() {
        return "com.ardor3d.math.Plane [Normal: " + _normal + " - Constant: " + _constant + "]";
    }

    /**
     * @return returns a unique code for this plane object based on its values. If two planes are numerically equal,
     *         they will return the same hash code value.
     */
    @Override
    public int hashCode() {
        int result = 17;

        result += 31 * result + _normal.hashCode();

        final long c = Double.doubleToLongBits(getConstant());
        result += 31 * result + (int) (c ^ (c >>> 32));

        return result;
    }

    /**
     * @param o
     *            the object to compare for equality
     * @return true if this plane and the provided plane have the same constant and normal values.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReadOnlyPlane)) {
            return false;
        }
        final ReadOnlyPlane comp = (ReadOnlyPlane) o;
        if (Double.compare(getConstant(), comp.getConstant()) == 0 && _normal.equals(comp.getNormal())) {
            return true;
        }
        return false;
    }

    // /////////////////
    // Method for Cloneable
    // /////////////////

    @Override
    public Plane clone() {
        try {
            final Plane p = (Plane) super.clone();
            p._normal.set(_normal);
            return p;
        } catch (final CloneNotSupportedException e) {
            throw new AssertionError(); // can not happen
        }
    }

    // /////////////////
    // Methods for Savable
    // /////////////////

    public Class<? extends Plane> getClassTag() {
        return this.getClass();
    }

    public void write(final Ardor3DExporter e) throws IOException {
        final OutputCapsule capsule = e.getCapsule(this);
        capsule.write(_normal, "normal", new Vector3(Vector3.UNIT_Y));
        capsule.write(_constant, "constant", 0);
    }

    public void read(final Ardor3DImporter e) throws IOException {
        final InputCapsule capsule = e.getCapsule(this);
        _normal.set((Vector3) capsule.readSavable("normal", new Vector3(Vector3.UNIT_Y)));
        _constant = capsule.readDouble("constant", 0);
    }

    // /////////////////
    // Methods for Externalizable
    // /////////////////

    /**
     * Used with serialization. Not to be called manually.
     * 
     * @param in
     *            ObjectInput
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        setNormal((Vector3) in.readObject());
        setConstant(in.readDouble());
    }

    /**
     * Used with serialization. Not to be called manually.
     * 
     * @param out
     *            ObjectOutput
     * @throws IOException
     */
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeObject(_normal);
        out.writeDouble(getConstant());
    }

    // /////////////////
    // Methods for creating temp variables (pooling)
    // /////////////////

    /**
     * @return An instance of Plane that is intended for temporary use in calculations and so forth. Multiple calls to
     *         the method should return instances of this class that are not currently in use.
     */
    public final static Plane fetchTempInstance() {
        if (Debug.useMathPools) {
            return PLANE_POOL.fetch();
        } else {
            return new Plane();
        }
    }

    /**
     * Releases a Plane back to be used by a future call to fetchTempInstance. TAKE CARE: this Plane object should no
     * longer have other classes referencing it or "Bad Things" will happen.
     * 
     * @param plane
     *            the Plane to release.
     */
    public final static void releaseTempInstance(final Plane plane) {
        if (Debug.useMathPools) {
            PLANE_POOL.release(plane);
        }
    }

    static final class PlanePool extends ObjectPool<Plane> {
        public PlanePool(final int initialSize) {
            super(initialSize);
        }

        @Override
        protected Plane newInstance() {
            return new Plane();
        }
    }
}
