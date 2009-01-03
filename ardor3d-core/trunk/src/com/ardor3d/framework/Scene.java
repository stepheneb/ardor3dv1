/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.framework;

import com.ardor3d.annotation.MainThread;
import com.ardor3d.math.Ray3;
import com.ardor3d.renderer.Renderer;

/**
 * Owns all the data that is related to the scene. This class should not really know anything about rendering or the
 * screen, it's just the scene data. It is possibly a good idea to implement Controllers in the Scene's update method as
 * it is done right now - but I (Petter) think it is a good idea to think a bit about how animation and Controllers
 * relate to each other. It seems like animation is just a more complicated kind of Controller, really.
 */
public interface Scene {
    /**
     * 
     * @param renderer
     * @return true if a render occurred and we should swap the buffers.
     */
    @MainThread
    boolean renderUnto(Renderer renderer);

    /**
     * A scene should be able to handle a pick execution as it is the only thing that has a complete picture of the
     * scenegraph(s).
     * 
     * @param pickRay
     */
    void doPick(Ray3 pickRay);
}
