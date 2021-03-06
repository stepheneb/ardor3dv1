/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.math;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.util.Debug;
import com.ardor3d.util.export.Ardor3DExporter;
import com.ardor3d.util.export.Ardor3DImporter;
import com.ardor3d.util.export.InputCapsule;
import com.ardor3d.util.export.OutputCapsule;
import com.ardor3d.util.export.Savable;
import com.ardor3d.util.pool.ObjectPool;

/**
 * ColorRGBA is a 4 component color value (red, green, blue, alpha). The standard range for each individual component is
 * [0f, 1f].
 */
public class ColorRGBA implements Cloneable, Savable, Externalizable, ReadOnlyColorRGBA {

    private static final long serialVersionUID = 1L;

    private static final ColorRGBAPool COLOR_POOL = new ColorRGBAPool(11);

    /**
     * the color black (0, 0, 0, 1).
     */
    public static final ReadOnlyColorRGBA BLACK = new ColorRGBA(0f, 0f, 0f, 1f);
    /**
     * the color black (0, 0, 0, 0).
     */
    public static final ReadOnlyColorRGBA BLACK_NO_ALPHA = new ColorRGBA(0f, 0f, 0f, 1f);
    /**
     * the color white (1, 1, 1, 1).
     */
    public static final ReadOnlyColorRGBA WHITE = new ColorRGBA(1f, 1f, 1f, 1f);
    /**
     * the color gray (.2f, .2f, .2f, 1).
     */
    public static final ReadOnlyColorRGBA DARK_GRAY = new ColorRGBA(0.2f, 0.2f, 0.2f, 1.0f);
    /**
     * the color gray (.5f, .5f, .5f, 1).
     */
    public static final ReadOnlyColorRGBA GRAY = new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f);
    /**
     * the color gray (.8f, .8f, .8f, 1).
     */
    public static final ReadOnlyColorRGBA LIGHT_GRAY = new ColorRGBA(0.8f, 0.8f, 0.8f, 1.0f);
    /**
     * the color red (1, 0, 0, 1).
     */
    public static final ReadOnlyColorRGBA RED = new ColorRGBA(1f, 0f, 0f, 1f);
    /**
     * the color green (0, 1, 0, 1).
     */
    public static final ReadOnlyColorRGBA GREEN = new ColorRGBA(0f, 1f, 0f, 1f);
    /**
     * the color blue (0, 0, 1, 1).
     */
    public static final ReadOnlyColorRGBA BLUE = new ColorRGBA(0f, 0f, 1f, 1f);
    /**
     * the color yellow (1, 1, 0, 1).
     */
    public static final ReadOnlyColorRGBA YELLOW = new ColorRGBA(1f, 1f, 0f, 1f);
    /**
     * the color magenta (1, 0, 1, 1).
     */
    public static final ReadOnlyColorRGBA MAGENTA = new ColorRGBA(1f, 0f, 1f, 1f);
    /**
     * the color cyan (0, 1, 1, 1).
     */
    public static final ReadOnlyColorRGBA CYAN = new ColorRGBA(0f, 1f, 1f, 1f);
    /**
     * the color orange (251/255f, 130/255f, 0, 1).
     */
    public static final ReadOnlyColorRGBA ORANGE = new ColorRGBA(251f / 255f, 130f / 255f, 0f, 1f);
    /**
     * the color brown (65/255f, 40/255f, 25/255f, 1).
     */
    public static final ReadOnlyColorRGBA BROWN = new ColorRGBA(65f / 255f, 40f / 255f, 25f / 255f, 1f);
    /**
     * the color pink (1, 0.68f, 0.68f, 1).
     */
    public static final ReadOnlyColorRGBA PINK = new ColorRGBA(1f, 0.68f, 0.68f, 1f);

    protected float _r = 0;
    protected float _g = 0;
    protected float _b = 0;
    protected float _a = 0;

    /**
     * Constructs a new, mutable color set to (1, 1, 1, 1).
     */
    public ColorRGBA() {
        this(1, 1, 1, 1);
    }

    /**
     * Constructs a new, mutable color set to the (r, g, b, a) values of the provided source color.
     * 
     * @param src
     */
    public ColorRGBA(final ReadOnlyColorRGBA src) {
        this(src.getRed(), src.getGreen(), src.getBlue(), src.getAlpha());
    }

    /**
     * Constructs a new color set to (r, g, b, a).
     * 
     * @param r
     * @param g
     * @param b
     * @param a
     */
    public ColorRGBA(final float r, final float g, final float b, final float a) {
        _r = r;
        _g = g;
        _b = b;
        _a = a;
    }

    public float getRed() {
        return _r;
    }

    public float getGreen() {
        return _g;
    }

    public float getBlue() {
        return _b;
    }

    public float getAlpha() {
        return _a;
    }

    /**
     * @param index
     * @return r value if index == 0, g value if index == 1, b value if index == 2 or a value if index == 3
     * @throws IllegalArgumentException
     *             if index is not one of 0, 1, 2, 3.
     */
    public float getValue(final int index) {
        switch (index) {
            case 0:
                return getRed();
            case 1:
                return getGreen();
            case 2:
                return getBlue();
            case 3:
                return getAlpha();
        }
        throw new IllegalArgumentException("index must be either 0, 1, 2 or 3");
    }

    /**
     * @param index
     *            which field index in this color to set.
     * @param value
     *            to set to one of r, g, b or a.
     * @throws IllegalArgumentException
     *             if index is not one of 0, 1, 2, 3.
     */
    public void setValue(final int index, final float value) {
        switch (index) {
            case 0:
                setRed(value);
                return;
            case 1:
                setGreen(value);
                return;
            case 2:
                setBlue(value);
                return;
            case 3:
                setAlpha(value);
                return;
        }
        throw new IllegalArgumentException("index must be either 0, 1, 2 or 3");
    }

    /**
     * Stores the float values of this color in the given float array.
     * 
     * @param store
     *            if null, a new float[4] array is created.
     * @return the float array
     * @throws NullPointerException
     *             if store is null.
     * @throws ArrayIndexOutOfBoundsException
     *             if store is not at least length 4.
     */
    public float[] toArray(float[] store) {
        if (store == null) {
            store = new float[4];
        }
        // do last first to ensure size is correct before any edits occur.
        store[3] = getAlpha();
        store[2] = getBlue();
        store[1] = getGreen();
        store[0] = getRed();
        return store;
    }

    /**
     * Sets the red component of this color to the given float value.
     * 
     * @param r
     *            new red value, generally should be in the range [0.0f, 1.0f]
     */
    public void setRed(final float r) {
        _r = r;
    }

    /**
     * Sets the green component of this color to the given float value.
     * 
     * @param g
     *            new green value, generally should be in the range [0.0f, 1.0f]
     */
    public void setGreen(final float g) {
        _g = g;
    }

    /**
     * Sets the blue component of this color to the given float value.
     * 
     * @param b
     *            new blue value, generally should be in the range [0.0f, 1.0f]
     */
    public void setBlue(final float b) {
        _b = b;
    }

    /**
     * Sets the alpha component of this color to the given float value. Consider that an alpha of 1.0f means opaque (can
     * not see through) and 0.0f means transparent.
     * 
     * @param a
     *            new alpha value, generally should be in the range [0.0f, 1.0f]
     */
    public void setAlpha(final float a) {
        _a = a;
    }

    /**
     * Sets the value of this color to (r, g, b, a)
     * 
     * @param r
     *            new red value, generally should be in the range [0.0f, 1.0f]
     * @param g
     *            new green value, generally should be in the range [0.0f, 1.0f]
     * @param b
     *            new blue value, generally should be in the range [0.0f, 1.0f]
     * @param a
     *            new alpha value, generally should be in the range [0.0f, 1.0f]
     * @return this color for chaining
     */
    public ColorRGBA set(final float r, final float g, final float b, final float a) {
        setRed(r);
        setGreen(g);
        setBlue(b);
        setAlpha(a);
        return this;
    }

    /**
     * Sets the value of this color to the (r, g, b, a) values of the provided source color.
     * 
     * @param source
     * @return this color for chaining
     * @throws NullPointerException
     *             if source is null.
     */
    public ColorRGBA set(final ReadOnlyColorRGBA source) {
        _r = source.getRed();
        _g = source.getGreen();
        _b = source.getBlue();
        _a = source.getAlpha();
        return this;
    }

    /**
     * Sets the value of this color to (0, 0, 0, 0)
     * 
     * @return this color for chaining
     */
    public ColorRGBA zero() {
        return set(0, 0, 0, 0);
    }

    /**
     * Brings all values (r,g,b,a) into the range [0.0f, 1.0f]. If a value is above or below this range it is replaced
     * with the appropriate end of the range.
     * 
     * @param store
     *            the color to store the result in for return. If null, a new color object is created and returned.
     */
    public ColorRGBA clamp(final ColorRGBA store) {
        ColorRGBA result = store;
        if (result == null) {
            result = new ColorRGBA();
        }

        result.set(this);

        if (Float.compare(result._r, 0f) < 0) {
            result._r = 0.0f;
        } else if (Float.compare(result._r, 1f) > 0) {
            result._r = 1.0f;
        }

        if (Float.compare(result._g, 0f) < 0) {
            result._g = 0.0f;
        } else if (Float.compare(result._g, 1f) > 0) {
            result._g = 1.0f;
        }

        if (Float.compare(result._b, 0f) < 0) {
            result._b = 0.0f;
        } else if (Float.compare(result._b, 1f) > 0) {
            result._b = 1.0f;
        }

        if (Float.compare(result._a, 0f) < 0) {
            result._a = 0.0f;
        } else if (Float.compare(result._a, 1f) > 0) {
            result._a = 1.0f;
        }
        return result;
    }

    /**
     * Brings all values (r,g,b,a) into the range [0.0f, 1.0f]. If a value is above or below this range it is replaced
     * with the appropriate end of the range.
     */
    public void clampLocal() {
        if (Float.compare(_r, 0f) < 0) {
            _r = 0.0f;
        } else if (Float.compare(_r, 1f) > 0) {
            _r = 1.0f;
        }

        if (Float.compare(_g, 0f) < 0) {
            _g = 0.0f;
        } else if (Float.compare(_g, 1f) > 0) {
            _g = 1.0f;
        }

        if (Float.compare(_b, 0f) < 0) {
            _b = 0.0f;
        } else if (Float.compare(_b, 1f) > 0) {
            _b = 1.0f;
        }

        if (Float.compare(_a, 0f) < 0) {
            _a = 0.0f;
        } else if (Float.compare(_a, 1f) > 0) {
            _a = 1.0f;
        }
    }

    /**
     * @param store
     *            the color to store the result in for return. If null, a new color object is created and returned.
     * @return a random, mutable opaque color.
     */
    public static ColorRGBA randomColor(final ColorRGBA store) {
        ColorRGBA result = store;
        if (result == null) {
            result = new ColorRGBA();
        }

        result._r = MathUtils.nextRandomFloat();
        result._g = MathUtils.nextRandomFloat();
        result._b = MathUtils.nextRandomFloat();
        result._a = 1.0f;
        return result;
    }

    /**
     * @return this color, stored as an integer by converting the values to the range [0, 255] and combining them as
     *         single byte values into a 4 byte int in the order ARGB. Note that this method expects color values in the
     *         [0.0f, 1.0f] range.
     */
    public int asIntARGB() {
        final int argb = (((int) (_a * 255) & 0xFF) << 24) | (((int) (_r * 255) & 0xFF) << 16)
                | (((int) (_g * 255) & 0xFF) << 8) | (((int) (_b * 255) & 0xFF));
        return argb;
    }

    /**
     * @return this color, stored as an integer by converting the values to the range [0, 255] and combining them as
     *         single byte values into a 4 byte int in the order RGBA. Note that this method expects color values in the
     *         [0.0f, 1.0f] range.
     */
    public int asIntRGBA() {
        final int rgba = (((int) (_r * 255) & 0xFF) << 24) | (((int) (_g * 255) & 0xFF) << 16)
                | (((int) (_b * 255) & 0xFF) << 8) | (((int) (_a * 255) & 0xFF));
        return rgba;
    }

    /**
     * Reads a color, packed into a 4 byte int as 1 byte values in the order ARGB. These byte values are normalized to
     * the range [0.0f, 1.0f]
     * 
     * @param color
     */
    public void fromIntARGB(final int color) {
        _a = ((byte) (color >> 24) & 0xFF) / 255f;
        _r = ((byte) (color >> 16) & 0xFF) / 255f;
        _g = ((byte) (color >> 8) & 0xFF) / 255f;
        _b = ((byte) (color) & 0xFF) / 255f;
    }

    /**
     * Reads a color, packed into a 4 byte int as 1 byte values in the order RGBA. These byte values are normalized to
     * the range [0.0f, 1.0f]
     * 
     * @param color
     */
    public void fromIntRGBA(final int color) {
        _r = ((byte) (color >> 24) & 0xFF) / 255f;
        _g = ((byte) (color >> 16) & 0xFF) / 255f;
        _b = ((byte) (color >> 8) & 0xFF) / 255f;
        _a = ((byte) (color) & 0xFF) / 255f;
    }

    /**
     * Adds the given values to those of this color and returns them in store.
     * 
     * @param r
     * @param g
     * @param b
     * @param a
     * @param store
     *            the color to store the result in for return. If null, a new color object is created and returned.
     * @return (this.r + r, this.g + g, this.b + b, this.a + a)
     */
    public ColorRGBA add(final float r, final float g, final float b, final float a, final ColorRGBA store) {
        ColorRGBA result = store;
        if (result == null) {
            result = new ColorRGBA();
        }

        return result.set(getRed() + r, getGreen() + g, getBlue() + b, getAlpha() + a);
    }

    /**
     * Increments the values of this color with the given r, g, b and a values.
     * 
     * @param r
     * @param g
     * @param b
     * @param a
     * @return this color for chaining
     */
    public ColorRGBA addLocal(final float r, final float g, final float b, final float a) {
        return set(getRed() + r, getGreen() + g, getBlue() + b, getAlpha() + a);
    }

    /**
     * Adds the values of the given source color to those of this color and returns them in store.
     * 
     * @param source
     * @param store
     *            the color to store the result in for return. If null, a new color object is created and returned.
     * @return (this.r + source.r, this.g + source.g, this.b + source.b, this.a + source.a)
     * @throws NullPointerException
     *             if source is null.
     */
    public ColorRGBA add(final ReadOnlyColorRGBA source, final ColorRGBA store) {
        return add(source.getRed(), source.getGreen(), source.getBlue(), source.getAlpha(), store);
    }

    /**
     * Increments the values of this color with the r, g, b and a values of the given color.
     * 
     * @param source
     * @return this color for chaining
     * @throws NullPointerException
     *             if source is null.
     */
    public ColorRGBA addLocal(final ReadOnlyColorRGBA source) {
        return addLocal(source.getRed(), source.getGreen(), source.getBlue(), source.getAlpha());
    }

    /**
     * Subtracts the given values from those of this color and returns them in store.
     * 
     * @param r
     * @param g
     * @param b
     * @param a
     * @param store
     *            the color to store the result in for return. If null, a new color object is created and returned.
     * @return (this.r - r, this.g - g, this.b - b, this.a - a)
     */
    public ColorRGBA subtract(final float r, final float g, final float b, final float a, final ColorRGBA store) {
        ColorRGBA result = store;
        if (result == null) {
            result = new ColorRGBA();
        }

        return result.set(getRed() - r, getGreen() - g, getBlue() - b, getAlpha() - a);
    }

    /**
     * Decrements the values of this color by the given r, g, b and a values.
     * 
     * @param r
     * @param g
     * @param b
     * @param a
     * @return this color for chaining
     */
    public ColorRGBA subtractLocal(final float r, final float g, final float b, final float a) {
        return set(getRed() - r, getGreen() - g, getBlue() - b, getAlpha() - a);
    }

    /**
     * Subtracts the values of the given source color from those of this color and returns them in store.
     * 
     * @param source
     * @param store
     *            the color to store the result in for return. If null, a new color object is created and returned.
     * @return (this.r - source.r, this.g - source.g, this.b - source.b, this.a - source.a)
     * @throws NullPointerException
     *             if source is null.
     */
    public ColorRGBA subtract(final ReadOnlyColorRGBA source, final ColorRGBA store) {
        return subtract(source.getRed(), source.getGreen(), source.getBlue(), source.getAlpha(), store);
    }

    /**
     * Decrements the values of this color by the r, g, b and a values from the given source color.
     * 
     * @param source
     * @return this color for chaining
     * @throws NullPointerException
     *             if source is null.
     */
    public ColorRGBA subtractLocal(final ReadOnlyColorRGBA source) {
        return subtractLocal(source.getRed(), source.getGreen(), source.getBlue(), source.getAlpha());
    }

    /**
     * Multiplies the values of this color by the given scalar value and returns the result in store.
     * 
     * @param scalar
     * @param store
     *            the color to store the result in for return. If null, a new color object is created and returned.
     * @return a new color (this.r * scalar, this.g * scalar, this.b * scalar, this.a * scalar)
     */
    public ColorRGBA multiply(final float scalar, final ColorRGBA store) {
        ColorRGBA result = store;
        if (result == null) {
            result = new ColorRGBA();
        }

        return result.set(getRed() * scalar, getGreen() * scalar, getBlue() * scalar, getAlpha() * scalar);
    }

    /**
     * Internally modifies the values of this color by multiplying them each by the given scalar value.
     * 
     * @param scalar
     * @return this color for chaining
     * 
     *         .
     */
    public ColorRGBA multiplyLocal(final float scalar) {
        return set(getRed() * scalar, getGreen() * scalar, getBlue() * scalar, getAlpha() * scalar);
    }

    /**
     * Multiplies the values of this color by the given scalar value and returns the result in store.
     * 
     * @param scale
     * @param store
     *            the color to store the result in for return. If null, a new color object is created and returned.
     * @return a new color (this.r * scale.r, this.g * scale.g, this.b * scale.b, this.a * scale.a)
     */
    public ColorRGBA multiply(final ReadOnlyColorRGBA scale, final ColorRGBA store) {
        ColorRGBA result = store;
        if (result == null) {
            result = new ColorRGBA();
        }

        return result.set(getRed() * scale.getRed(), getGreen() * scale.getGreen(), getBlue() * scale.getBlue(),
                getAlpha() * scale.getAlpha());
    }

    /**
     * Internally modifies the values of this color by multiplying them each by the given scale values.
     * 
     * @param scale
     * @return this color for chaining
     */
    public ColorRGBA multiplyLocal(final ReadOnlyColorRGBA scale) {
        return set(getRed() * scale.getRed(), getGreen() * scale.getGreen(), getBlue() * scale.getBlue(), getAlpha()
                * scale.getAlpha());
    }

    /**
     * Divides the values of this color by the given scalar value and returns the result in store.
     * 
     * @param scalar
     * @param store
     *            the color to store the result in for return. If null, a new color object is created and returned.
     * @return a new color (this.r / scalar, this.g / scalar, this.b / scalar, this.a / scalar)
     */
    public ColorRGBA divide(final float scalar, final ColorRGBA store) {
        ColorRGBA result = store;
        if (result == null) {
            result = new ColorRGBA();
        }

        return result.set(getRed() / scalar, getGreen() / scalar, getBlue() / scalar, getAlpha() / scalar);
    }

    /**
     * Internally modifies the values of this color by dividing them each by the given scalar value.
     * 
     * @param scalar
     * @return this color for chaining
     * @throws ArithmeticException
     *             if scalar is 0
     */
    public ColorRGBA divideLocal(final float scalar) {
        final float invScalar = 1.0f / scalar;

        return set(getRed() * invScalar, getGreen() * invScalar, getBlue() * invScalar, getAlpha() * invScalar);
    }

    /**
     * Divides the values of this color by the given scale values and returns the result in store.
     * 
     * @param scale
     * @param store
     *            the color to store the result in for return. If null, a new color object is created and returned.
     * @return a new color (this.r / scale.r, this.g / scale.g, this.b / scale.b, this.a / scale.a)
     */
    public ColorRGBA divide(final ReadOnlyColorRGBA scale, final ColorRGBA store) {
        ColorRGBA result = store;
        if (result == null) {
            result = new ColorRGBA();
        }

        return result.set(getRed() / scale.getRed(), getGreen() / scale.getGreen(), getBlue() / scale.getBlue(),
                getAlpha() / scale.getAlpha());
    }

    /**
     * Internally modifies the values of this color by dividing them each by the given scale values.
     * 
     * @param scale
     * @return this color for chaining
     */
    public ColorRGBA divideLocal(final ReadOnlyColorRGBA scale) {
        return set(getRed() / scale.getRed(), getGreen() / scale.getGreen(), getBlue() / scale.getBlue(), getAlpha()
                / scale.getAlpha());
    }

    /**
     * Performs a linear interpolation between this color and the given end color, using the given scalar as a percent.
     * iow, if changeAmnt is closer to 0, the result will be closer to the current value of this color and if it is
     * closer to 1, the result will be closer to the end value.
     * 
     * @param endColor
     * @param scalar
     * @param store
     *            the color to store the result in for return. If null, a new color object is created and returned.
     * @return a new mutable color as described above.
     * @throws NullPointerException
     *             if endVec is null.
     */
    public ColorRGBA lerp(final ReadOnlyColorRGBA endColor, final float scalar, final ColorRGBA store) {
        ColorRGBA result = store;
        if (result == null) {
            result = new ColorRGBA();
        }

        final float r = (1.0f - scalar) * getRed() + scalar * endColor.getRed();
        final float g = (1.0f - scalar) * getGreen() + scalar * endColor.getGreen();
        final float b = (1.0f - scalar) * getBlue() + scalar * endColor.getBlue();
        final float a = (1.0f - scalar) * getAlpha() + scalar * endColor.getAlpha();
        return result.set(r, g, b, a);
    }

    /**
     * Performs a linear interpolation between this color and the given end color, using the given scalar as a percent.
     * iow, if changeAmnt is closer to 0, the result will be closer to the current value of this color and if it is
     * closer to 1, the result will be closer to the end value. The result is stored back in this color.
     * 
     * @param endColor
     * @param scalar
     * @return this color for chaining
     * @throws NullPointerException
     *             if endVec is null.
     */
    public ColorRGBA lerpLocal(final ReadOnlyColorRGBA endColor, final float scalar) {
        setRed((1.0f - scalar) * getRed() + scalar * endColor.getRed());
        setGreen((1.0f - scalar) * getGreen() + scalar * endColor.getGreen());
        setBlue((1.0f - scalar) * getBlue() + scalar * endColor.getBlue());
        setAlpha((1.0f - scalar) * getAlpha() + scalar * endColor.getAlpha());
        return this;
    }

    /**
     * Performs a linear interpolation between the given begin and end colors, using the given scalar as a percent. iow,
     * if changeAmnt is closer to 0, the result will be closer to the begin value and if it is closer to 1, the result
     * will be closer to the end value.
     * 
     * @param beginColor
     * @param endColor
     * @param scalar
     *            the scalar as a percent.
     * @param store
     *            the color to store the result in for return. If null, a new color object is created and returned.
     * @return a new mutable color as described above.
     * @throws NullPointerException
     *             if beginVec or endVec are null.
     */
    public static ColorRGBA lerp(final ReadOnlyColorRGBA beginColor, final ReadOnlyColorRGBA endColor,
            final float scalar, final ColorRGBA store) {
        ColorRGBA result = store;
        if (result == null) {
            result = new ColorRGBA();
        }

        final float r = (1.0f - scalar) * beginColor.getRed() + scalar * endColor.getRed();
        final float g = (1.0f - scalar) * beginColor.getGreen() + scalar * endColor.getGreen();
        final float b = (1.0f - scalar) * beginColor.getBlue() + scalar * endColor.getBlue();
        final float a = (1.0f - scalar) * beginColor.getAlpha() + scalar * endColor.getAlpha();
        return result.set(r, g, b, a);
    }

    /**
     * Performs a linear interpolation between the given begin and end colors, using the given scalar as a percent. iow,
     * if changeAmnt is closer to 0, the result will be closer to the begin value and if it is closer to 1, the result
     * will be closer to the end value. The result is stored back in this color.
     * 
     * @param beginColor
     * @param endColor
     * @param changeAmnt
     *            the scalar as a percent.
     * @return this color for chaining
     * @throws NullPointerException
     *             if beginVec or endVec are null.
     */
    public ColorRGBA lerpLocal(final ReadOnlyColorRGBA beginColor, final ReadOnlyColorRGBA endColor, final float scalar) {
        setRed((1.0f - scalar) * beginColor.getRed() + scalar * endColor.getRed());
        setGreen((1.0f - scalar) * beginColor.getGreen() + scalar * endColor.getGreen());
        setBlue((1.0f - scalar) * beginColor.getBlue() + scalar * endColor.getBlue());
        setAlpha((1.0f - scalar) * beginColor.getAlpha() + scalar * endColor.getAlpha());
        return this;
    }

    /**
     * Check a color... if it is null or its values are NaN or infinite, return false. Else return true.
     * 
     * @param color
     *            the color to check
     * @return true or false as stated above.
     */
    public static boolean isValid(final ReadOnlyColorRGBA color) {
        if (color == null) {
            return false;
        }
        if (Float.isNaN(color.getRed()) || Float.isNaN(color.getGreen()) || Float.isNaN(color.getBlue())
                || Float.isNaN(color.getAlpha())) {
            return false;
        }
        if (Float.isInfinite(color.getRed()) || Float.isInfinite(color.getGreen()) || Float.isInfinite(color.getBlue())
                || Float.isInfinite(color.getAlpha())) {
            return false;
        }
        return true;
    }

    /**
     * @return the string representation of this color.
     */
    @Override
    public String toString() {
        return "com.ardor3d.math.ColorRGBA [R=" + getRed() + ", G=" + getGreen() + ", B=" + getBlue() + ", A="
                + getAlpha() + "]";
    }

    /**
     * @return returns a unique code for this color object based on its values. If two colors are numerically equal,
     *         they will return the same hash code value.
     */
    @Override
    public int hashCode() {
        int result = 17;

        final int r = Float.floatToIntBits(getRed());
        result += 31 * result + r;

        final int g = Float.floatToIntBits(getGreen());
        result += 31 * result + g;

        final int b = Float.floatToIntBits(getBlue());
        result += 31 * result + b;

        final int a = Float.floatToIntBits(getAlpha());
        result += 31 * result + a;

        return result;
    }

    /**
     * @param o
     *            the object to compare for equality
     * @return true if this color and the provided color have the same r, g, b and a values.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReadOnlyColorRGBA)) {
            return false;
        }
        final ReadOnlyColorRGBA comp = (ReadOnlyColorRGBA) o;
        if (Float.compare(getRed(), comp.getRed()) == 0 && Float.compare(getGreen(), comp.getGreen()) == 0
                && Float.compare(getBlue(), comp.getBlue()) == 0 && Float.compare(getAlpha(), comp.getAlpha()) == 0) {
            return true;
        }
        return false;
    }

    // /////////////////
    // Method for Cloneable
    // /////////////////

    @Override
    public ColorRGBA clone() {
        try {
            return (ColorRGBA) super.clone();
        } catch (final CloneNotSupportedException e) {
            throw new AssertionError(); // can not happen
        }
    }

    // /////////////////
    // Methods for Savable
    // /////////////////

    public Class<? extends ColorRGBA> getClassTag() {
        return this.getClass();
    }

    public void write(final Ardor3DExporter e) throws IOException {
        final OutputCapsule capsule = e.getCapsule(this);
        capsule.write(getRed(), "r", 1);
        capsule.write(getGreen(), "g", 1);
        capsule.write(getBlue(), "b", 1);
        capsule.write(getAlpha(), "a", 1);
    }

    public void read(final Ardor3DImporter e) throws IOException {
        final InputCapsule capsule = e.getCapsule(this);
        setRed(capsule.readFloat("r", 1));
        setGreen(capsule.readFloat("g", 1));
        setBlue(capsule.readFloat("b", 1));
        setAlpha(capsule.readFloat("a", 1));
    }

    // /////////////////
    // Methods for Externalizable
    // /////////////////

    /**
     * Used with serialization. Not to be called manually.
     * 
     * @param in
     *            ObjectInput
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        setRed(in.readFloat());
        setGreen(in.readFloat());
        setBlue(in.readFloat());
        setAlpha(in.readFloat());
    }

    /**
     * Used with serialization. Not to be called manually.
     * 
     * @param out
     *            ObjectOutput
     * @throws IOException
     */
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeFloat(getRed());
        out.writeFloat(getGreen());
        out.writeFloat(getBlue());
        out.writeFloat(getAlpha());
    }

    // /////////////////
    // Methods for creating temp variables (pooling)
    // /////////////////

    /**
     * @return An instance of ColorRGBA that is intended for temporary use in calculations and so forth. Multiple calls
     *         to the method should return instances of this class that are not currently in use.
     */
    public final static ColorRGBA fetchTempInstance() {
        if (Debug.useMathPools) {
            return COLOR_POOL.fetch();
        } else {
            return new ColorRGBA();
        }
    }

    /**
     * Releases a ColorRGBA back to be used by a future call to fetchTempInstance. TAKE CARE: this ColorRGBA object
     * should no longer have other classes referencing it or "Bad Things" will happen.
     * 
     * @param color
     *            the ColorRGBA to release.
     */
    public final static void releaseTempInstance(final ColorRGBA color) {
        if (Debug.useMathPools) {
            COLOR_POOL.release(color);
        }
    }

    static final class ColorRGBAPool extends ObjectPool<ColorRGBA> {
        public ColorRGBAPool(final int initialSize) {
            super(initialSize);
        }

        @Override
        protected ColorRGBA newInstance() {
            return new ColorRGBA();
        }
    }
}
