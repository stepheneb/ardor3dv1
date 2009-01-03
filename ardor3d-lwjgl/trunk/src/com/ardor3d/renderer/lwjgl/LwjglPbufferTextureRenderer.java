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

import java.nio.IntBuffer;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.AWTGLCanvas;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.opengl.RenderTexture;

import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture2D;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.ContextManager;
import com.ardor3d.renderer.RenderContext;
import com.ardor3d.renderer.TextureRenderer;
import com.ardor3d.scene.state.lwjgl.LwjglTextureStateUtil;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.Ardor3dException;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;

/**
 * This class is used by LWJGL to render textures. Users should <b>not </b> create this class directly. Instead, allow
 * DisplaySystem to create it for you.
 * 
 * @see com.ardor3d.system.DisplaySystem#createTextureRenderer
 */
public class LwjglPbufferTextureRenderer implements TextureRenderer {
    private static final Logger logger = Logger.getLogger(LwjglPbufferTextureRenderer.class.getName());

    private Camera camera;

    private final ColorRGBA backgroundColor = new ColorRGBA(1, 1, 1, 1);

    private int pBufferWidth = 16;

    private int pBufferHeight = 16;

    /* Pbuffer instance */
    private Pbuffer pbuffer;

    private int active;

    final int caps;

    private boolean useDirectRender = false;

    private boolean isSupported = true;

    private final LwjglRenderer parentRenderer;

    private RenderTexture texture;

    private final boolean headless = false;

    private int bpp = 0, alpha = 0, depth = 0, stencil = 0, samples = 0;

    public LwjglPbufferTextureRenderer(final DisplaySettings settings, final TextureRenderer.Target target,
            final LwjglRenderer parentRenderer) {
        this.parentRenderer = parentRenderer;

        caps = Pbuffer.getCapabilities();
        isSupported = (caps & Pbuffer.PBUFFER_SUPPORTED) != 0;
        if (!isSupported) {
            logger.warning("Pbuffer not supported.");
            return;
        }

        bpp = settings.getColorDepth();
        alpha = settings.getAlphaBits();
        depth = settings.getDepthBits();
        stencil = settings.getStencilBits();
        samples = settings.getSamples();

        int pTarget = RenderTexture.RENDER_TEXTURE_2D;

        // XXX: It seems this does not work properly on many cards...
        final boolean nonPow2Support = false; // ((Pbuffer.getCapabilities() &
        // Pbuffer.RENDER_TEXTURE_RECTANGLE_SUPPORTED) != 0);

        int width = settings.getWidth();
        int height = settings.getHeight();
        if (!MathUtils.isPowerOfTwo(width) || !MathUtils.isPowerOfTwo(height)) {
            // If we don't support non-pow2 textures in pbuffers, we need to resize them.
            if (!nonPow2Support) {
                if (!MathUtils.isPowerOfTwo(width)) {
                    int newWidth = 2;
                    do {
                        newWidth <<= 1;
                    } while (newWidth < width);
                    width = newWidth;
                }

                if (!MathUtils.isPowerOfTwo(height)) {
                    int newHeight = 2;
                    do {
                        newHeight <<= 1;
                    } while (newHeight < height);
                    height = newHeight;
                }
            } else {
                pTarget = RenderTexture.RENDER_TEXTURE_RECTANGLE;
            }

            // sanity check
            if (width <= 0) {
                width = 16;
            }
            if (height <= 0) {
                height = 16;
            }
        }

        switch (target) {
            case Texture1D:
                pTarget = RenderTexture.RENDER_TEXTURE_1D;
                break;
            case TextureCubeMap:
                pTarget = RenderTexture.RENDER_TEXTURE_CUBE_MAP;
                break;

        }

        pBufferWidth = width;
        pBufferHeight = height;

        // boolean useRGB, boolean useRGBA, boolean useDepth, boolean isRectangle, int target, int mipmaps
        texture = new RenderTexture(false, true, true, pTarget == RenderTexture.RENDER_TEXTURE_RECTANGLE, pTarget, 0);

        setMultipleTargets(false);

        logger.fine("Creating Pbuffer sized: " + pBufferWidth + " x " + pBufferHeight);
        initPbuffer();
    }

