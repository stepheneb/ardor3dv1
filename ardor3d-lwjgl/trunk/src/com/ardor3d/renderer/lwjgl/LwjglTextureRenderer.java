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

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.lwjgl.opengl.ARBDrawBuffers;
import org.lwjgl.opengl.ARBTextureFloat;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

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
import com.ardor3d.renderer.state.RenderState;
import com.ardor3d.renderer.state.record.TextureRecord;
import com.ardor3d.renderer.state.record.TextureStateRecord;
import com.ardor3d.scene.state.lwjgl.LwjglTextureStateUtil;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;

/**
 * This class is used by Ardor3D's LWJGL implementation to render textures. Users should <b>not </b> create this class
 * directly. Instead, allow DisplaySystem to create it for you.
 * 
 * @see com.ardor3d.system.DisplaySystem#createTextureRenderer
 */
public class LwjglTextureRenderer implements TextureRenderer {
    private static final Logger logger = Logger.getLogger(LwjglTextureRenderer.class.getName());

    private Camera camera;

    private final ColorRGBA backgroundColor = new ColorRGBA(1, 1, 1, 1);

    private int active;

    int fboID = 0, depthRBID = 0, _width = 0, _height = 0;

    private final boolean inited = false;
    private boolean isSupported = true;
    private boolean supportsMultiDraw = false;
    private int maxDrawBuffers = 1;
    private IntBuffer attachBuffer = null;
    private boolean usingDepthRB = false;

    private final LwjglRenderer parentRenderer;

