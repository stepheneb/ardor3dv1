/**
 * Copyright (c) 2008-2009 Ardor Labs, Inc.
 *
 * This file is part of Ardor3D.
 *
 * Ardor3D is free software: you can redistribute it and/or modify it 
 * under the terms of its license which may be found in the accompanying
 * LICENSE file or at <http://www.ardor3d.com/LICENSE>.
 */

package com.ardor3d.extension.app;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;

import org.aopalliance.intercept.MethodInvocation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ardor3d.example.app.ThreadInterceptor;
import com.google.inject.Provider;

public class TestThreadInterceptor {
    ThreadInterceptor ti;
    MethodInvocation mi;

    Object[] mocks;

    @Before
    public void setup() {
        mi = createMock("MethodInvocation", MethodInvocation.class);

        mocks = new Object[] { mi };
    }

    @After
    public void verifyMocks() throws Exception {
        verify(mocks);
    }

    @Test
    public void testMain() throws Throwable {
        expect(mi.proceed()).andReturn(null);

        replay(mocks);

        ti = new ThreadInterceptor(new Provider<Thread>() {
            public Thread get() {
                return Thread.currentThread();
            }
        });

        ti.invoke(mi);
    }

    @Test(expected = IllegalStateException.class)
    public void testOther() throws Throwable {
        expect(mi.getMethod()).andReturn(null);

        replay(mocks);

        ti = new ThreadInterceptor(new Provider<Thread>() {
            public Thread get() {
                return new Thread();
            }
        });

        ti.invoke(mi);
    }

}