    /**
     * 
     * <code>isSupported</code> obtains the capability of the graphics card. If the graphics card does not have pbuffer
     * support, false is returned, otherwise, true is returned. TextureRenderer will not process any scene elements if
     * pbuffer is not supported.
     * 
     * @return if this graphics card supports pbuffers or not.
     */
    public boolean isSupported() {
        return isSupported;
    }

    /**
     * <code>getCamera</code> retrieves the camera this renderer is using.
     * 
     * @return the camera this renderer is using.
     */
    public Camera getCamera() {
        return camera;
    }

    /**
     * <code>setCamera</code> sets the camera this renderer should use.
     * 
     * @param camera
     *            the camera this renderer should use.
     */
    public void setCamera(final Camera camera) {
        this.camera = camera;
    }

    public void setBackgroundColor(final ColorRGBA c) {

        backgroundColor.set(c);

        if (!isSupported) {
            return;
        }

        try {
            activate();
            GL11.glClearColor(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(),
                    backgroundColor.getAlpha());
        } finally {
            deactivate();
        }
    }

    /**
     * <code>getBackgroundColor</code> retrieves the clear color of the current OpenGL context.
     * 
     * @see com.ardor3d.renderer.Renderer#getBackgroundColor()
     * @return the current clear color.
     */
    public ColorRGBA getBackgroundColor(final ColorRGBA store) {
        return store.set(backgroundColor);
    }

    /**
     * <code>setupTexture</code> initializes a new Texture object for use with TextureRenderer. Generates a valid gl
     * texture id for this texture and inits the data type for the texture.
     */
    public void setupTexture(final Texture2D tex) {
        setupTexture(tex, pBufferWidth, pBufferHeight);
    }

    /**
     * <code>setupTexture</code> initializes a new Texture object for use with TextureRenderer. Generates a valid gl
     * texture id for this texture and inits the data type for the texture.
     */
    public void setupTexture(final Texture2D tex, final int width, final int height) {
        if (!isSupported) {
            return;
        }

        final IntBuffer ibuf = BufferUtils.createIntBuffer(1);

        if (tex.getTextureId() != 0) {
            ibuf.put(tex.getTextureId());
            GL11.glDeleteTextures(ibuf);
            ibuf.clear();
        }

        // Create the texture
        GL11.glGenTextures(ibuf);
        tex.setTextureId(ibuf.get(0));
        TextureManager.registerForCleanup(tex.getTextureKey(), tex.getTextureId());
        LwjglTextureStateUtil.doTextureBind(tex.getTextureId(), 0, Texture.Type.TwoDimensional);

        int source = GL11.GL_RGBA;
        switch (tex.getRTTSource()) {
            case RGBA:
            case RGBA8:
                break;
            case RGB:
            case RGB8:
                source = GL11.GL_RGB;
                break;
            case Alpha:
            case Alpha8:
                source = GL11.GL_ALPHA;
                break;
            case Depth:
                source = GL11.GL_DEPTH_COMPONENT;
                break;
            case Intensity:
            case Intensity8:
                source = GL11.GL_INTENSITY;
                break;
            case Luminance:
            case Luminance8:
                source = GL11.GL_LUMINANCE;
                break;
            case LuminanceAlpha:
            case Luminance8Alpha8:
                source = GL11.GL_LUMINANCE_ALPHA;
                break;
            case Alpha4:
                source = GL11.GL_ALPHA;
                break;
            case Alpha12:
                source = GL11.GL_ALPHA;
                break;
            case Alpha16:
                source = GL11.GL_ALPHA;
                break;
            case Luminance4:
                source = GL11.GL_LUMINANCE;
                break;
            case Luminance12:
                source = GL11.GL_LUMINANCE;
                break;
            case Luminance16:
                source = GL11.GL_LUMINANCE;
                break;
            case Luminance4Alpha4:
                source = GL11.GL_LUMINANCE_ALPHA;
                break;
            case Luminance6Alpha2:
                source = GL11.GL_LUMINANCE_ALPHA;
                break;
            case Luminance12Alpha4:
                source = GL11.GL_LUMINANCE_ALPHA;
                break;
            case Luminance12Alpha12:
                source = GL11.GL_LUMINANCE_ALPHA;
                break;
            case Luminance16Alpha16:
                source = GL11.GL_LUMINANCE_ALPHA;
                break;
            case Intensity4:
                source = GL11.GL_INTENSITY;
                break;
            case Intensity12:
                source = GL11.GL_INTENSITY;
                break;
            case Intensity16:
                source = GL11.GL_INTENSITY;
                break;
            case R3_G3_B2:
                source = GL11.GL_RGB;
                break;
            case RGB4:
                source = GL11.GL_RGB;
                break;
            case RGB5:
                source = GL11.GL_RGB;
                break;
            case RGB10:
                source = GL11.GL_RGB;
                break;
            case RGB12:
                source = GL11.GL_RGB;
                break;
            case RGB16:
                source = GL11.GL_RGB;
                break;
            case RGBA2:
                source = GL11.GL_RGBA;
                break;
            case RGBA4:
                source = GL11.GL_RGBA;
                break;
            case RGB5_A1:
                source = GL11.GL_RGBA;
                break;
            case RGB10_A2:
                source = GL11.GL_RGBA;
                break;
            case RGBA12:
                source = GL11.GL_RGBA;
                break;
            case RGBA16:
                source = GL11.GL_RGBA;
                break;
            case RGBA32F:
                source = GL11.GL_RGBA;
                break;
            case RGB32F:
                source = GL11.GL_RGB;
                break;
            case Alpha32F:
                source = GL11.GL_ALPHA;
                break;
            case Intensity32F:
                source = GL11.GL_INTENSITY;
                break;
            case Luminance32F:
                source = GL11.GL_LUMINANCE;
                break;
            case LuminanceAlpha32F:
                source = GL11.GL_LUMINANCE_ALPHA;
                break;
            case RGBA16F:
                source = GL11.GL_RGBA;
                break;
            case RGB16F:
                source = GL11.GL_RGB;
                break;
            case Alpha16F:
                source = GL11.GL_ALPHA;
                break;
            case Intensity16F:
                source = GL11.GL_INTENSITY;
                break;
            case Luminance16F:
                source = GL11.GL_LUMINANCE;
                break;
            case LuminanceAlpha16F:
                source = GL11.GL_LUMINANCE_ALPHA;
                break;
        }
        GL11.glCopyTexImage2D(GL11.GL_TEXTURE_2D, 0, source, 0, 0, width, height, 0);
        logger.fine("setup tex" + tex.getTextureId() + ": " + width + "," + height);
    }

