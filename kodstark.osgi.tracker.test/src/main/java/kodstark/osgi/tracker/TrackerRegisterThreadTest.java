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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import kodstark.osgi.tracker.internal.LazyServiceTracker;
import kodstark.osgi.tracker.internal.testsupport.ClassTestedRegister;
import kodstark.osgi.tracker.internal.testsupport.InterfaceTestedRegister01;
import kodstark.osgi.tracker.internal.testsupport.InterfaceTestedRegister02;
import kodstark.osgi.tracker.internal.testsupport.InterfaceTestedRegister03;
import kodstark.osgi.tracker.internal.testsupport.InterfaceTestedRegister04;
import kodstark.osgi.tracker.internal.testsupport.InterfaceTestedRegister05;
import kodstark.osgi.tracker.test.BundleContextMock;

import org.junit.Test;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.osgi.framework.BundleContext;

public class TrackerRegisterThreadTest
{
    private static final int MIN_SERVICE_NUMBER = 1;
    private static final int MAX_SERVICE_NUMBER = 5;
    private static final int MAX_THREADS = 300;
    private TrackerRegister register;
    private BundleContextMock contextMock;
    private List<Thread> threads = new ArrayList<Thread>();
    private List<Throwable> exceptions = new ArrayList<Throwable>();
    private List<LazyServiceTracker<?>> createdTrackers;

    @Test
    public void shouldGetServiceInConcurentWithBundleContextMock() throws Exception
    {
        contextMock = new BundleContextMock().setDebug(true);
        register = new TrackerRegister(contextMock.getBundleContext());
        contextMock.bindInterfaceWithInstance(InterfaceTestedRegister01.class, new ClassTestedRegister(1));
        contextMock.bindInterfaceWithInstance(InterfaceTestedRegister02.class, new ClassTestedRegister(2));
        contextMock.bindInterfaceWithInstance(InterfaceTestedRegister03.class, new ClassTestedRegister(3));
        contextMock.bindInterfaceWithInstance(InterfaceTestedRegister04.class, new ClassTestedRegister(4));
        contextMock.bindInterfaceWithInstance(InterfaceTestedRegister05.class, new ClassTestedRegister(5));
        contextMock.replay();
        startThreadsAndWaitThemToFinish();
        assertNotExceptionsOccured();
        register.close();
    }

    @Test
    public void shouldVerifyOpenedAndClosedTrackersWithMockedLazyServiceTracker() throws Exception
    {
        register = new TrackerRegister(null);
        addReplayMocksDuringCreatingLazyServiceTracker();
        startThreadsAndWaitThemToFinish();
        register.close();
        assertNotExceptionsOccured();
        assertCreatedTrackersGreatEqThanFive();
        assertOnlyFiveTrackersAreUsed();
        assertClosedTrackersOnlyIfOpened();
        assertUsedTrackersAreClosedOnce();
        assertNeverUsedTrackersAreNeverClosed();  
    }

