/**
 * Copyright (C) 2009 Kamil Demecki <kodstark@gmail.com>
 *
 * Licensed under the terms of any of the following licenses at your
 * choice:
 *
 *  - GNU Lesser General Public License Version 2.1 or later (the "LGPL")
 *    http://www.gnu.org/licenses/lgpl.html
 *
 *  - Mozilla Public License Version 1.1 or later (the "MPL")
 *    http://www.mozilla.org/MPL/MPL-1.1.html
 */
package kodstark.osgi.tracker;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import kodstark.osgi.tracker.internal.testsupport.ClassTestedRegister;
import kodstark.osgi.tracker.internal.testsupport.InterfaceTestedRegister;
import kodstark.osgi.tracker.internal.testsupport.InterfaceTestedRegister01;
import kodstark.osgi.tracker.internal.testsupport.InterfaceTestedRegister02;
import kodstark.osgi.tracker.test.BundleContextMock;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TrackerRegisterTest
{
    private TrackerRegister register;
    private BundleContextMock contextMock;

    @Before
    public void setUp() throws IOException, Exception
    {
        contextMock = new BundleContextMock();
        contextMock.setDebug(true);
        register = new TrackerRegister(contextMock.getBundleContext());
    }

    @After
    public void tearDown()
    {
        register.close();
    }

    @Test
    public void shouldGetSpecificService() throws Exception
    {
        contextMock.bindInterfaceWithInstance(InterfaceTestedRegister.class, new ClassTestedRegister(100));
        contextMock.bindInterfaceWithInstance(Object.class, new Object());
        contextMock.replay();
        final InterfaceTestedRegister service = register.getService(InterfaceTestedRegister.class);
        assertEquals(100, service.action());
    }

    @Test(expected = ExceptionNotExistedService.class)
    public void shoutCatchNotExistedService() throws Exception
    {
        contextMock.bindInterfaceWithInstance(InterfaceTestedRegister01.class, new ClassTestedRegister(1));
        contextMock.replay();
        register.getService(InterfaceTestedRegister02.class);
    }

    @Test
    public void shouldOptionalCallGetNull() throws Exception
    {
        contextMock.bindInterfaceWithInstance(InterfaceTestedRegister01.class, new ClassTestedRegister(1));
        contextMock.replay();
        Assert.assertNull(register.getOptionalService(InterfaceTestedRegister02.class));
    }

    @Test
    public void shouldServicesBeEmpty() throws Exception
    {
        contextMock.bindInterfaceWithInstance(InterfaceTestedRegister01.class, new ClassTestedRegister(1));
        contextMock.replay();
        final List<InterfaceTestedRegister02> services = register.getServices(InterfaceTestedRegister02.class);
        assertEquals(0, services.size());
    }
    
    @Test
    public void shouldServicesBeNotEmpty() throws Exception
    {
        contextMock.bindInterfaceWithInstance(InterfaceTestedRegister01.class, new ClassTestedRegister(1));
        contextMock.replay();
        final List<InterfaceTestedRegister01> services = register.getServices(InterfaceTestedRegister01.class);
        assertEquals(1, services.size());
    }    
}