    public void render(final Spatial spat, final Texture tex) {
        render(spat, tex, true);
    }

    /**
     * <code>render</code> renders a scene. As it recieves a base class of <code>Spatial</code> the renderer hands off
     * management of the scene to spatial for it to determine when a <code>Geometry</code> leaf is reached. The result
     * of the rendering is then copied into the given texture(s). What is copied is based on the Texture object's
     * rttSource field.
     * 
     * NOTE: If more than one texture is given, copy-texture is used regardless of card capabilities to decrease render
     * time.
     * 
     * @param spat
     *            the scene to render.
     * @param tex
     *            the Texture(s) to render it to.
     */
    public void render(final Spatial spat, final Texture tex, final boolean doClear) {
        if (!isSupported) {
            return;
        }

        // clear the current states since we are renderering into a new location
        // and can not rely on states still being set.
        try {
            if (pbuffer.isBufferLost()) {
                logger.warning("PBuffer contents lost - will recreate the buffer");
                deactivate();
                pbuffer.destroy();
                initPbuffer();
            }

            // Override parent's last frustum test to avoid accidental incorrect
            // cull
            if (spat.getParent() != null) {
                spat.getParent().setLastFrustumIntersection(Camera.FrustumIntersect.Intersects);
            }

            if (useDirectRender && tex.getRTTSource() != Texture.RenderToTextureType.Depth) {
                // setup and render directly to a 2d texture.
                pbuffer.releaseTexImage(Pbuffer.FRONT_LEFT_BUFFER);
                activate();
                switchCameraIn(doClear);
                doDraw(spat);
                deactivate();
                switchCameraOut();
                LwjglTextureStateUtil.doTextureBind(tex.getTextureId(), 0, Texture.Type.TwoDimensional);
                pbuffer.bindTexImage(Pbuffer.FRONT_LEFT_BUFFER);
            } else {
                // render and copy to a texture
                activate();
                switchCameraIn(doClear);
                doDraw(spat);
                switchCameraOut();

                copyToTexture(tex, pBufferWidth, pBufferHeight);

                deactivate();
            }

        } catch (final Exception e) {
            logger.logp(Level.SEVERE, this.getClass().toString(), "render(Spatial, Texture)", "Exception", e);
        }
    }

