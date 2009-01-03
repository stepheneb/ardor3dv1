/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.framework.lwjgl;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.ARBMultisample;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import com.ardor3d.annotation.MainThread;
import com.ardor3d.framework.CanvasRenderer;
import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.Scene;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.ContextManager;
import com.ardor3d.renderer.RenderContext;
import com.ardor3d.renderer.lwjgl.LwjglContextCapabilities;
import com.ardor3d.renderer.lwjgl.LwjglRenderer;
import com.google.inject.Inject;

public class LwjglCanvasRenderer implements CanvasRenderer {
    protected Scene scene;
    protected Camera camera;
    protected boolean headless;
    protected LwjglRenderer renderer;

    @Inject
    public LwjglCanvasRenderer(final Scene scene) {
        this.scene = scene;
    }

    @MainThread
    public void init(final DisplaySettings settings, final boolean headless) {
        this.headless = headless;
        final Object contextKey = this;
        try {
            GLContext.useContext(contextKey);
        } catch (final LWJGLException e) {
            throw new RuntimeException(e);
        }

        final LwjglContextCapabilities caps = new LwjglContextCapabilities(GLContext.getCapabilities());
        final RenderContext currentContext = new RenderContext(contextKey, caps);

        ContextManager.addContext(contextKey, currentContext);

        renderer = new LwjglRenderer(settings.getWidth(), settings.getHeight());
        currentContext.setupRecords(renderer);
        renderer.initDefaultStates();

        if (settings.getSamples() != 0 && caps.isMultisampleSupported()) {
            GL11.glEnable(ARBMultisample.GL_MULTISAMPLE_ARB);
        }

        renderer.setBackgroundColor(ColorRGBA.BLACK);

        /** Set up how our camera sees. */
        camera = new Camera(settings.getWidth(), settings.getHeight());
        camera.setFrustumPerspective(45.0f, (float) settings.getWidth() / (float) settings.getHeight(), 1, 1000);
        camera.setParallelProjection(false);

        final Vector3 loc = new Vector3(0.0f, 0.0f, 10.0f);
        final Vector3 left = new Vector3(-1.0f, 0.0f, 0.0f);
        final Vector3 up = new Vector3(0.0f, 1.0f, 0.0f);
        final Vector3 dir = new Vector3(0.0f, 0f, -1.0f);
        /** Move our camera to a correct place and orientation. */
        camera.setFrame(loc, left, up, dir);
    }

    @MainThread
    public boolean draw() {
        // set up context+display for rendering this canvas
        renderer.setHeadless(headless);
        renderer.setSize(camera.getWidth(), camera.getHeight());
        ContextManager.switchContext(this);
        try {
            GLContext.useContext(this);
        } catch (final LWJGLException e) {
            throw new RuntimeException(e);
        }

        // render stuff
        if (ContextManager.getCurrentContext().getCurrentCamera() != camera) {
            ContextManager.getCurrentContext().setCurrentCamera(camera);
            camera.update();
        }
        camera.apply(renderer);
        renderer.clearBuffers();
        final boolean drew = scene.renderUnto(renderer);
        renderer.displayBackBuffer();
        try {
            GLContext.useContext(null);
        } catch (final LWJGLException e) {
            throw new RuntimeException(e);
        }
        return drew;
    }

    public Camera getCamera() {
        return camera;
    }

    public Scene getScene() {
        return scene;
    }

    public void cleanup() {
        renderer.cleanup();
    }
}