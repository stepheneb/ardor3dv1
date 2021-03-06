/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.renderer.queue;

import java.util.Comparator;

import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.RenderState;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Spatial;

public class OpaqueRenderBucket extends AbstractRenderBucket {

    public OpaqueRenderBucket(final Renderer renderer) {
        super(renderer);

        _comparator = new OpaqueComparator();
    }

    private class OpaqueComparator implements Comparator<Spatial> {
        public int compare(final Spatial o1, final Spatial o2) {
            if (o1 instanceof Mesh && o2 instanceof Mesh) {
                return compareByStates((Mesh) o1, (Mesh) o2);
            }

            final double d1 = distanceToCam(o1);
            final double d2 = distanceToCam(o2);
            return Double.compare(d1, d2);
        }

        /**
         * Compare opaque items by their texture states - generally the most expensive switch. Later this might expand
         * to comparisons by other states as well, such as lighting or material.
         */
        private int compareByStates(final Mesh g1, final Mesh g2) {
            final TextureState ts1 = (TextureState) g1._getWorldRenderState(RenderState.StateType.Texture);
            final TextureState ts2 = (TextureState) g2._getWorldRenderState(RenderState.StateType.Texture);
            if (ts1 == ts2) {
                return 0;
            } else if (ts1 == null && ts2 != null) {
                return -1;
            } else if (ts2 == null && ts1 != null) {
                return 1;
            }

            for (int x = 0, nots = Math.min(ts1.getNumberOfSetTextures(), ts2.getNumberOfSetTextures()); x < nots; x++) {

                final int tid1 = ts1.getTextureID(x);
                final int tid2 = ts2.getTextureID(x);
                if (tid1 == tid2) {
                    continue;
                } else if (tid1 < tid2) {
                    return -1;
                } else {
                    return 1;
                }
            }

            if (ts1.getNumberOfSetTextures() != ts2.getNumberOfSetTextures()) {
                return ts2.getNumberOfSetTextures() - ts1.getNumberOfSetTextures();
            }

            return 0;
        }
    }

}
