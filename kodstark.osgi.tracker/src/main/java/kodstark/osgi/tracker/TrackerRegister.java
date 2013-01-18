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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import kodstark.osgi.tracker.internal.LazyServiceTracker;

import org.osgi.framework.BundleContext;

/**
 * Register for getting services on demand.
 * <p>
 * Normally instance is created inside
 * {@link org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)}. Method {@link #close()} is called
 * in {@link org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)} because all opened osgi trackers
 * needs be closed.
 * <p>
 * It is alternative approach to using osgi tracker with white board pattern.
 * 
 * @author <a href="mailto:kodstark@gmail.com">Kamil Demecki</a>
 * @since 14 Sep, 2009
 */
public class TrackerRegister
{
    LazyServiceTracker.Factory trackerFactory;
    private final ConcurrentMap<Class<?>, LazyServiceTracker<?>> trackers;
    private final BundleContext bundleContext;

    public TrackerRegister(BundleContext context)
    {
        this.bundleContext = context;
        trackerFactory = new LazyServiceTracker.Factory();
        trackers = new ConcurrentHashMap<Class<?>, LazyServiceTracker<?>>();
    }

    /**
     * Get service from bundle context according to full class name or throw exception
     * {@link ExceptionNotExistedService} when it doesn't exist.
     */
    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> clazz)
    {
        LazyServiceTracker<T> tracker = (LazyServiceTracker<T>) trackers.get(clazz);
        if (tracker == null)
        {
            tracker = trackerFactory.createLazyServiceTracker(bundleContext, clazz);
            LazyServiceTracker<?> putResult0 = trackers.putIfAbsent(clazz, tracker);
            LazyServiceTracker<T> putResult = (LazyServiceTracker<T>) putResult0;
            if (putResult != null)
            {
                tracker = putResult; // replace on failed race condition
            }
        }
        tracker.openOnlyFirstTime();
        final T result = tracker.getService();
        if (result == null)
        {
            throw new ExceptionNotExistedService(tracker.getClazz());
        }
        return result;
    }

    /**
     * Get service from bundle context according to full class name or return null when it doesn't exist.
     */
    @SuppressWarnings("unchecked")
    public <T> T getOptionalService(Class<T> clazz)
    {
        LazyServiceTracker<T> tracker = (LazyServiceTracker<T>) trackers.get(clazz);
        if (tracker == null)
        {
            tracker = trackerFactory.createLazyServiceTracker(bundleContext, clazz);
            LazyServiceTracker<?> putResult0 = trackers.putIfAbsent(clazz, tracker);
            LazyServiceTracker<T> putResult = (LazyServiceTracker<T>) putResult0;
            if (putResult != null)
            {
                tracker = putResult; // replace on failed race condition
            }
        }
        tracker.openOnlyFirstTime();
        return tracker.getService();
    }

    /**
     * Get services from bundle context according to full class name and always return list.
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getServices(Class<T> clazz)
    {
        LazyServiceTracker<T> tracker = (LazyServiceTracker<T>) trackers.get(clazz);
        if (tracker == null)
        {
            tracker = trackerFactory.createLazyServiceTracker(bundleContext, clazz);
            LazyServiceTracker<?> putResult0 = trackers.putIfAbsent(clazz, tracker);
            LazyServiceTracker<T> putResult = (LazyServiceTracker<T>) putResult0;
            if (putResult != null)
            {
                tracker = putResult; // replace on failed race condition
            }
        }
        tracker.openOnlyFirstTime();
        return tracker.getServices();
    }

    /**
     * Close register. Normally invoked during closing activator.
     */
    public void close()
    {
        Collection<LazyServiceTracker<?>> trackersValues = trackers.values();
        for (LazyServiceTracker<?> tracker : trackersValues)
        {
            tracker.close();
        }
    }
}
