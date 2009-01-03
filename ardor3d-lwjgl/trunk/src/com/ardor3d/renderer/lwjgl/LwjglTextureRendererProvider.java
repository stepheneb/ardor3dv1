/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.renderer.lwjgl;

import java.util.logging.Logger;

import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.TextureRenderer;
import com.ardor3d.renderer.TextureRendererFactory;
import com.ardor3d.renderer.TextureRendererProvider;

public class LwjglTextureRendererProvider implements TextureRendererProvider {

    private static final Logger logger = Logger.getLogger(TextureRendererFactory.class.getName());

    public TextureRenderer createTextureRenderer(final DisplaySettings settings, final Renderer renderer,
            final TextureRenderer.Target target) {

        TextureRenderer textureRenderer = new LwjglTextureRenderer(settings, (LwjglRenderer) renderer);

        if (!textureRenderer.isSupported()) {
            logger.fine("FBO not supported, attempting Pbuffer.");

            textureRenderer = new LwjglPbufferTextureRenderer(settings, target, (LwjglRenderer) renderer);
        }

        return textureRenderer;
    }

}
