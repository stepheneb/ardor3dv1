/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.extension.shape;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.TexCoords;
import com.ardor3d.util.geom.BufferUtils;

/**
 * GeoSphere - generate a polygon mesh approximating a sphere by recursive subdivision. First approximation is an
 * octahedron; each level of refinement increases the number of polygons by a factor of 4.
 * <p>
 * todo: texture coordinates could be nicer
 * </p>
 * <p/>
 * Shared vertices are not retained, so numerical errors may produce cracks between polygons at high subdivision levels.
 * <p/>
 * Initial idea and text from C-Sourcecode by Jon Leech 3/24/89
 */

public class GeoSphere extends Mesh {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private int maxlevels;
    private boolean useIkosa = true;

    /**
     * @param name
     *            name of the spatial
     * @param ikosa
     *            true to start with an 20 triangles, false to start with 8 triangles
     * @param maxlevels
     *            an integer >= 1 setting the recursion level
     */
    public GeoSphere(final String name, final boolean ikosa, final int maxlevels) {
        super(name);
        this.maxlevels = maxlevels;
        useIkosa = ikosa;
        setGeometry();
    }

    /**
     * Default ctor for restoring.
     */
    public GeoSphere() {}

    /**
     * TODO: radius is always 1
     * 
     * @return 1
     */
    public float getRadius() {
        return 1;
    }

    static class Triangle {
        int[] pt = new int[3]; /* Vertices of triangle */

        public Triangle() {}

        public Triangle(final int pt0, final int pt1, final int pt2) {
            pt[0] = pt0;
            pt[1] = pt1;
            pt[2] = pt2;
        }
    }

