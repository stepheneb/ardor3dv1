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

import java.util.Arrays;
import java.util.Comparator;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.ContextManager;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.SortUtil;

public class AbstractRenderBucket implements RenderBucket {
    protected final Renderer _renderer;

    protected Spatial[] _list, _tempList;
    protected int _listSize;

    protected Spatial[] _backList;
    protected int _backListSize;

    protected Comparator<Spatial> _comparator;

    public AbstractRenderBucket(final Renderer renderer) {
        _renderer = renderer;

        _list = new Spatial[32];
        _backList = new Spatial[32];
    }

    public void add(final Spatial spatial) {
        if (_listSize == _list.length) {
            final Spatial[] temp = new Spatial[_listSize * 2];
            System.arraycopy(_list, 0, temp, 0, _listSize);
            _list = temp;
        }
        _list[_listSize++] = spatial;
    }

    public void clear() {
        for (int i = 0; i < _listSize; i++) {
            _list[i] = null;
        }
        if (_tempList != null) {
            Arrays.fill(_tempList, null);
        }
        _listSize = 0;
    }

    public void render() {
        for (int i = 0; i < _listSize; i++) {
            _list[i].draw(_renderer);
        }
    }

    public void sort() {
        if (_listSize > 1) {
            // resize or populate our temporary array as necessary
            if (_tempList == null || _tempList.length != _list.length) {
                _tempList = _list.clone();
            } else {
                System.arraycopy(_list, 0, _tempList, 0, _list.length);
            }
            // now merge sort tlist into list
            SortUtil.msort(_tempList, _list, 0, _listSize, _comparator);
        }
    }

    public void swap() {
        final Spatial[] tmpList = _list;
        _list = _backList;
        _backList = tmpList;

        final int tmpListSize = _listSize;
        _listSize = _backListSize;
        _backListSize = tmpListSize;
    }

    /**
     * Calculates the distance from a spatial to the camera. Distance is a squared distance.
     * 
     * @param spat
     *            Spatial to check distance.
     * @return Distance from Spatial to current context's camera.
     */
    protected double distanceToCam(final Spatial spat) {
        // this optimization should not be stored in the spatial
        // if (spat.queueDistance != Double.NEGATIVE_INFINITY) {
        // return spat.queueDistance;
        // }

        final Camera cam = ContextManager.getCurrentContext().getCurrentCamera();

        // spat.queueDistance = 0;

        ReadOnlyVector3 spatPosition;
        if (spat.getWorldBound() != null && Vector3.isValid(spat.getWorldBound().getCenter())) {
            spatPosition = spat.getWorldBound().getCenter();
        } else {
            spatPosition = spat.getWorldTranslation();
            if (!Vector3.isValid(spatPosition)) {
                return Double.NEGATIVE_INFINITY;
            }
        }

        return cam.distanceToCam(spatPosition);
        // return spat.queueDistance;
    }
}
