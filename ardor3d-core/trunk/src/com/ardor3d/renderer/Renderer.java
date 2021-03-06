/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.renderer;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.EnumMap;
import java.util.List;

import com.ardor3d.image.Image;
import com.ardor3d.image.Texture;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Transform;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.renderer.queue.RenderQueue;
import com.ardor3d.renderer.state.RenderState;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.scenegraph.Renderable;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.TexCoords;
import com.ardor3d.scenegraph.VBOInfo;
import com.ardor3d.ui.text.BasicText;
import com.ardor3d.util.Ardor3dException;

/**
 * <code>Renderer</code> defines an abstract class that handles displaying of graphics data to the context. Creation of
 * this object is typically handled via a call to a <code>DisplaySystem</code> subclass.
 * 
 * All rendering state and tasks can be handled through this class.
 * 
 * @see com.ardor3d.system.DisplaySystem
 */
public abstract class Renderer {

    // clear color
    protected final ColorRGBA backgroundColor = new ColorRGBA(ColorRGBA.BLACK);

    protected boolean processingQueue;

    protected RenderQueue queue;

    private boolean headless = false;

    // width and height of renderer
    protected int width;

    protected int height;

    /**
     * <code>setBackgroundColor</code> sets the color of window. This color will be shown for any pixel that is not set
     * via typical rendering operations.
     * 
     * @param c
     *            the color to set the background to.
     */
    public abstract void setBackgroundColor(ReadOnlyColorRGBA c);

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
     * <code>clearZBuffer</code> clears the depth buffer of the renderer. The Z buffer allows sorting of pixels by depth
     * or distance from the view port. Clearing this buffer prepares it for the next frame.
     * 
     */
    public abstract void clearZBuffer();

    /**
     * <code>clearBackBuffer</code> clears the back buffer of the renderer. The backbuffer is the buffer being rendered
     * to before it is displayed to the screen. Clearing this buffer frees it for rendering the next frame.
     * 
     */
    public abstract void clearColorBuffer();

    /**
     * <code>clearStencilBuffer</code> clears the stencil buffer of the renderer.
     */
    public abstract void clearStencilBuffer();

    /**
     * <code>clearBuffers</code> clears both the depth buffer and the back buffer.
     * 
     */
    public abstract void clearBuffers();

    /**
     * <code>clearStrictBuffers</code> clears both the depth buffer and the back buffer restricting the clear to the
     * rectangle defined by the width and height of the renderer.
     * 
     */
    public abstract void clearStrictBuffers();

    /**
     * <code>displayBackBuffer</code> swaps the back buffer with the currently displayed buffer. Swapping (page
     * flipping) allows the renderer to display a prerenderer display without any flickering.
     * 
     */
    public abstract void displayBackBuffer();

    /**
     * 
     * <code>setOrtho</code> sets the display system to be in orthographic mode. If the system has already been set to
     * orthographic mode a <code>Ardor3dException</code> is thrown. The origin (0,0) is the bottom left of the screen.
     * 
     */
    public abstract void setOrtho();

    /**
     * @return true if the renderer is currently in ortho mode.
     */
    public abstract boolean isInOrthoMode();

    /**
     * render queue if needed
     */
    public void renderQueue() {
        processingQueue = true;
        queue.renderBuckets();
        processingQueue = false;
    }

    /**
     * clear the render queue
     */
    public void clearQueue() {
        queue.clearBuckets();
    }

    /**
     * 
     * <code>setOrthoCenter</code> sets the display system to be in orthographic mode. If the system has already been
     * set to orthographic mode a <code>Ardor3dException</code> is thrown. The origin (0,0) is the center of the screen.
     * 
     * 
     */
    public abstract void setOrthoCenter();

    /**
     * 
     * <code>unsetOrhto</code> unsets the display system from orthographic mode back into regular projection mode. If
     * the system is not in orthographic mode a <code>Ardor3dException</code> is thrown.
     * 
     * 
     */
    public abstract void unsetOrtho();

    /**
     * 
     * <code>takeScreenShot</code> saves the current buffer to a png file. The filename is provided, .png will be
     * appended to the end of the name.
     * 
     * @param filename
     *            the name of the screenshot file.
     * @return true if the screen capture was successful, false otherwise.
     */
    public abstract boolean takeScreenShot(String filename);