    public LwjglTextureRenderer(final DisplaySettings settings, final LwjglRenderer parentRenderer) {
        this.parentRenderer = parentRenderer;

        if (!inited) {
            isSupported = GLContext.getCapabilities().GL_EXT_framebuffer_object;
            supportsMultiDraw = GLContext.getCapabilities().GL_ARB_draw_buffers;
            if (supportsMultiDraw) {
                final IntBuffer buf = BufferUtils.createIntBuffer(16);
                GL11.glGetInteger(EXTFramebufferObject.GL_MAX_COLOR_ATTACHMENTS_EXT, buf);
                maxDrawBuffers = buf.get(0);
                if (maxDrawBuffers > 1) {
                    attachBuffer = BufferUtils.createIntBuffer(maxDrawBuffers);
                    for (int i = 0; i < maxDrawBuffers; i++) {
                        attachBuffer.put(EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT + i);
                    }

                } else {
                    maxDrawBuffers = 1;
                }
            }
            if (!isSupported) {
                logger.warning("FBO not supported.");
                return;
            } else {
                logger.fine("FBO support detected.");
            }
        }

        int width = settings.getWidth();
        int height = settings.getHeight();
        if (!GLContext.getCapabilities().GL_ARB_texture_non_power_of_two) {
            // Check if we have non-power of two sizes. If so,
            // find the smallest power of two size that is greater than
            // the provided size.
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
        }

        logger.fine("Creating FBO sized: " + width + " x " + height);

        final IntBuffer buffer = BufferUtils.createIntBuffer(1);
        EXTFramebufferObject.glGenFramebuffersEXT(buffer); // generate id
        fboID = buffer.get(0);

        if (fboID <= 0) {
            logger.severe("Invalid FBO id returned! " + fboID);
            isSupported = false;
            return;
        }

        EXTFramebufferObject.glGenRenderbuffersEXT(buffer); // generate id
        depthRBID = buffer.get(0);
        EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, depthRBID);
        EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT,
                GL11.GL_DEPTH_COMPONENT, width, height);

        _width = width;
        _height = height;

        initCamera();
    }

    /**
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

    /**
     * <code>setBackgroundColor</code> sets the OpenGL clear color to the color specified.
     * 
     * @see com.ardor3d.renderer.TextureRenderer#setBackgroundColor(com.ardor3d.renderer.ColorRGBA)
     * @param c
     *            the color to set the background color to.
     */
    public void setBackgroundColor(final ColorRGBA c) {
        backgroundColor.set(c);
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
     * <code>setupTexture</code> initializes a new Texture object for use with TextureRenderer. Generates a valid OpenGL
     * texture id for this texture and initializes the data type for the texture.
     */
    public void setupTexture(final Texture2D tex) {
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
        int format = GL11.GL_RGBA;
        int components = GL11.GL_RGBA8;
        int dataType = GL11.GL_UNSIGNED_BYTE;
        switch (tex.getRTTSource()) {
            case RGBA:
            case RGBA8:
                break;
            case RGB:
            case RGB8:
                format = GL11.GL_RGB;
                components = GL11.GL_RGB8;
                break;
            case Alpha:
            case Alpha8:
                format = GL11.GL_ALPHA;
                components = GL11.GL_ALPHA8;
                break;
            case Depth:
                format = GL11.GL_DEPTH_COMPONENT;
                components = GL11.GL_DEPTH_COMPONENT;
                break;
            case Intensity:
            case Intensity8:
                format = GL11.GL_INTENSITY;
                components = GL11.GL_INTENSITY8;
                break;
            case Luminance:
            case Luminance8:
                format = GL11.GL_LUMINANCE;
                components = GL11.GL_LUMINANCE8;
                break;
            case LuminanceAlpha:
            case Luminance8Alpha8:
                format = GL11.GL_LUMINANCE_ALPHA;
                components = GL11.GL_LUMINANCE8_ALPHA8;
                break;
            case Alpha4:
                format = GL11.GL_ALPHA;
                components = GL11.GL_ALPHA4;
                break;
            case Alpha12:
                format = GL11.GL_ALPHA;
                components = GL11.GL_ALPHA12;
                break;
            case Alpha16:
                format = GL11.GL_ALPHA;
                components = GL11.GL_ALPHA16;
                break;
            case Luminance4:
                format = GL11.GL_LUMINANCE;
                components = GL11.GL_LUMINANCE4;
                break;
            case Luminance12:
                format = GL11.GL_LUMINANCE;
                components = GL11.GL_LUMINANCE12;
                break;
            case Luminance16:
                format = GL11.GL_LUMINANCE;
                components = GL11.GL_LUMINANCE16;
                break;
            case Luminance4Alpha4:
                format = GL11.GL_LUMINANCE_ALPHA;
                components = GL11.GL_LUMINANCE4_ALPHA4;
                break;
            case Luminance6Alpha2:
                format = GL11.GL_LUMINANCE_ALPHA;
                components = GL11.GL_LUMINANCE6_ALPHA2;
                break;
            case Luminance12Alpha4:
                format = GL11.GL_LUMINANCE_ALPHA;
                components = GL11.GL_LUMINANCE12_ALPHA4;
                break;
            case Luminance12Alpha12:
                format = GL11.GL_LUMINANCE_ALPHA;
                components = GL11.GL_LUMINANCE12_ALPHA12;
                break;
            case Luminance16Alpha16:
                format = GL11.GL_LUMINANCE_ALPHA;
                components = GL11.GL_LUMINANCE16_ALPHA16;
                break;
            case Intensity4:
                format = GL11.GL_INTENSITY;
                components = GL11.GL_INTENSITY4;
                break;
            case Intensity12:
                format = GL11.GL_INTENSITY;
                components = GL11.GL_INTENSITY12;
                break;
            case Intensity16:
                format = GL11.GL_INTENSITY;
                components = GL11.GL_INTENSITY4;
                break;
            case R3_G3_B2:
                format = GL11.GL_RGB;
                components = GL11.GL_R3_G3_B2;
                break;
            case RGB4:
                format = GL11.GL_RGB;
                components = GL11.GL_RGB4;
                break;
            case RGB5:
                format = GL11.GL_RGB;
                components = GL11.GL_RGB5;
                break;
            case RGB10:
                format = GL11.GL_RGB;
                components = GL11.GL_RGB10;
                break;
            case RGB12:
                format = GL11.GL_RGB;
                components = GL11.GL_RGB12;
                break;
            case RGB16:
                format = GL11.GL_RGB;
                components = GL11.GL_RGB16;
                break;
            case RGBA2:
                format = GL11.GL_RGBA;
                components = GL11.GL_RGBA2;
                break;
            case RGBA4:
                format = GL11.GL_RGBA;
                components = GL11.GL_RGBA4;
                break;
            case RGB5_A1:
                format = GL11.GL_RGBA;
                components = GL11.GL_RGB5_A1;
                break;
            case RGB10_A2:
                format = GL11.GL_RGBA;
                components = GL11.GL_RGB10_A2;
                break;
            case RGBA12:
                format = GL11.GL_RGBA;
                components = GL11.GL_RGBA12;
                break;
            case RGBA16:
                format = GL11.GL_RGBA;
                components = GL11.GL_RGBA16;
                break;
            case RGBA32F:
                format = GL11.GL_RGBA;
                components = ARBTextureFloat.GL_RGBA32F_ARB;
                dataType = GL11.GL_FLOAT;
                break;
            case RGB32F:
                format = GL11.GL_RGB;
                components = ARBTextureFloat.GL_RGB32F_ARB;
                dataType = GL11.GL_FLOAT;
                break;
            case Alpha32F:
                format = GL11.GL_ALPHA;
                components = ARBTextureFloat.GL_ALPHA32F_ARB;
                dataType = GL11.GL_FLOAT;
                break;
            case Intensity32F:
                format = GL11.GL_INTENSITY;
                components = ARBTextureFloat.GL_INTENSITY32F_ARB;
                dataType = GL11.GL_FLOAT;
                break;
            case Luminance32F:
                format = GL11.GL_LUMINANCE;
                components = ARBTextureFloat.GL_LUMINANCE32F_ARB;
                dataType = GL11.GL_FLOAT;
                break;
            case LuminanceAlpha32F:
                format = GL11.GL_LUMINANCE_ALPHA;
                components = ARBTextureFloat.GL_LUMINANCE_ALPHA32F_ARB;
                dataType = GL11.GL_FLOAT;
                break;
            case RGBA16F:
                format = GL11.GL_RGBA;
                components = ARBTextureFloat.GL_RGBA16F_ARB;
                dataType = GL11.GL_FLOAT;
                break;
            case RGB16F:
                format = GL11.GL_RGB;
                components = ARBTextureFloat.GL_RGB16F_ARB;
                dataType = GL11.GL_FLOAT;
                break;
            case Alpha16F:
                format = GL11.GL_ALPHA;
                components = ARBTextureFloat.GL_ALPHA16F_ARB;
                dataType = GL11.GL_FLOAT;
                break;
            case Intensity16F:
                format = GL11.GL_INTENSITY;
                components = ARBTextureFloat.GL_INTENSITY16F_ARB;
                dataType = GL11.GL_FLOAT;
                break;
            case Luminance16F:
                format = GL11.GL_LUMINANCE;
                components = ARBTextureFloat.GL_LUMINANCE16F_ARB;
                dataType = GL11.GL_FLOAT;
                break;
            case LuminanceAlpha16F:
                format = GL11.GL_LUMINANCE_ALPHA;
                components = ARBTextureFloat.GL_LUMINANCE_ALPHA16F_ARB;
                dataType = GL11.GL_FLOAT;
                break;
        }

        // Initialize our texture with some default data.
        if (dataType == GL11.GL_UNSIGNED_BYTE) {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, components, _width, _height, 0, format, dataType,
                    (ByteBuffer) null);
        } else {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, components, _width, _height, 0, format, dataType,
                    (FloatBuffer) null);
        }

        // Initialize mipmapping for this texture, if requested
        if (tex.getMinificationFilter().usesMipMapLevels()) {
            EXTFramebufferObject.glGenerateMipmapEXT(GL11.GL_TEXTURE_2D);
        }

        // Setup filtering and wrap
        final RenderContext context = ContextManager.getCurrentContext();
        final TextureStateRecord record = (TextureStateRecord) context.getStateRecord(RenderState.StateType.Texture);
        final TextureRecord texRecord = record.getTextureRecord(tex.getTextureId(), tex.getType());

        LwjglTextureStateUtil.applyFilter(tex, texRecord, 0, record, context.getCapabilities());
        LwjglTextureStateUtil.applyWrap(tex, texRecord, 0, record, context.getCapabilities());

        logger.fine("setup fbo tex with id " + tex.getTextureId() + ": " + _width + "," + _height);
    }

    /**
     * <code>render</code> renders a scene. As it recieves a base class of <code>Spatial</code> the renderer hands off
     * management of the scene to spatial for it to determine when a <code>Geometry</code> leaf is reached. The result
     * of the rendering is then copied into the given texture(s). What is copied is based on the Texture object's
     * rttSource field.
     * 
     * @param toDraw
     *            the scene to render.
     * @param tex
     *            the Texture(s) to render it to.
     */
    public void render(final Spatial toDraw, final Texture tex) {
        render(toDraw, tex, true);
    }

    /**
     * <code>render</code> renders a scene. As it recieves a base class of <code>Spatial</code> the renderer hands off
     * management of the scene to spatial for it to determine when a <code>Geometry</code> leaf is reached. The result
     * of the rendering is then copied into the given texture(s). What is copied is based on the Texture object's
     * rttSource field.
     * 
     * @param toDraw
     *            the scene to render.
     * @param tex
     *            the Texture(s) to render it to.
     */
    public void render(final Spatial toDraw, final Texture tex, final boolean doClear) {
        if (!isSupported) {
            return;
        }

        try {
            activate();

            setupForSingleTexDraw(tex, doClear);

            doDraw(toDraw);

            takedownForSingleTexDraw(tex);

            deactivate();
        } catch (final Exception e) {
            logger.logp(Level.SEVERE, this.getClass().toString(), "render(Spatial, Texture, boolean)", "Exception", e);
        }
    }

    public void render(final List<? extends Spatial> toDraw, final List<Texture> texs) {
        render(toDraw, texs, true);
    }

    public void render(final List<? extends Spatial> toDraw, final List<Texture> texs, final boolean doClear) {
        if (!isSupported) {
            return;
        }

        // if we only support 1 draw buffer at a time anyway, we'll have to render to each texture individually...
        if (maxDrawBuffers == 1 || texs.size() == 1) {
            try {
                activate();
                for (int i = 0; i < texs.size(); i++) {
                    final Texture tex = texs.get(i);

                    setupForSingleTexDraw(tex, doClear);

                    doDraw(toDraw);

                    takedownForSingleTexDraw(tex);
                }
            } catch (final Exception e) {
                logger.logp(Level.SEVERE, this.getClass().toString(), "render(Spatial, Texture, boolean)", "Exception",
                        e);
            } finally {
                deactivate();
            }
            return;
        }
        try {
            activate();

            // Otherwise, we can streamline this by rendering to multiple textures at once.
            // first determine how many groups we need
            final LinkedList<Texture> depths = new LinkedList<Texture>();
            final LinkedList<Texture> colors = new LinkedList<Texture>();
            for (int i = 0; i < texs.size(); i++) {
                final Texture tex = texs.get(i);
                if (tex.getRTTSource() == Texture.RenderToTextureType.Depth) {
                    depths.add(tex);
                } else {
                    colors.add(tex);
                }
            }
            // we can only render to 1 depth texture at a time, so # groups is at minimum == numDepth
            final int groups = Math.max(depths.size(), (int) (0.999f + (colors.size() / (float) maxDrawBuffers)));
            for (int i = 0; i < groups; i++) {
                // First handle colors
                int colorsAdded = 0;
                while (colorsAdded < maxDrawBuffers && !colors.isEmpty()) {
                    final Texture tex = colors.removeFirst();
                    EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
                            EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT + colorsAdded, GL11.GL_TEXTURE_2D, tex
                                    .getTextureId(), 0);
                    colorsAdded++;
                }

                // Now take care of depth.
                if (!depths.isEmpty()) {
                    final Texture tex = depths.removeFirst();
                    // Set up our depth texture
                    EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
                            EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, GL11.GL_TEXTURE_2D, tex.getTextureId(), 0);
                    usingDepthRB = false;
                } else if (!usingDepthRB) {
                    // setup our default depth render buffer if not already set
                    EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
                            EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT,
                            depthRBID);
                    usingDepthRB = true;
                }

                setDrawBuffers(colorsAdded);
                setReadBuffer(colorsAdded != 0 ? EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT : GL11.GL_NONE);

                // Check FBO complete
                checkFBOComplete();

                switchCameraIn(doClear);

                doDraw(toDraw);

                switchCameraOut();
            }

            // automatically generate mipmaps for our textures.
            for (int x = 0, max = texs.size(); x < max; x++) {
                if (texs.get(x).getMinificationFilter().usesMipMapLevels()) {
                    LwjglTextureStateUtil.doTextureBind(texs.get(x).getTextureId(), 0, Texture.Type.TwoDimensional);
                    EXTFramebufferObject.glGenerateMipmapEXT(GL11.GL_TEXTURE_2D);
                }
            }

        } catch (final Exception e) {
            logger.logp(Level.SEVERE, this.getClass().toString(), "render(Spatial, Texture)", "Exception", e);
        } finally {
            deactivate();
        }
    }

    private void setupForSingleTexDraw(final Texture tex, final boolean doClear) {

        LwjglTextureStateUtil.doTextureBind(tex.getTextureId(), 0, Texture.Type.TwoDimensional);

        if (tex.getRTTSource() == Texture.RenderToTextureType.Depth) {
            // Setup depth texture into FBO
            EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
                    EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, GL11.GL_TEXTURE_2D, tex.getTextureId(), 0);

            setDrawBuffer(GL11.GL_NONE);
            setReadBuffer(GL11.GL_NONE);
        } else {
            // Set textures into FBO
            EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
                    EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT, GL11.GL_TEXTURE_2D, tex.getTextureId(), 0);

            // setup depth RB
            EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
                    EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, depthRBID);

            setDrawBuffer(EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT);
            setReadBuffer(EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT);
        }

        // Check FBO complete
        checkFBOComplete();

        switchCameraIn(doClear);
    }

    private void setReadBuffer(final int attachVal) {
        GL11.glReadBuffer(attachVal);
    }

    private void setDrawBuffer(final int attachVal) {
        GL11.glDrawBuffer(attachVal);
    }

    private void setDrawBuffers(final int maxEntry) {
        if (maxEntry <= 1) {
            setDrawBuffer(maxEntry != 0 ? EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT : GL11.GL_NONE);
        } else {
            // We should only get to this point if we support ARBDrawBuffers.
            attachBuffer.clear();
            attachBuffer.limit(maxEntry);
            ARBDrawBuffers.glDrawBuffersARB(attachBuffer);
        }
    }

    private void takedownForSingleTexDraw(final Texture tex) {
        switchCameraOut();

        // automatically generate mipmaps for our texture.
        if (tex.getMinificationFilter().usesMipMapLevels()) {
            LwjglTextureStateUtil.doTextureBind(tex.getTextureId(), 0, Texture.Type.TwoDimensional);
            EXTFramebufferObject.glGenerateMipmapEXT(GL11.GL_TEXTURE_2D);
        }
    }

    private void checkFBOComplete() {
        final int framebuffer = EXTFramebufferObject
                .glCheckFramebufferStatusEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT);
        switch (framebuffer) {
            case EXTFramebufferObject.GL_FRAMEBUFFER_COMPLETE_EXT:
                break;
            case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT:
                throw new RuntimeException("FrameBuffer: " + fboID
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT exception");
            case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT:
                throw new RuntimeException("FrameBuffer: " + fboID
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT exception");
            case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT:
                throw new RuntimeException("FrameBuffer: " + fboID
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT exception");
            case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT:
                throw new RuntimeException("FrameBuffer: " + fboID
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT exception");
            case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT:
                throw new RuntimeException("FrameBuffer: " + fboID
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT exception");
            case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT:
                throw new RuntimeException("FrameBuffer: " + fboID
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT exception");
            case EXTFramebufferObject.GL_FRAMEBUFFER_UNSUPPORTED_EXT:
                throw new RuntimeException("FrameBuffer: " + fboID
                        + ", has caused a GL_FRAMEBUFFER_UNSUPPORTED_EXT exception");
            default:
                throw new RuntimeException("Unexpected reply from glCheckFramebufferStatusEXT: " + framebuffer);
        }
    }

    /**
     * <code>copyToTexture</code> copies the FBO contents to the given Texture. What is copied is up to the Texture
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

    private int oldWidth, oldHeight;

    private void switchCameraIn(final boolean doClear) {
        // grab non-rtt settings
        oldWidth = parentRenderer.getWidth();
        oldHeight = parentRenderer.getHeight();

        // swap to rtt settings
        parentRenderer.getQueue().pushBuckets();
        parentRenderer.setSize(_width, _height);

        // clear the scene
        if (doClear) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            parentRenderer.clearBuffers();
        }

        getCamera().update();
        getCamera().apply(parentRenderer);
        ContextManager.getCurrentContext().setCurrentCamera(getCamera());
    }

    private void switchCameraOut() {
        parentRenderer.setSize(oldWidth, oldHeight);

        // back to the non rtt settings
        parentRenderer.getQueue().popBuckets();
    }

    private void doDraw(final Spatial spat) {
        // Override parent's last frustum test to avoid accidental incorrect
        // cull
        if (spat.getParent() != null) {
            spat.getParent().setLastFrustumIntersection(Camera.FrustumIntersect.Intersects);
        }

        // do rtt scene render
        spat.onDraw(parentRenderer);
        parentRenderer.renderQueue();
    }

    private void doDraw(final List<? extends Spatial> toDraw) {
        for (int x = 0, max = toDraw.size(); x < max; x++) {
            final Spatial spat = toDraw.get(x);
            doDraw(spat);
        }
    }

    private void activate() {
        if (!isSupported) {
            return;
        }
        if (active == 0) {
            GL11.glClearColor(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(),
                    backgroundColor.getAlpha());
            EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, fboID);
        }
        active++;
    }

    private void deactivate() {
        if (!isSupported) {
            return;
        }
        if (active == 1) {
            final ColorRGBA bgColor = parentRenderer.getBackgroundColor(ColorRGBA.fetchTempInstance());
            GL11.glClearColor(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), bgColor.getAlpha());
            ColorRGBA.releaseTempInstance(bgColor);
            EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);
        }
        active--;
    }

    private void initCamera() {
        if (!isSupported) {
            return;
        }
        camera = new Camera(_width, _height);
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

        if (fboID > 0) {
            final IntBuffer id = BufferUtils.createIntBuffer(1);
            id.put(fboID);
            id.rewind();
            EXTFramebufferObject.glDeleteFramebuffersEXT(id);
        }
    }

    public int getWidth() {
        return _width;
    }

    public int getHeight() {
        return _height;
    }

    public void setMultipleTargets(final boolean multi) {
    // ignore. Does not matter to FBO.
    }
}
