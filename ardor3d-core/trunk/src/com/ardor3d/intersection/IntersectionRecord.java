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

import com.ardor3d.math.Vector3;
import com.ardor3d.util.Ardor3dException;

/**
 * that were intersected, and the distances between these points. Therefore, a 1 to 1 ratio between the distance array
 * and the point array is enforced.
 * 
 */
public class IntersectionRecord {

    private double[] distances;
    private Vector3[] points;

    /**
     * Instantiates a new IntersectionRecord with no distances or points assigned.
     * 
     */
    public IntersectionRecord() {}

    /**
     * Instantiates a new IntersectionRecord defining the distances and points. If the size of the distance and point
     * arrays do not match, an exception is thrown.
     * 
     * @param distances
     *            the distances of this intersection.
     * @param points
     *            the points of this intersection.
     */
    public IntersectionRecord(final double[] distances, final Vector3[] points) {
        if (distances.length != points.length) {
            throw new Ardor3dException("The distances and points variables must have an equal number of elements.");
        }
        this.distances = distances;
        this.points = points;
    }

    /**
     * Returns the number of intersections that occured.
     * 
     * @return the number of intersections that occured.
     */
    public int getNumberOfChildren() {
        if (points == null) {
            return 0;
        }
        return points.length;
    }

    /**
     * Returns an intersection point at a provided index.
     * 
     * @param index
     *            the index of the point to obtain.
     * @return the point at the index of the array.
     */
    public Vector3 getIntersectionPoint(final int index) {
        return points[index];
    }

    /**
     * Returns an intersection distance at a provided index.
     * 
     * @param index
     *            the index of the distance to obtain.
     * @return the distance at the index of the array.
     */
    public double getIntersectionDistance(final int index) {
        return distances[index];
    }

    /**
     * Returns the smallest distance in the distance array.
     * 
     * @return the smallest distance in the distance array.
     */
    public double getClosestDistance() {
        double min = Double.MAX_VALUE;
        if (distances != null) {
            for (final double val : distances) {
                if (val < min) {
                    min = val;
                }
            }
        }
        return min;
    }

    /**
     * Returns the point that has the smallest associated distance value.
     * 
     * @return the point that has the smallest associated distance value.
     */
    public int getClosestPoint() {
        double min = Double.MAX_VALUE;
        int point = 0;
        if (distances != null) {
            for (int i = distances.length; --i >= 0;) {
                final double val = distances[i];
                if (val < min) {
                    min = val;
                    point = i;
                }
            }
        }
        return point;
    }

    /**
     * Returns the point that has the largest associated distance value.
     * 
     * @return the point that has the largest associated distance value.
     */
    public int getFarthestPoint() {
        double max = Double.MIN_VALUE;
        int point = 0;
        if (distances != null) {
            for (int i = distances.length; --i >= 0;) {
                final double val = distances[i];
                if (val > max) {
                    max = val;
                    point = i;
                }
            }
        }
        return point;
    }

}