    // inherited docs
    public void render(final List<? extends Spatial> spats, final List<Texture> texs) {
        render(spats, texs, true);
    }

    public void render(final List<? extends Spatial> spats, final List<Texture> texs, final boolean doClear) {
        if (!isSupported) {
            return;
        }

        // clear the current states since we are renderering into a new location
        // and can not rely on states still being set.
        try {
            if (pbuffer.isBufferLost()) {
                logger.warning("PBuffer contents lost - will recreate the buffer");
                deactivate();
                pbuffer.destroy();
                initPbuffer();
            }

            if (texs.size() == 1 && useDirectRender && texs.get(0).getRTTSource() != Texture.RenderToTextureType.Depth) {
                // setup and render directly to a 2d texture.
                LwjglTextureStateUtil.doTextureBind(texs.get(0).getTextureId(), 0, Texture.Type.TwoDimensional);
                activate();
                switchCameraIn(doClear);
                pbuffer.releaseTexImage(Pbuffer.FRONT_LEFT_BUFFER);
                for (int x = 0, max = spats.size(); x < max; x++) {
                    final Spatial spat = spats.get(x);
                    // Override parent's last frustum test to avoid accidental incorrect
                    // cull
                    if (spat.getParent() != null) {
                        spat.getParent().setLastFrustumIntersection(Camera.FrustumIntersect.Intersects);
                    }

                    doDraw(spat);
                }
                switchCameraOut();

                deactivate();
                pbuffer.bindTexImage(Pbuffer.FRONT_LEFT_BUFFER);
            } else {
                // render and copy to a texture
                activate();
                switchCameraIn(doClear);
                for (int x = 0, max = spats.size(); x < max; x++) {
                    final Spatial spat = spats.get(x);
                    // Override parent's last frustum test to avoid accidental incorrect
                    // cull
                    if (spat.getParent() != null) {
                        spat.getParent().setLastFrustumIntersection(Camera.FrustumIntersect.Intersects);
                    }

                    doDraw(spat);
                }
                switchCameraOut();

                for (int i = 0; i < texs.size(); i++) {
                    copyToTexture(texs.get(i), pBufferWidth, pBufferHeight);
                }

                deactivate();
            }

        } catch (final Exception e) {
            logger.logp(Level.SEVERE, this.getClass().toString(), "render(Spatial, Texture)", "Exception", e);
        }
    }

