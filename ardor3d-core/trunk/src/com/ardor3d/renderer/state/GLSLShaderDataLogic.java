/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.renderer.state;

import com.ardor3d.scenegraph.MeshData;

/**
 * Logic responsible for transfering data from a geometry to a shader before rendering
 */
public interface GLSLShaderDataLogic {
    /**
     * Responsible for transfering data from a Mesh object to a shader before rendering
     * 
     * @param shader
     *            Shader to update with new data(setUniform/setAttribute)
     * @param geom
     *            Mesh to retrieve data from
     */
    void applyData(GLSLShaderObjectsState shader, MeshData meshData);
}
