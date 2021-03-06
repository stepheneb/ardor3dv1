/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.framework.jogl;

import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLException;

import com.ardor3d.annotation.MainThread;
import com.ardor3d.framework.CanvasRenderer;
import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.image.Image;
import com.ardor3d.util.Ardor3dException;
import com.google.inject.Inject;

/**
 * A canvas implementation for use with native JOGL windows.
 */
public class JoglCanvas extends Frame implements NativeCanvas {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(JoglCanvas.class.getName());

    private final JoglCanvasRenderer canvasRenderer;

    private final DisplaySettings settings;
    private boolean inited = false;
    // private Frame frame;
    private boolean isClosing = false;

    private GLCanvas glCanvas;

    // private PhysicalLayer physicalLayer;
    // private final boolean hasFocus = false;

    @Inject
    public JoglCanvas(final JoglCanvasRenderer canvasRenderer, final DisplaySettings settings) {
        this.canvasRenderer = canvasRenderer;
        this.settings = settings;
    }

    @Override
    public void addKeyListener(final KeyListener l) {
        glCanvas.addKeyListener(l);
    }

    @Override
    public void addMouseListener(final MouseListener l) {
        glCanvas.addMouseListener(l);
    }

    @Override
    public void addMouseMotionListener(final MouseMotionListener l) {
        glCanvas.addMouseMotionListener(l);
    }

    @Override
    public void addMouseWheelListener(final MouseWheelListener l) {
        glCanvas.addMouseWheelListener(l);
    }

    @Override
    public void addFocusListener(final FocusListener l) {
        glCanvas.addFocusListener(l); // To change body of overridden methods use File | Settings | File Templates.
    }

    @MainThread
    public void init() {
        privateInit();
    }

