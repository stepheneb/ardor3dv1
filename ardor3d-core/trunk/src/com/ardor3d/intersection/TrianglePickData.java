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
import java.util.logging.Logger;

import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyTransform;
import com.ardor3d.scenegraph.Mesh;

/**
 * Pick data for triangle accuracy picking including sort by distance to intersection point.
 */
public class TrianglePickData extends PickData {

    private static final Logger logger = Logger.getLogger(TrianglePickData.class.getName());

    private final Vector3[] worldTriangle = new Vector3[] { new Vector3(), new Vector3(), new Vector3() };
    private final Vector3[] vertices = new Vector3[] { new Vector3(), new Vector3(), new Vector3() };
    private double[] distances = null;

    public TrianglePickData(final Ray3 ray, final Mesh targetMesh, final List<Integer> targetTris,
            final boolean checkDistance) {
        super(ray, targetMesh, targetTris, false);
    }

    @Override
    protected double calculateDistance() {
        final List<Integer> tris = getTargetTris();
        if (tris.isEmpty()) {
            return Double.POSITIVE_INFINITY;
        }

        final Mesh mesh = getTargetMesh();

        double distanceSq = Double.POSITIVE_INFINITY;
        distances = new double[tris.size()];
        for (int i = 0; i < tris.size(); i++) {
            final int triIndex = tris.get(i);
            PickingUtil.getTriangle(mesh, triIndex, vertices);
            final double triDistanceSq = getDistanceSquaredToTriangle(vertices, mesh.getWorldTransform());
            distances[i] = triDistanceSq;
            if (triDistanceSq > 0 && triDistanceSq < distanceSq) {
                distanceSq = triDistanceSq;
            }
        }

        // FIXME: optimize! ugly bubble sort for now
        boolean sorted = false;
        while (!sorted) {
            sorted = true;
            for (int sort = 0; sort < distances.length - 1; sort++) {
                if (distances[sort] > distances[sort + 1]) {
                    // swap
                    sorted = false;
                    final double temp = distances[sort + 1];
                    distances[sort + 1] = distances[sort];
                    distances[sort] = temp;

                    // swap tris too
                    final int temp2 = tris.get(sort + 1);
                    tris.set(sort + 1, tris.get(sort));
                    tris.set(sort, temp2);
                }
            }
        }

        if (Double.isInfinite(distanceSq)) {
            return distanceSq;
        } else {
            return Math.sqrt(distanceSq);
        }
    }

    public double[] getDistances() {
        return distances;
    }

    private double getDistanceSquaredToTriangle(final Vector3[] triangle, final ReadOnlyTransform worldTransform) {
        // Transform triangle to world space
        for (int i = 0; i < 3; i++) {
            worldTransform.applyForward(triangle[i], worldTriangle[i]);
        }
        // Intersection test
        final Ray3 ray = getRay();
        final Vector3 intersect = Vector3.fetchTempInstance();
        if (ray.intersects(worldTriangle[0], worldTriangle[1], worldTriangle[2], intersect, true)) {
            return ray.getOrigin().distanceSquared(intersect);
        }
        Vector3.releaseTempInstance(intersect);

        // Should not happen
        logger.warning("Couldn't detect nearest triangle intersection!");
        return Double.POSITIVE_INFINITY;
    }
}