    /**
     * <code>copyToTexture</code> copies the pbuffer contents to the given Texture. What is copied is up to the Texture
     * object's rttSource field.
     * 
     * @param tex
     *            The Texture to copy into.
     * @param width
     *            the width of the texture image
     * @param height
     *            the height of the texture image
     */
    public void copyToTexture(final Texture tex, final int width, final int height) {
        LwjglTextureStateUtil.doTextureBind(tex.getTextureId(), 0, Texture.Type.TwoDimensional);

        int source = GL11.GL_RGBA;
        switch (tex.getRTTSource()) {
            case RGBA:
            case RGBA8:
                break;
            case RGB:
            case RGB8:
                source = GL11.GL_RGB;
                break;
            case Alpha:
            case Alpha8:
                source = GL11.GL_ALPHA;
                break;
            case Depth:
                source = GL11.GL_DEPTH_COMPONENT;
                break;
            case Intensity:
            case Intensity8:
                source = GL11.GL_INTENSITY;
                break;
            case Luminance:
            case Luminance8:
                source = GL11.GL_LUMINANCE;
                break;
            case LuminanceAlpha:
            case Luminance8Alpha8:
                source = GL11.GL_LUMINANCE_ALPHA;
                break;
            case Alpha4:
                source = GL11.GL_ALPHA;
                break;
            case Alpha12:
                source = GL11.GL_ALPHA;
                break;
            case Alpha16:
                source = GL11.GL_ALPHA;
                break;
            case Luminance4:
                source = GL11.GL_LUMINANCE;
                break;
            case Luminance12:
                source = GL11.GL_LUMINANCE;
                break;
            case Luminance16:
                source = GL11.GL_LUMINANCE;
                break;
            case Luminance4Alpha4:
                source = GL11.GL_LUMINANCE_ALPHA;
                break;
            case Luminance6Alpha2:
                source = GL11.GL_LUMINANCE_ALPHA;
                break;
            case Luminance12Alpha4:
                source = GL11.GL_LUMINANCE_ALPHA;
                break;
            case Luminance12Alpha12:
                source = GL11.GL_LUMINANCE_ALPHA;
                break;
            case Luminance16Alpha16:
                source = GL11.GL_LUMINANCE_ALPHA;
                break;
            case Intensity4:
                source = GL11.GL_INTENSITY;
                break;
            case Intensity12:
                source = GL11.GL_INTENSITY;
                break;
            case Intensity16:
                source = GL11.GL_INTENSITY;
                break;
            case R3_G3_B2:
                source = GL11.GL_RGB;
                break;
            case RGB4:
                source = GL11.GL_RGB;
                break;
            case RGB5:
                source = GL11.GL_RGB;
                break;
            case RGB10:
                source = GL11.GL_RGB;
                break;
            case RGB12:
                source = GL11.GL_RGB;
                break;
            case RGB16:
                source = GL11.GL_RGB;
                break;
            case RGBA2:
                source = GL11.GL_RGBA;
                break;
            case RGBA4:
                source = GL11.GL_RGBA;
                break;
            case RGB5_A1:
                source = GL11.GL_RGBA;
                break;
            case RGB10_A2:
                source = GL11.GL_RGBA;
                break;
            case RGBA12:
                source = GL11.GL_RGBA;
                break;
            case RGBA16:
                source = GL11.GL_RGBA;
                break;
            case RGBA32F:
                source = GL11.GL_RGBA;
                break;
            case RGB32F:
                source = GL11.GL_RGB;
                break;
            case Alpha32F:
                source = GL11.GL_ALPHA;
                break;
            case Intensity32F:
                source = GL11.GL_INTENSITY;
                break;
            case Luminance32F:
                source = GL11.GL_LUMINANCE;
                break;
            case LuminanceAlpha32F:
                source = GL11.GL_LUMINANCE_ALPHA;
                break;
            case RGBA16F:
                source = GL11.GL_RGBA;
                break;
            case RGB16F:
                source = GL11.GL_RGB;
                break;
            case Alpha16F:
                source = GL11.GL_ALPHA;
                break;
            case Intensity16F:
                source = GL11.GL_INTENSITY;
                break;
            case Luminance16F:
                source = GL11.GL_LUMINANCE;
                break;
            case LuminanceAlpha16F:
                source = GL11.GL_LUMINANCE_ALPHA;
                break;
        }
        GL11.glCopyTexImage2D(GL11.GL_TEXTURE_2D, 0, source, 0, 0, width, height, 0);
    }

    private Camera oldCamera;
    private int oldWidth, oldHeight;

    private RenderContext oldContext;

    private void switchCameraIn(final boolean doClear) {
        // grab non-rtt settings
        oldWidth = parentRenderer.getWidth();
        oldHeight = parentRenderer.getHeight();

        // swap to rtt settings
        parentRenderer.getQueue().swapBuckets();
        parentRenderer.setSize(pBufferWidth, pBufferHeight);

        // clear the scene
        if (doClear) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            parentRenderer.clearBuffers();
        }

