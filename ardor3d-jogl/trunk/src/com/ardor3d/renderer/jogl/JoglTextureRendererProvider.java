/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.renderer.jogl;

import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.TextureRenderer;
import com.ardor3d.renderer.TextureRendererProvider;

public class JoglTextureRendererProvider implements TextureRendererProvider {

    public TextureRenderer createTextureRenderer(final DisplaySettings settings, final Renderer renderer,
            final TextureRenderer.Target target) {

        return new JoglTextureRenderer(settings, (JoglRenderer) renderer);

    }

}
