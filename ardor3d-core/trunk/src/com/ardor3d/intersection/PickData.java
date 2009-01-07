/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.intersection;

import java.util.List;

import com.ardor3d.math.Ray3;
import com.ardor3d.scenegraph.Mesh;

/**
 * 
 * PickData contains information about a picking operation (or Ray/Volume intersection). This data contains the mesh the
 * ray hit, the triangles it hit, and the ray itself.
 */
public class PickData {

    private Ray3 ray;

    private Mesh targetMesh;

    private List<Integer> targetTris;

    protected double distance;

    public PickData(final Ray3 ray, final Mesh targetMesh, final boolean checkDistance) {
        this(ray, targetMesh, null, checkDistance);
    }

    /**
     * instantiates a new PickData object.
     */
    public PickData(final Ray3 ray, final Mesh targetMesh, final List<Integer> targetTris, final boolean checkDistance) {
        this.ray = ray;
        this.targetMesh = targetMesh;
        this.targetTris = targetTris;

        if (checkDistance) {
            distance = calculateDistance();
        }
    }

    /**
     * 
     * <code>getTargetMesh</code> returns the geometry that was hit by the ray.
     * 
     * @return the geometry hit by the ray.
     */
    public Mesh getTargetMesh() {
        return targetMesh;
    }

    /**
     * 
     * <code>setTargetMesh</code> sets the geometry hit by the ray.
     * 
     * @param mesh
     *            the geometry hit by the ray.
     */
    public void setTargetMesh(final Mesh mesh) {
        targetMesh = mesh;
    }

    /**
     * @return Returns the target.
     */
    public List<Integer> getTargetTris() {
        return targetTris;
    }

    /**
     * @param target
     *            The target to set.
     */
    public void setTargetTris(final List<Integer> target) {
        targetTris = target;
    }

    /**
     * @return Returns the ray.
     */
    public Ray3 getRay() {
        return ray;
    }

    /**
     * @param ray
     *            The ray to set.
     */
    public void setRay(final Ray3 ray) {
        this.ray = ray;
    }

    public double getDistance() {
        return distance;
    }

    /**
     * For bounds picking this method returns the distance of the ray origin to the bound. For triangle picking the it
     * should return the distance to the closest hit triangle.
     * 
     * @return distance to the target
     */
    protected double calculateDistance() {
        final IntersectionRecord record = targetMesh.getWorldBound().intersectsWhere(ray);
        return record.getClosestDistance();
    }
}