    private void setGeometry() {
        final boolean useIkosa = this.useIkosa;
        final int initialTriangleCount = useIkosa ? 20 : 8;
        final int initialVertexCount = useIkosa ? 12 : 6;
        // number of triangles = initialTriangleCount * 4^(maxlevels-1)
        final int tris = initialTriangleCount << ((maxlevels - 1) * 2);

        // number of vertBuf = (initialVertexCount + initialTriangleCount*4 +
        // initialTriangleCount*4*4 + ...)
        // = initialTriangleCount*(((4^maxlevels)-1)/(4-1)-1) +
        // initialVertexCount
        final int verts = initialTriangleCount * (((1 << (maxlevels * 2)) - 1) / (4 - 1) - 1) + initialVertexCount;

        FloatBuffer vertBuf = _meshData.getVertexBuffer();
        _meshData.setVertexBuffer(vertBuf = BufferUtils.createVector3Buffer(vertBuf, verts));
        _meshData.setNormalBuffer(BufferUtils.createVector3Buffer(_meshData.getNormalBuffer(), verts));
        final TexCoords textureCoords = _meshData.getTextureCoords(0);
        _meshData.setTextureCoords(new TexCoords(BufferUtils.createVector3Buffer(
                textureCoords != null ? textureCoords.coords : null, verts)), 0);

        int pos = 0;

        Triangle[] old;
        if (useIkosa) {
            final int[] indices = new int[] { pos + 0, pos + 1, pos + 2, pos + 0, pos + 2, pos + 3, pos + 0, pos + 3,
                    pos + 4, pos + 0, pos + 4, pos + 5, pos + 0, pos + 5, pos + 1, pos + 1, pos + 10, pos + 6, pos + 2,
                    pos + 6, pos + 7, pos + 3, pos + 7, pos + 8, pos + 4, pos + 8, pos + 9, pos + 5, pos + 9, pos + 10,
                    pos + 6, pos + 2, pos + 1, pos + 7, pos + 3, pos + 2, pos + 8, pos + 4, pos + 3, pos + 9, pos + 5,
                    pos + 4, pos + 10, pos + 1, pos + 5, pos + 11, pos + 7, pos + 6, pos + 11, pos + 8, pos + 7,
                    pos + 11, pos + 9, pos + 8, pos + 11, pos + 10, pos + 9, pos + 11, pos + 6, pos + 10 };
            final float y = 0.4472f;
            final float a = 0.8944f;
            final float b = 0.2764f;
            final float c = 0.7236f;
            final float d = 0.8507f;
            final float e = 0.5257f;
            pos++;
            put(new Vector3(0, 1, 0));
            pos++;
            put(new Vector3(a, y, 0));
            pos++;
            put(new Vector3(b, y, -d));
            pos++;
            put(new Vector3(-c, y, -e));
            pos++;
            put(new Vector3(-c, y, e));
            pos++;
            put(new Vector3(b, y, d));
            pos++;
            put(new Vector3(c, -y, -e));
            pos++;
            put(new Vector3(-b, -y, -d));
            pos++;
            put(new Vector3(-a, -y, 0));
            pos++;
            put(new Vector3(-b, -y, d));
            pos++;
            put(new Vector3(c, -y, e));
            pos++;
            put(new Vector3(0, -1, 0));
            final Triangle[] ikosaedron = new Triangle[indices.length / 3];
            for (int i = 0; i < ikosaedron.length; i++) {
                final Triangle triangle = ikosaedron[i] = new Triangle();
                triangle.pt[0] = indices[i * 3];
                triangle.pt[1] = indices[i * 3 + 1];
                triangle.pt[2] = indices[i * 3 + 2];
            }

            old = ikosaedron;
        } else {
            /* Six equidistant points lying on the unit sphere */
            final Vector3 XPLUS = new Vector3(1, 0, 0); /* X */
            final Vector3 XMIN = new Vector3(-1, 0, 0); /* -X */
            final Vector3 YPLUS = new Vector3(0, 1, 0); /* Y */
            final Vector3 YMIN = new Vector3(0, -1, 0); /* -Y */
            final Vector3 ZPLUS = new Vector3(0, 0, 1); /* Z */
            final Vector3 ZMIN = new Vector3(0, 0, -1); /* -Z */

            final int xplus = pos++;
            put(XPLUS);
            final int xmin = pos++;
            put(XMIN);
            final int yplus = pos++;
            put(YPLUS);
            final int ymin = pos++;
            put(YMIN);
            final int zplus = pos++;
            put(ZPLUS);
            final int zmin = pos++;
            put(ZMIN);

            final Triangle[] octahedron = new Triangle[] { new Triangle(yplus, zplus, xplus),
                    new Triangle(xmin, zplus, yplus), new Triangle(ymin, zplus, xmin),
                    new Triangle(xplus, zplus, ymin), new Triangle(zmin, yplus, xplus),
                    new Triangle(zmin, xmin, yplus), new Triangle(zmin, ymin, xmin), new Triangle(zmin, xplus, ymin) };

            old = octahedron;
        }

        // if ( CLOCKWISE )
        // /* Reverse order of points in each triangle */
        // for ( int i = 0; i < old.length; i++ ) {
        // int tmp;
        // tmp = old[i].pt[0];
        // old[i].pt[0] = old[i].pt[2];
        // old[i].pt[2] = tmp;
        // }

        final Vector3 pt0 = new Vector3();
        final Vector3 pt1 = new Vector3();
        final Vector3 pt2 = new Vector3();

        /* Subdivide each starting triangle (maxlevels - 1) times */
        for (int level = 1; level < maxlevels; level++)

        {
            /* Allocate a next triangle[] */
            final Triangle[] next = new Triangle[old.length * 4];
            for (int i = 0; i < next.length; i++) {
                next[i] = new Triangle();
            }

            /*
             * Subdivide each polygon in the old approximation and normalize the next points thus generated to lie on
             * the surface of the unit sphere. Each input triangle with vertBuf labelled [0,1,2] as shown below will be
             * turned into four next triangles: Make next points a = (0+2)/2 b = (0+1)/2 c = (1+2)/2 1 /\ Normalize a,
             * b, c / \ b/____\ c Construct next triangles /\ /\ [0,b,a] / \ / \ [b,1,c] /____\/____\ [a,b,c] 0 a 2
             * [a,c,2]
             */
            for (int i = 0; i < old.length; i++) {
                int newi = i * 4;
                final Triangle oldt = old[i];
                Triangle newt = next[newi];

                BufferUtils.populateFromBuffer(pt0, vertBuf, oldt.pt[0]);
                BufferUtils.populateFromBuffer(pt1, vertBuf, oldt.pt[1]);
                BufferUtils.populateFromBuffer(pt2, vertBuf, oldt.pt[2]);
                final Vector3 av = createMidpoint(pt0, pt2).normalizeLocal();
                final Vector3 bv = createMidpoint(pt0, pt1).normalizeLocal();
                final Vector3 cv = createMidpoint(pt1, pt2).normalizeLocal();
                final int a = pos++;
                put(av);
                final int b = pos++;
                put(bv);
                final int c = pos++;
                put(cv);

                newt.pt[0] = oldt.pt[0];
                newt.pt[1] = b;
                newt.pt[2] = a;
                newt = next[++newi];

                newt.pt[0] = b;
                newt.pt[1] = oldt.pt[1];
                newt.pt[2] = c;
                newt = next[++newi];

                newt.pt[0] = a;
                newt.pt[1] = b;
                newt.pt[2] = c;
                newt = next[++newi];

                newt.pt[0] = a;
                newt.pt[1] = c;
                newt.pt[2] = oldt.pt[2];
            }

            /* Continue subdividing next triangles */
            old = next;
        }

        final IntBuffer indexBuffer = BufferUtils.createIntBuffer(tris * 3);
        _meshData.setIndexBuffer(indexBuffer);

        for (final Triangle triangle : old) {
            for (final int aPt : triangle.pt) {
                indexBuffer.put(aPt);
            }
        }
    }

    private void put(final Vector3 vec) {
        final FloatBuffer vertBuf = _meshData.getVertexBuffer();
        vertBuf.put((float) vec.getX());
        vertBuf.put((float) vec.getY());
        vertBuf.put((float) vec.getZ());

        final double length = vec.length();
        final FloatBuffer normBuf = _meshData.getNormalBuffer();
        final double xNorm = vec.getX() / length;
        normBuf.put((float) xNorm);
        final double yNorm = vec.getY() / length;
        normBuf.put((float) yNorm);
        final double zNorm = vec.getZ() / length;
        normBuf.put((float) zNorm);

        final FloatBuffer texBuf = _meshData.getTextureCoords(0).coords;
        texBuf.put((float) ((Math.atan2(yNorm, xNorm) / (2 * Math.PI) + 1) % 1));
        texBuf.put((float) (zNorm / 2 + 0.5));
    }

    /**
     * Compute the average of two vectors.
     * 
     * @param a
     *            first vector
     * @param b
     *            second vector
     * @return the average of two points
     */
    Vector3 createMidpoint(final Vector3 a, final Vector3 b) {
        return new Vector3((a.getX() + b.getX()) * 0.5, (a.getY() + b.getY()) * 0.5, (a.getZ() + b.getZ()) * 0.5);
    }
}
