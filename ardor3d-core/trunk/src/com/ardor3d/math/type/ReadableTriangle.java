/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.math.type;

public interface ReadableTriangle {
    public int getIndex();

    public ReadableVector3 get(final int index);

    public ReadableVector3 getA();

    public ReadableVector3 getB();

    public ReadableVector3 getC();

    public ReadableVector3 getNormal();

    public ReadableVector3 getCenter();
}
