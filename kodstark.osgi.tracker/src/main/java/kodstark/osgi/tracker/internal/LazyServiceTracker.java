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
package kodstark.osgi.tracker.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * Tracker with open method without synchronization.
 * 
 * @param <T>
 *            type of tracking service.
 * 
 * @author <a href="mailto:kodstark@gmail.com">Kamil Demecki</a>
 * @since 14 Sep, 2009
 */
@SuppressWarnings("unchecked")
public class LazyServiceTracker<T>
{
    @SuppressWarnings("rawtypes")
    private final ServiceTracker tracker;
    private volatile boolean isNotOpen = true;
    private final String clazz;

    @SuppressWarnings("rawtypes")
    LazyServiceTracker(final BundleContext context, final String clazz, final ServiceTrackerCustomizer customizer)
    {
        tracker = new ServiceTracker(context, clazz, customizer);
        this.clazz = clazz;
    }

    public void openOnlyFirstTime()
    {
        if (isNotOpen)
        {
            synchronized (this)
            {
                if (isNotOpen)
                {
                    tracker.open();
                    isNotOpen = false;
                }
            }
        }
    }

    public T getService()
    {
        return (T) tracker.getService();
    }

    public List<T> getServices()
    {
        Object[] result = tracker.getServices();
        if (result != null)
        {
            return (List<T>) Arrays.asList(result);
        }
        else
        {
            return Collections.emptyList();
        }
    }

    public void close()
    {
        tracker.close();
    }

    public String getClazz()
    {
        return clazz;
    }

    public static class Factory
    {
        public <T> LazyServiceTracker<T> createLazyServiceTracker(BundleContext context, Class<T> clazz)
        {
            return new LazyServiceTracker<T>(context, clazz.getName(), null);
        }
    }
}
