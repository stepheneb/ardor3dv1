/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.example.renderer;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.example.ExampleBase;
import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.FrameWork;
import com.ardor3d.image.Image;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture2D;
import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Quaternion;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.TextureRenderer;
import com.ardor3d.renderer.TextureRendererFactory;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Spatial.CullHint;
import com.ardor3d.scenegraph.Spatial.LightCombineMode;
import com.ardor3d.scenegraph.shape.Quad;
import com.ardor3d.scenegraph.shape.Sphere;
import com.ardor3d.ui.text.BasicText;
import com.ardor3d.util.TextureManager;
import com.google.inject.Inject;

public class RenderTextureSideBySideExample extends ExampleBase {
    private final Quaternion rotQuat = new Quaternion();
    private double angle = 0;
    private final Vector3 axis = new Vector3(1, 1, 0);
    private Sphere sphere;
    private Quad orthoQuad;
    private Quad quad;
    private TextureRenderer textureRenderer;
    private Texture2D fakeTex;
    private final DisplaySettings _settings;

    public static void main(final String[] args) {
        start(RenderTextureSideBySideExample.class);
    }

    @Inject
    public RenderTextureSideBySideExample(final LogicalLayer layer,
            final FrameWork frameWork, final DisplaySettings settings) {
        super(layer, frameWork);
        _settings = settings;
    }

    @Override
    protected void quit(final Renderer r) {
        try {
            textureRenderer.cleanup();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        super.quit(r);
    }

    @Override
    protected void updateExample(final double tpf) {
        if (tpf < 1) {
            angle = angle + (tpf * 1);
            if (angle > 360) {
                angle = 0;
            }
        }

        rotQuat.fromAngleAxis(angle, axis);
        sphere.setRotation(rotQuat);

    }

    @Override
    protected void renderExample(final Renderer renderer) {
        if (textureRenderer == null) {
            initRtt(renderer);
        }
        if (textureRenderer.isSupported()) {
            final Camera camera = _canvas.getCanvasRenderer().getCamera();
            textureRenderer.getCamera().setLocation(camera.getLocation());
            textureRenderer.getCamera().setUp(camera.getUp());
            textureRenderer.getCamera().setLeft(camera.getLeft());
            textureRenderer.getCamera().setDirection(camera.getDirection());
            textureRenderer.render(sphere, fakeTex);
        }
    }

    private void initRtt(final Renderer renderer) {
        final DisplaySettings rttSettings = new DisplaySettings(512, 512, _settings.getColorDepth(), _settings
                .getFrequency(), _settings.getAlphaBits(), _settings.getDepthBits(), _settings.getStencilBits(),
                _settings.getSamples(), _settings.isFullScreen());
        textureRenderer = TextureRendererFactory.INSTANCE.createTextureRenderer(rttSettings, renderer,
                TextureRenderer.Target.Texture2D);

        if (!textureRenderer.isSupported()) {
            final BasicText t = BasicText.createDefaultTextLabel("Text", "RTT not supported on this computer.");
            t.setRenderBucketType(RenderBucketType.Ortho);
            t.setLightCombineMode(LightCombineMode.Off);
            t.setTranslation(new Vector3(0, 20, 0));
            _root.attachChild(t);
        } else {
            textureRenderer.setBackgroundColor(new ColorRGBA(0.3f, 0.3f, 0.4f, 1));
            fakeTex = new Texture2D();
            textureRenderer.setupTexture(fakeTex);
            final TextureState screen = new TextureState();
            screen.setTexture(fakeTex);
            screen.setEnabled(true);
            quad.setRenderState(screen);
            orthoQuad.setRenderState(screen);
        }
    }

    @Override
    protected void initExample() {
        _canvas.setTitle("RTT Side By Side");

        sphere = new Sphere("Sphere", 25, 25, 5);
        sphere.updateModelBound();
        sphere.setTranslation(new Vector3(-6, 0, 0));
        _root.attachChild(sphere);

        orthoQuad = new Quad("OrthoQuad", 150, 150);
        orthoQuad.setTranslation(new Vector3(80, 80, 0));
        orthoQuad.setModelBound(new BoundingBox());
        orthoQuad.setRenderBucketType(RenderBucketType.Ortho);
        orthoQuad.updateModelBound();
        orthoQuad.setLightCombineMode(LightCombineMode.Off);
        orthoQuad.setCullHint(CullHint.Never);
        _root.attachChild(orthoQuad);

        quad = new Quad("Quad", 10, 10);
        quad.setTranslation(new Vector3(6, 0, 0));
        quad.setModelBound(new BoundingBox());
        quad.updateModelBound();
        quad.setLightCombineMode(LightCombineMode.Off);
        _root.attachChild(quad);

        final TextureState ts = new TextureState();
        ts.setEnabled(true);
        ts.setTexture(TextureManager.load("images/ardor3d_white_256.jpg", Texture.MinificationFilter.Trilinear,
                Image.Format.Guess, true));

        _root.setRenderState(ts);
    }
}