    /**
     * <code>grabScreenContents</code> reads a block of data as bytes from the current framebuffer. The format
     * determines how many bytes per pixel are read and thus how big the buffer must be that you pass in.
     * 
     * @param buff
     *            a buffer to store contents in.
     * @param format
     *            the format to read in bytes for.
     * @param x
     *            - x starting point of block
     * @param y
     *            - y starting point of block
     * @param w
     *            - width of block
     * @param h
     *            - height of block
     */
    public abstract void grabScreenContents(ByteBuffer buff, Image.Format format, int x, int y, int w, int h);

    /**
     * <code>draw</code> renders a scene. As it recieves a base class of <code>Spatial</code> the renderer hands off
     * management of the scene to spatial for it to determine when a <code>Geometry</code> leaf is reached.
     * 
     * @param s
     *            the scene to render.
     */
    public abstract void draw(Spatial s);

    /**
     * 
     * <code>draw</code> renders text to the back buffer.
     * 
     * @param t
     *            the text object to be rendered.
     */
    public abstract void draw(BasicText t);

    /**
     * <code>flush</code> tells opengl to send through all currently waiting commands in the buffer.
     */
    public abstract void flush();

    /**
     * <code>finish</code> is similar to flush, however it blocks until all waiting OpenGL commands have been finished.
     */
    public abstract void finish();

    /**
     * Get the render queue associated with this Renderer.
     * 
     * @return RenderQueue
     */
    public RenderQueue getQueue() {
        return queue;
    }

    /**
     * Return true if this renderer is in the middle of processing its RenderQueue.
     * 
     * @return boolean
     */
    public boolean isProcessingQueue() {
        return processingQueue;
    }

    /**
     * Check a given Spatial to see if it should be queued. return true if it was queued.
     * 
     * @param s
     *            Spatial to check
     * @return true if it was queued.
     */
    public abstract boolean checkAndAdd(Spatial s);

    /**
     * See Renderer.isHeadless()
     * 
     * @return boolean
     */
    public boolean isHeadless() {
        return headless;
    }

    /**
     * See Renderer.setHeadless()
     */
    public void setHeadless(final boolean headless) {
        this.headless = headless;
    }

    /**
     * Retrieve the width set on this renderer.
     * 
     * @return width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Retrieve the height set on this renderer.
     * 
     * @return height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Reinitialize the renderer with the given width/height. Also calls resize on the attached camera if present.
     * 
     * @param width
     *            int
     * @param height
     *            int
     */
    public abstract void setSize(int width, int height);

    /**
     * Sets an offset to the zbuffer to be used when comparing an incoming polygon for depth buffer pass/fail.
     * 
     * @param factor
     *            Specifies a scale factor that is used to create a variable depth offset for each polygon. The initial
     *            value is 0.
     * @param offset
     *            Is multiplied by an implementation-specific value to create a constant depth offset. The initial value
     *            is 0.
     */
    public abstract void setPolygonOffset(float factor, float offset);

    /**
     * Removes any previously set offset from the renderer.
     */
    public abstract void clearPolygonOffset();

    /**
     * Checks the VBO cache to see if this Buffer is mapped to a VBO-id. If it does the mapping will be removed from the
     * cache and the VBO with the VBO-id found will be deleted.
     * 
     * If no mapped VBO-id is found, this method does not do anything else.
     * 
     * @param buffer
     *            The Buffer who's associated VBO should be deleted.
     */
    public abstract void deleteVBO(Buffer buffer);

    /**
     * Attempts to delete the VBO with this VBO id. Ignores ids < 1.
     * 
     * @param vboid
     */
    public abstract void deleteVBO(int vboid);

    /**
     * Clears all entries from the VBO cache. Does not actually delete any VBO buffer, only all mappings between Buffers
     * and VBO-ids.
     * 
     */
    public abstract void clearVBOCache();

    /**
     * Removes the mapping between this Buffer and it's VBO-id. Does not actually delete the VBO. <br>
     * This method is usefull if you want to use the same Buffer to create several VBOs. After a VBO is created for this
     * Buffer, update the Buffer and remove it from the VBO cache. You can now reuse the same buffer with another Mesh
     * object. <br>
     * If no association is found, this method does nothing.
     * 
     * @param buffer
     *            The nio Buffer whose associated VBO should be deleted.
     * @return An int wrapped in an Integer object that's the VBO-id of the VBO previously mapped to this Buffer, or
     *         null is no mapping existed.
     */
    public abstract Integer removeFromVBOCache(Buffer buffer);

