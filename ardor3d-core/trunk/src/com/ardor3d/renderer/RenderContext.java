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

import java.util.EnumMap;

import com.ardor3d.renderer.state.RenderState;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.renderer.state.record.LineRecord;
import com.ardor3d.renderer.state.record.RendererRecord;
import com.ardor3d.renderer.state.record.StateRecord;

/**
 * Represents the state of an individual context in OpenGL.
 */
public class RenderContext {

    /** List of states that override any set states on a spatial if not null. */
    protected final EnumMap<RenderState.StateType, RenderState> enforcedStates = new EnumMap<RenderState.StateType, RenderState>(
            RenderState.StateType.class);

    /** RenderStates a Spatial contains during rendering. */
    protected final EnumMap<RenderState.StateType, RenderState> currentStates = new EnumMap<RenderState.StateType, RenderState>(
            RenderState.StateType.class);

    protected final EnumMap<RenderState.StateType, StateRecord> stateRecords = new EnumMap<RenderState.StateType, StateRecord>(
            RenderState.StateType.class);

    protected final LineRecord lineRecord = new LineRecord();
    protected final RendererRecord rendererRecord = new RendererRecord();

    protected final ContextCapabilities capabilities;

    protected Object contextHolder = null;

    protected Camera currentCamera = null;

    public RenderContext(final Object key, final ContextCapabilities caps) {
        contextHolder = key;
        capabilities = caps;
        setupRecords();
    }

    protected void setupRecords() {
        for (final RenderState.StateType type : RenderState.StateType.values()) {
            stateRecords.put(type, RenderState.createState(type).createStateRecord());
        }
    }

    public void invalidateStates() {
        for (final RenderState.StateType type : RenderState.StateType.values()) {
            stateRecords.get(type).invalidate();
        }
        lineRecord.invalidate();
        rendererRecord.invalidate();

        clearCurrentStates();
    }

    public ContextCapabilities getCapabilities() {
        return capabilities;
    }

    public StateRecord getStateRecord(final RenderState.StateType type) {
        return stateRecords.get(type);
    }

    public LineRecord getLineRecord() {
        return lineRecord;
    }

    public RendererRecord getRendererRecord() {
        return rendererRecord;
    }

    /**
     * Enforce a particular state. In other words, the given state will override any state of the same type set on a
     * scene object. Remember to clear the state when done enforcing. Very useful for multipass techniques where
     * multiple sets of states need to be applied to a scenegraph drawn multiple times.
     * 
     * @param state
     *            state to enforce
     */
    public void enforceState(final RenderState state) {
        enforcedStates.put(state.getType(), state);
    }

    /**
     * Clears an enforced render state index by setting it to null. This allows object specific states to be used.
     * 
     * @param type
     *            The type of RenderState to clear enforcement on.
     */
    public void clearEnforcedState(final RenderState.StateType type) {
        enforcedStates.remove(type);
    }

    /**
     * sets all enforced states to null.
     */
    public void clearEnforcedStates() {
        enforcedStates.clear();
    }

    /**
     * sets all current states to null, and therefore forces the use of the default states.
     */
    public void clearCurrentStates() {
        currentStates.clear();
    }

    /**
     * @param type
     *            the state type to clear.
     */
    public void clearCurrentState(final RenderState.StateType type) {
        currentStates.remove(type);
    }

    public RenderState getEnforcedState(final RenderState.StateType type) {
        return enforcedStates.get(type);
    }

    public RenderState getCurrentState(final RenderState.StateType type) {
        return currentStates.get(type);
    }

    public Object getContextHolder() {
        return contextHolder;
    }

    public void setContextHolder(final Object contextHolder) {
        this.contextHolder = contextHolder;
    }

    public void setCurrentState(final StateType type, final RenderState state) {
        currentStates.put(type, state);
    }

    public EnumMap<StateType, RenderState> getEnforcedStates() {
        return enforcedStates;
    }

    public Camera getCurrentCamera() {
        return currentCamera;
    }

    public void setCurrentCamera(final Camera cam) {
        currentCamera = cam;
    }
}
