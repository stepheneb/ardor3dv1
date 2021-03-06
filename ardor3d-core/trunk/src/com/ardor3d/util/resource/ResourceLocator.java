/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.util.resource;

import java.net.URL;

/**
 * Interface for locating resources from resource names.
 */
public interface ResourceLocator {

    /**
     * Locates a resource according to the strategy of the reousrce locator implementation (subclass).
     * 
     * @see SimpleResourceLocator
     * @see MultiFormatResourceLocator
     * @param resourceName
     *            the name of the resource to locate; it this is a path it must be slash separated (no backslashes)
     * @return a URL for the resource, null if the resource was not found
     */
    public URL locateResource(String resourceName);

}