    /**
     * Updates a region of the content area of the provided texture using the specified region of the given data.
     * 
     * @param dstTexture
     *            the texture to be updated
     * @param dstX
     *            the x offset relative to the lower-left corner of this texture where the update will be applied
     * @param dstY
     *            the y offset relative to the lower-left corner of this texture where the update will be applied
     * @param srcImage
     *            the image data to be uploaded to the texture
     * @param srcX
     *            the x offset relative to the lower-left corner of the supplied buffer from which to fetch the update
     *            rectangle
     * @param srcY
     *            the y offset relative to the lower-left corner of the supplied buffer from which to fetch the update
     *            rectangle
     * @param width
     *            the width of the region to be updated
     * @param height
     *            the height of the region to be updated
     * @throws Ardor3dException
     *             if unable to update the texture
     * @throws UnsupportedOperationException
     *             if updating for the provided texture type is unsupported
     * @see com.sun.opengl.util.texture.Texture#updateSubImage(com.sun.opengl.util.texture.TextureData, int, int, int,
     *      int, int, int, int)
     * @since 2.0
     */
    public abstract void updateTextureSubImage(Texture dstTexture, int dstX, int dstY, Image srcImage, int srcX,
            int srcY, int width, int height) throws Ardor3dException, UnsupportedOperationException;

    /**
     * Check the underlying rendering system (opengl, etc.) for exceptions.
     * 
     * @throws Ardor3dException
     *             if an error is found.
     */
    public abstract void checkCardError() throws Ardor3dException;

    /**
     * Perform any necessary cleanup operations such as deleting VBOs, etc.
     */
    public abstract void cleanup();

    /**
     * <code>draw</code> renders the renderable to the back buffer.
     * 
     * @param renderable
     *            the text object to be rendered.
     */
    public abstract void draw(final Renderable renderable);

    /**
     * <code>applyStates</code> applies the sent in renderstates.
     * 
     * @param states
     *            states to apply.
     */
    public abstract void applyStates(final EnumMap<StateType, RenderState> states);

    /**
     * <code>doTransforms</code> sets the current transform.
     * 
     * @param transform
     *            transform to apply.
     */
    public abstract boolean doTransforms(final Transform transform);

    /**
     * <code>undoTransforms</code> reverts the latest transform.
     * 
     * @param transform
     *            transform to revert.
     */
    public abstract void undoTransforms(final Transform transform);

    /**
     * <code>setupVertexData</code> sets up the buffers for vertex data.
     * 
     * @param vertices
     *            transform to apply.
     */
    public abstract void setupVertexData(final FloatBuffer vertexBuffer, final VBOInfo vbo);

    public abstract void setupNormalData(final FloatBuffer normalBuffer, final NormalsMode normalMode,
            final Transform worldTransform, final VBOInfo vbo);

    public abstract void setupColorData(final FloatBuffer colorBuffer, final VBOInfo vbo, final ColorRGBA defaultColor);

    public abstract void setupFogData(final FloatBuffer fogBuffer, final VBOInfo vbo);

    public abstract void setupTextureData(final List<TexCoords> textureCoords, final VBOInfo vbo);

    public abstract void setupInterleavedData(final FloatBuffer interleavedBuffer, InterleavedFormat format,
            final VBOInfo vbo);

    public abstract void drawElements(final IntBuffer indices, final VBOInfo vbo, final int[] indexLengths,
            final IndexMode[] indexModes);

    public abstract void drawArrays(final FloatBuffer vertexBuffer, final int[] indexLengths,
            final IndexMode[] indexModes);

    public abstract void renderDisplayList(final int displayListID);

    public abstract void setProjectionMatrix(DoubleBuffer matrix);

    public abstract void setModelViewMatrix(DoubleBuffer matrix);

    public abstract void setViewport(int x, int y, int width, int height);

    /**
     * This is a workaround until we make shared abstract Record classes, or open up lower level opengl calls abstracted
     * from lwjgl/jogl.
     * 
     * @param lineWidth
     * @param stippleFactor
     * @param stipplePattern
     * @param antialiased
     */
    public abstract void setupLineParameters(float lineWidth, int stippleFactor, short stipplePattern,
            boolean antialiased);

    /**
     * This is a workaround until we make shared abstract Record classes, or open up lower level opengl calls abstracted
     * from lwjgl/jogl.
     * 
     * @param pointSize
     * @param antialiased
     */
    public abstract void setupPointParameters(float pointSize, boolean antialiased);

    /**
     * Apply the given state to the current RenderContext using this Renderer.
     * 
     * @param state
     */
    public abstract void applyState(RenderState state);

    public abstract void loadTexture(Texture texture, int unit);

    public abstract void deleteTextureId(int textureId);
}