    @MainThread
    protected void privateInit() {
        if (inited) {
            return;
        }

        // Validate window dimensions.
        if (settings.getWidth() <= 0 || settings.getHeight() <= 0) {
            throw new Ardor3dException("Invalid resolution values: " + settings.getWidth() + " " + settings.getHeight());
        }

        // Validate bit depth.
        if ((settings.getColorDepth() != 32) && (settings.getColorDepth() != 16) && (settings.getColorDepth() != 24)) {
            throw new Ardor3dException("Invalid pixel depth: " + settings.getColorDepth());
        }

        // Create the OpenGL canvas, and place it within a frame.
        // frame = new Frame();

        // Create the singleton's status.
        final GLCapabilities caps = new GLCapabilities();
        caps.setHardwareAccelerated(true);
        caps.setDoubleBuffered(true);

        // Create the OpenGL canvas,
        glCanvas = new GLCanvas(caps);

        glCanvas.setFocusable(true);
        glCanvas.requestFocus();
        glCanvas.setSize(settings.getWidth(), settings.getHeight());
        glCanvas.setIgnoreRepaint(true);
        glCanvas.setAutoSwapBufferMode(false);

        final GLContext glContext = glCanvas.getContext();
        canvasRenderer.setContext(glContext);

        this.add(glCanvas);
        final boolean isDisplayModeModified;
        final GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        // Get the current display mode
        final DisplayMode previousDisplayMode = gd.getDisplayMode();
        // Handle full screen mode if requested.
        if (settings.isFullScreen()) {
            setUndecorated(true);
            // Check if the full-screen mode is supported by the OS
            boolean isFullScreenSupported = gd.isFullScreenSupported();
            if (isFullScreenSupported) {
                gd.setFullScreenWindow(this);
                // Check if display mode changes are supported by the OS
                if (gd.isDisplayChangeSupported()) {
                    // Get all available display modes
                    final DisplayMode[] displayModes = gd.getDisplayModes();
                    DisplayMode multiBitsDepthSupportedDisplayMode = null;
                    DisplayMode refreshRateUnknownDisplayMode = null;
                    DisplayMode multiBitsDepthSupportedAndRefreshRateUnknownDisplayMode = null;
                    DisplayMode matchingDisplayMode = null;
                    DisplayMode currentDisplayMode;
                    // Look for the display mode that matches with our parameters
                    // Look for some display modes that are close to these parameters
                    // and that could be used as substitutes
                    // On some machines, the refresh rate is unknown and/or multi bit
                    // depths are supported. If you try to force a particular refresh
                    // rate or a bit depth, you might find no available display mode
                    // that matches exactly with your parameters
                    for (int i = 0; i < displayModes.length && matchingDisplayMode == null; i++) {
                        currentDisplayMode = displayModes[i];
                        if (currentDisplayMode.getWidth() == settings.getWidth()
                                && currentDisplayMode.getHeight() == settings.getHeight()) {
                            if (currentDisplayMode.getBitDepth() == settings.getColorDepth()) {
                                if (currentDisplayMode.getRefreshRate() == settings.getFrequency()) {
                                    matchingDisplayMode = currentDisplayMode;
                                } else if (currentDisplayMode.getRefreshRate() == DisplayMode.REFRESH_RATE_UNKNOWN) {
                                    refreshRateUnknownDisplayMode = currentDisplayMode;
                                }
                            } else if (currentDisplayMode.getBitDepth() == DisplayMode.BIT_DEPTH_MULTI) {
                                if (currentDisplayMode.getRefreshRate() == settings.getFrequency()) {
                                    multiBitsDepthSupportedDisplayMode = currentDisplayMode;
                                } else if (currentDisplayMode.getRefreshRate() == DisplayMode.REFRESH_RATE_UNKNOWN) {
                                    multiBitsDepthSupportedAndRefreshRateUnknownDisplayMode = currentDisplayMode;
                                }
                            }
                        }
                    }
                    DisplayMode nextDisplayMode = null;
                    if (matchingDisplayMode != null) {
                        nextDisplayMode = matchingDisplayMode;
                    } else if (multiBitsDepthSupportedDisplayMode != null) {
                        nextDisplayMode = multiBitsDepthSupportedDisplayMode;
                    } else if (refreshRateUnknownDisplayMode != null) {
                        nextDisplayMode = refreshRateUnknownDisplayMode;
                    } else if (multiBitsDepthSupportedAndRefreshRateUnknownDisplayMode != null) {
                        nextDisplayMode = multiBitsDepthSupportedAndRefreshRateUnknownDisplayMode;
                    } else {
                        isFullScreenSupported = false;
                    }
                    // If we have found a display mode that approximatively matches
                    // with the input parameters, use it
                    if (nextDisplayMode != null) {
                        gd.setDisplayMode(nextDisplayMode);
                        isDisplayModeModified = true;
                    } else {
                        isDisplayModeModified = false;
                    }
                } else {
                    isDisplayModeModified = false;
                    // Resize the canvas if the display mode cannot be changed
                    // and the screen size is not equal to the canvas size
                    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    if (screenSize.width != settings.getWidth() || screenSize.height != settings.getHeight()) {
                        glCanvas.setSize(screenSize);
                    }
                }
            } else {
                isDisplayModeModified = false;
            }

            // Software windowed full-screen mode
            if (!isFullScreenSupported) {
                final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                // Resize the canvas
                glCanvas.setSize(screenSize);
                // Resize the frame so that it occupies the whole screen
                this.setSize(screenSize);
                // Set its location at the top left corner
                this.setLocation(0, 0);
            }
        }
        // Otherwise, center the window on the screen.
        else {
            isDisplayModeModified = false;
            pack();

            int x, y;
            x = (Toolkit.getDefaultToolkit().getScreenSize().width - settings.getWidth()) / 2;
            y = (Toolkit.getDefaultToolkit().getScreenSize().height - settings.getHeight()) / 2;
            this.setLocation(x, y);
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                isClosing = true;
                // If required, restore the previous display mode
                if (isDisplayModeModified) {
                    gd.setDisplayMode(previousDisplayMode);
                }
                // If required, get back to the windowed mode
                if (gd.getFullScreenWindow() == JoglCanvas.this) {
                    gd.setFullScreenWindow(null);
                }
            }
        });

        // Make the window visible to realize the OpenGL surface.
        setVisible(true);

        canvasRenderer.init(settings, false);
        inited = true;
    }

    public void draw(final CountDownLatch latch) {
        if (!inited) {
            privateInit();
        }

        checkFocus();

        canvasRenderer.draw();
        latch.countDown();
    }

    private void checkFocus() {
    // // TODO: might be better to just not do anything if there is no physical layer set as focus listener - possibly,
    // // people might not
    // // want to do any input at all.
    // if (physicalLayer == null) {
    // throw new IllegalStateException("no physical layer set as focus listener");
    // }
    //
    // final boolean newFocus = Display.isActive() && Display.isVisible();
    //
    // if (!hasFocus && newFocus) {
    // // didn't use to have focus, but now we do
    // // do nothing for now, just keep track of the fact that we have focus
    // hasFocus = newFocus;
    // } else if (hasFocus && !newFocus) {
    // // had focus, but don't anymore - notify the physical input layer
    // physicalLayer.lostFocus();
    // hasFocus = newFocus;
    // }
    }

    public CanvasRenderer getCanvasRenderer() {
        return canvasRenderer;
    }

    public void close() {
        try {
            if (GLContext.getCurrent() != null) {
                // Release the OpenGL resources.
                GLContext.getCurrent().release();
            }
        } catch (final GLException releaseFailure) {
            logger.log(Level.WARNING, "Failed to release OpenGL Context: " + glCanvas, releaseFailure);
        }

        // Dispose of any window resources.
        dispose();
    }

    @Override
    public boolean isActive() {
        return hasFocus();
    }

    public boolean isClosing() {
        return isClosing;
    }

    public void moveWindowTo(final int locX, final int locY) {
        setLocation(locX, locY);
    }

    public void setIcon(final Image[] iconImages) {
    // TODO Auto-generated method stub
    }

    public void setVSyncEnabled(final boolean enabled) {
        if (GLContext.getCurrent() != null) {
            // Release the OpenGL resources.
            GLContext.getCurrent().getGL().setSwapInterval(enabled ? 1 : 0);
        }
    }

    public void cleanup() {
        canvasRenderer.cleanup();
    }

    // public void forward(final JoglCanvas canvas, final PhysicalLayer physicalLayer) {
    // this.physicalLayer = physicalLayer;
    // }
}