    private void addReplayMocksDuringCreatingLazyServiceTracker()
    {
        createdTrackers = new CopyOnWriteArrayList<LazyServiceTracker<?>>();
        register.trackerFactory = new LazyServiceTracker.Factory()
        {
            @Override
            public <T> LazyServiceTracker<T> createLazyServiceTracker(BundleContext context, Class<T> clazz)
            {
                return createTrackerMock(clazz);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private <T> LazyServiceTracker<T> createTrackerMock(Class<T> clazz)
    {
        LazyServiceTracker<T> result = mock(LazyServiceTracker.class);
        createdTrackers.add(result);
        replayGetServiceForTrackerMock(result, clazz);
        return result;
    }

    @SuppressWarnings("rawtypes")
    private <T> void replayGetServiceForTrackerMock(LazyServiceTracker trackerMock, Class<T> clazz)
    {
        if (clazz == InterfaceTestedRegister01.class)
        {
            when(trackerMock.getService()).thenReturn(new ClassTestedRegister(1));
        }
        else if (clazz == InterfaceTestedRegister02.class)
        {
            when(trackerMock.getService()).thenReturn(new ClassTestedRegister(2));
        }
        else if (clazz == InterfaceTestedRegister03.class)
        {
            when(trackerMock.getService()).thenReturn(new ClassTestedRegister(3));
        }
        else if (clazz == InterfaceTestedRegister04.class)
        {
            when(trackerMock.getService()).thenReturn(new ClassTestedRegister(4));
        }
        else if (clazz == InterfaceTestedRegister05.class)
        {
            when(trackerMock.getService()).thenReturn(new ClassTestedRegister(5));
        }
        else
        {
            throw new RuntimeException("Invalid class " + clazz);
        }
    }

    private void startThreadsAndWaitThemToFinish() throws InterruptedException
    {
        int cycledServiceNumber = MIN_SERVICE_NUMBER;
        for (int i = 0; i < MAX_THREADS; i++)
        {
            Thread thread = new Thread(createThreadRunnable(cycledServiceNumber));
            thread.start();
            threads.add(thread);
            cycledServiceNumber = getNextCycledNumber(cycledServiceNumber, MIN_SERVICE_NUMBER, MAX_SERVICE_NUMBER);
        }
        for (Thread thread : threads)
        {
            thread.join();
        }
    }

    private Runnable createThreadRunnable(final int serviceNumber)
    {
        return new Runnable()
        {
            @Override
            public void run()
            {
                assertGetService(serviceNumber);
            }
        };
    }

    private void assertGetService(int serviceNumber)
    {
        try
        {
            tryAssertGetService(serviceNumber);
        }
        catch (RuntimeException ex)
        {
            exceptions.add(ex);
        }
        catch (AssertionError ex)
        {
            exceptions.add(ex);
        }
    }

    private void tryAssertGetService(int serviceNumber)
    {
        if (serviceNumber == 1)
        {
            assertEquals(1, register.getService(InterfaceTestedRegister01.class).action());
        }
        else if (serviceNumber == 2)
        {
            assertEquals(2, register.getService(InterfaceTestedRegister02.class).action());
        }
        else if (serviceNumber == 3)
        {
            assertEquals(3, register.getService(InterfaceTestedRegister03.class).action());
        }
        else if (serviceNumber == 4)
        {
            assertEquals(4, register.getService(InterfaceTestedRegister04.class).action());
        }
        else if (serviceNumber == 5)
        {
            assertEquals(5, register.getService(InterfaceTestedRegister05.class).action());
        }
        else
        {
            throw new RuntimeException("Invalid fServiceNumber " + serviceNumber);
        }
    }

    private int getNextCycledNumber(int cycledNumber, int min, int max)
    {
        int result = cycledNumber + 1;
        if (result > max)
        {
            result = min;
        }
        return result;
    }

    private void assertNotExceptionsOccured()
    {
        for (Throwable ex : exceptions)
        {
            ex.printStackTrace();
        }
        assertTrue("There were exceptions in threads", exceptions.isEmpty());
    }

    private void assertClosedTrackersOnlyIfOpened()
    {
        for (LazyServiceTracker<?> tracker : createdTrackers)
        {
            try
            {
                verify(tracker).close();
            }
            catch (MockitoAssertionError ex)
            {
                verify(tracker, never()).openOnlyFirstTime();
                verify(tracker, never()).close();
            }
        }
    }

    private void assertCreatedTrackersGreatEqThanFive()
    {
        assertTrue(createdTrackers.size() >= 5);
    }

    private void assertOnlyFiveTrackersAreUsed()
    {
        List<LazyServiceTracker<?>> usedTrackers = new ArrayList<LazyServiceTracker<?>>();
        for (LazyServiceTracker<?> tracker : createdTrackers)
        {
            try
            {
                verify(tracker, atLeastOnce()).getService();
                usedTrackers.add(tracker);
            }
            catch (MockitoAssertionError ex)
            {
            }
        }
        assertEquals(5, usedTrackers.size());
    }

    private void assertNeverUsedTrackersAreNeverClosed()
    {
        for (LazyServiceTracker<?> tracker : createdTrackers)
        {
            try
            {
                verify(tracker, atLeastOnce()).getService();
            }
            catch (MockitoAssertionError ex)
            {
                verify(tracker, never()).close();
            }
        }
    }

    private void assertUsedTrackersAreClosedOnce()
    {
        for (LazyServiceTracker<?> tracker : createdTrackers)
        {
            try
            {
                verify(tracker, never()).getService();
            }
            catch (MockitoAssertionError ex)
            {
                verify(tracker).close();
            }
        }
    }
}