        getCamera().update();
        getCamera().apply(parentRenderer);
    }

    private void switchCameraOut() {
        parentRenderer.setSize(oldWidth, oldHeight);

        // back to the non rtt settings
        parentRenderer.getQueue().swapBuckets();
        oldCamera.update();
        oldCamera.apply(parentRenderer);
    }

    private void doDraw(final Spatial spat) {
        // do rtt scene render
        spat.onDraw(parentRenderer);
        parentRenderer.renderQueue();
    }

    private void initPbuffer() {
        if (!isSupported) {
            return;
        }

        try {
            if (pbuffer != null) {
                giveBackContext();
                ContextManager.removeContext(pbuffer);
            }
            final PixelFormat format = new PixelFormat(alpha, depth, stencil).withSamples(samples);
            pbuffer = new Pbuffer(pBufferWidth, pBufferHeight, format, texture, null);
        } catch (final Exception e) {
            logger.logp(Level.SEVERE, this.getClass().toString(), "initPbuffer()", "Exception", e);

            if (texture != null && useDirectRender) {
                logger
                        .warning("Your card claims to support Render to Texture but fails to enact it.  Updating your driver might solve this problem.");
                logger.warning("Attempting to fall back to Copy Texture.");
                texture = null;
                useDirectRender = false;
                initPbuffer();
                return;
            }

            logger.log(Level.WARNING, "Failed to create Pbuffer.", e);
            isSupported = false;
            return;
        }

        try {
            activate();

            pBufferWidth = pbuffer.getWidth();
            pBufferHeight = pbuffer.getHeight();

            GL11.glClearColor(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(),
                    backgroundColor.getAlpha());

            if (camera == null) {
                initCamera();
            }
            camera.update();

            deactivate();
        } catch (final Exception e) {
            logger.log(Level.WARNING, "Failed to initialize created Pbuffer.", e);
            isSupported = false;
            return;
        }
    }

    private void activate() {
        if (!isSupported) {
            return;
        }
        if (active == 0) {
            try {
                oldContext = ContextManager.getCurrentContext();
                pbuffer.makeCurrent();
                ContextManager.switchContext(pbuffer);
            } catch (final LWJGLException e) {
                logger.logp(Level.SEVERE, this.getClass().toString(), "activate()", "Exception", e);
                throw new Ardor3dException();
            }
        }
        active++;
    }

    private void deactivate() {
        if (!isSupported) {
            return;
        }
        if (active == 1) {
            try {
                if (!useDirectRender) {
                    ContextManager.getCurrentContext().invalidateStates();
                }
                giveBackContext();
                parentRenderer.reset();
            } catch (final LWJGLException e) {
                logger.logp(Level.SEVERE, this.getClass().toString(), "deactivate()", "Exception", e);
                throw new Ardor3dException();
            }
        }
        active--;
    }

    private void giveBackContext() throws LWJGLException {
        if (!headless && Display.isCreated()) {
            Display.makeCurrent();
            ContextManager.switchContext(null); // TODO: need ref to parent context?
        } else if (oldContext.getContextHolder() instanceof AWTGLCanvas) {
            ((AWTGLCanvas) oldContext.getContextHolder()).makeCurrent();
            ContextManager.switchContext(oldContext.getContextHolder());
        }
    }

    private void initCamera() {
        if (!isSupported) {
            return;
        }
        camera = new Camera(pBufferWidth, pBufferHeight);
        camera.setFrustum(1.0f, 1000.0f, -0.50f, 0.50f, 0.50f, -0.50f);
        final Vector3 loc = new Vector3(0.0f, 0.0f, 0.0f);
        final Vector3 left = new Vector3(-1.0f, 0.0f, 0.0f);
        final Vector3 up = new Vector3(0.0f, 1.0f, 0.0f);
        final Vector3 dir = new Vector3(0.0f, 0f, -1.0f);
        camera.setFrame(loc, left, up, dir);
    }

    public void cleanup() {
        if (!isSupported) {
            return;
        }

        ContextManager.removeContext(pbuffer);
        pbuffer.destroy();
    }

    public int getWidth() {
        return pBufferWidth;
    }

    public int getHeight() {
        return pBufferHeight;
    }

    public void setMultipleTargets(final boolean force) {
        if (force) {
            logger.fine("Copy Texture Pbuffer used!");
            useDirectRender = false;
            texture = null;
            initPbuffer();
        } else {
            if ((caps & Pbuffer.RENDER_TEXTURE_SUPPORTED) != 0) {
                logger.fine("Render to Texture Pbuffer supported!");
                if (texture == null) {
                    logger.fine("No RenderTexture used in init, falling back to Copy Texture PBuffer.");
                    useDirectRender = false;
                } else {
                    useDirectRender = true;
                }
            } else {
                logger.fine("Copy Texture Pbuffer supported!");
                texture = null;
            }
        }
    }
}
