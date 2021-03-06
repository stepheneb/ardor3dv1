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

import com.ardor3d.scenegraph.Mesh;

/**
 * CollisionData contains information about a collision between two Mesh objects. The mesh that was hit by the relevant
 * Mesh (the one making the collision check) is referenced as well as an ArrayList for the triangles that collided.
 */
public class CollisionData {

    private Mesh targetMesh;

    private Mesh sourceMesh;

    private List<Integer> sourceTris;

    private List<Integer> targetTris;

    /**
     * instantiates a new CollisionData object.
     * 
     * @param sourceMesh
     *            the relevant Geometry
     * @param targetMesh
     *            the mesh the relevant Mesh collided with.
     */
    public CollisionData(final Mesh sourceMesh, final Mesh targetMesh) {
        this(sourceMesh, targetMesh, null, null);
    }

    /**
     * instantiates a new CollisionData object.
     * 
     * @param sourceMesh
     *            the relevant Geometry
     * @param targetMesh
     *            the mesh the relevant Mesh collided with.
     * @param sourceTris
     *            the triangles of the relevant Mesh that made contact.
     * @param targetTris
     *            the triangles of the second mesh that made contact.
     */
    public CollisionData(final Mesh sourceMesh, final Mesh targetMesh, final List<Integer> sourceTris,
            final List<Integer> targetTris) {
        this.targetMesh = targetMesh;
        this.sourceMesh = sourceMesh;
        this.targetTris = targetTris;
        this.sourceTris = sourceTris;
    }

    /**
     * @return Returns the source mesh.
     */
    public Mesh getSourceMesh() {
        return sourceMesh;
    }

    public Mesh getTargetMesh() {
        return targetMesh;
    }

    /**
     * @param mesh
     *            The mesh to set.
     */
    public void setSourceMesh(final Mesh mesh) {
        sourceMesh = mesh;
    }

    /**
     * <code>setTargetMesh</code> sets the mesh that is hit by the source mesh.
     * 
     * @param mesh
     *            the mesh that was hit by the source mesh.
     */
    public void setTargetMesh(final Mesh mesh) {
        targetMesh = mesh;
    }

    /**
     * @return Returns the source.
     */
    public List<Integer> getSourceTris() {
        return sourceTris;
    }

    /**
     * @param source
     *            The source to set.
     */
    public void setSourceTris(final List<Integer> source) {
        sourceTris = source;
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
}