/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.light;

import java.io.IOException;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.util.export.Ardor3DExporter;
import com.ardor3d.util.export.Ardor3DImporter;
import com.ardor3d.util.export.InputCapsule;
import com.ardor3d.util.export.OutputCapsule;

/**
 * <code>PointLight</code> defines a light that has a location in space and emits light in all directions evenly. This
 * would be something similar to a light bulb. Typically this light's values are attenuated based on the distance of the
 * point light and the object it illuminates.
 */
public class PointLight extends Light {
    private static final long serialVersionUID = 1L;
    // Position of the light.
    private Vector3 location;

    /**
     * Constructor instantiates a new <code>PointLight</code> object. The initial position of the light is (0,0,0) and
     * it's colors are white.
     * 
     */
    public PointLight() {
        super();
        location = new Vector3();
    }

    /**
     * <code>getLocation</code> returns the position of this light.
     * 
     * @return the position of the light.
     */
    public ReadOnlyVector3 getLocation() {
        return location;
    }

    /**
     * <code>setLocation</code> sets the position of the light.
     * 
     * @param location
     *            the position of the light.
     */
    public void setLocation(final Vector3 location) {
        this.location.set(location);
    }

    /**
     * <code>setLocation</code> sets the position of the light.
     * 
     * @param x
     *            the x position of the light.
     * @param y
     *            the y position of the light.
     * @param z
     *            the z position of the light.
     */
    public void setLocation(final double x, final double y, final double z) {
        location.set(x, y, z);
    }

    /**
     * <code>getType</code> returns the type of this light (Type.Point).
     * 
     * @see com.ardor3d.light.Light#getType()
     */
    @Override
    public Type getType() {
        return Type.Point;
    }

    @Override
    public void write(final Ardor3DExporter e) throws IOException {
        super.write(e);
        final OutputCapsule capsule = e.getCapsule(this);
        capsule.write(location, "location", new Vector3(Vector3.ZERO));

    }

    @Override
    public void read(final Ardor3DImporter e) throws IOException {
        super.read(e);
        final InputCapsule capsule = e.getCapsule(this);
        location = (Vector3) capsule.readSavable("location", new Vector3(Vector3.ZERO));

    }

}
