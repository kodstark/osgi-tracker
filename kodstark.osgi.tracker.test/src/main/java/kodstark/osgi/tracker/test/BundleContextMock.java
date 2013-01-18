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
package kodstark.osgi.tracker.test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * Util for mocking osgi services in bundle context. In this class you can register what services will be returned when
 * querying bundle context.
 * 
 * @author <a href="mailto:kodstark@gmail.com">Kamil Demecki</a>
 * @since 14 Sep, 2009
 */
@SuppressWarnings("rawtypes")
public class BundleContextMock
{
    private final Map<String, Object> mockServices;
    private final BundleContext mock;
    private List<ServiceReference> listSerRefs;
    /**
     * Debug flag. Because of classic chicken-egg problem we can't use logging service from osgi context to mock osgi
     * context.
     */
    private boolean debug;

    public BundleContextMock()
    {
        mock = mock(BundleContext.class);
        mockServices = new LinkedHashMap<String, Object>();
    }

    public BundleContext getBundleContext()
    {
        return mock;
    }

    public BundleContextMock setDebug(boolean debug)
    {
        this.debug = debug;
        return this;
    }

    /**
     * Register service object under service name - it will be returned by mocked osgi context
     */
    public void bindNameWithInstance(String name, Object service)
    {
        mockServices.put(name, service);
    }
    
    /**
     * Register service object under service name - it will be returned by mocked osgi context
     */
    public void bindInterfaceWithInstance(Class<?> clazz, Object service)
    {
        bindNameWithInstance(clazz.getName(), service);
    }    

    /**
     * Replay bundle context mock with registered services
     */
    public void replay() throws InvalidSyntaxException
    {
        createRefsOfAddedServices();
        replayMockAnswers();
    }

    private void replayMockAnswers() throws InvalidSyntaxException
    {
        replayGetService();
        replayGetServiceReference();
        replayGetServiceReferences();
        replayGetAllServiceReferences();
    }

    @SuppressWarnings("unchecked")
    private void replayGetService()
    {
        when(mock.getService(any(ServiceReference.class))).thenAnswer(new Answer<Object>()
        {
            public Object answer(InvocationOnMock invocation)
            {
                Object[] args = invocation.getArguments();
                if (debug)
                {
                    System.out.println("[BundleContextMock] Call getService with " + Arrays.toString(args));
                }
                ServiceReferenceStubImpl serviceRef = (ServiceReferenceStubImpl) args[0];
                return mockServices.get(serviceRef.getName());
            }
        });
    }

    private void replayGetServiceReference()
    {
        when(mock.getServiceReference(any(String.class))).thenAnswer(new Answer<ServiceReference>()
        {
            public ServiceReference answer(InvocationOnMock invocation)
            {
                Object[] args = invocation.getArguments();
                if (debug)
                {
                    System.out.println("[BundleContextMock] Call getServiceReference with " + Arrays.toString(args));
                }
                return new ServiceReferenceStubImpl((String) args[0]);
            }
        });
    }

    private void replayGetServiceReferences() throws InvalidSyntaxException
    {
        when(mock.getServiceReferences(any(String.class), any(String.class))).thenAnswer(new Answer<ServiceReference[]>()
        {
            public ServiceReference[] answer(InvocationOnMock invocation)
            {
                Object[] args = invocation.getArguments();
                if (debug)
                {
                    System.out.println("[BundleContextMock] Call getServiceReferences with " + Arrays.toString(args));
                }
                String serviceName = (String) args[0];
                return new ServiceReference[] { new ServiceReferenceStubImpl(serviceName) };
            }
        });
    }

    private void replayGetAllServiceReferences() throws InvalidSyntaxException
    {
        when(mock.getAllServiceReferences(any(String.class), any(String.class))).thenAnswer(
                new Answer<ServiceReference[]>()
                {
                    public ServiceReference[] answer(InvocationOnMock invocation)
                    {
                        Object[] args = invocation.getArguments();
                        if (debug)
                        {
                            System.out.println("[BundleContextMock] Call getAllServiceReferences with " + Arrays.toString(args));
                        }
                        return listSerRefs.toArray(new ServiceReference[0]);
                    }
                });
    }

    private void createRefsOfAddedServices()
    {
        listSerRefs = new ArrayList<ServiceReference>();
        for (String serviceName : mockServices.keySet())
        {
            listSerRefs.add(new ServiceReferenceStubImpl(serviceName));
        }
    }

    private static class ServiceReferenceStubImpl implements ServiceReference
    {
        private final String name;

        public ServiceReferenceStubImpl(final String name)
        {
            this.name = name;
        }

        @Override
        public Object getProperty(final String key)
        {
            if (Constants.SERVICE_ID.equals(key))
            {
                return 0L;
            }
            return null;
        }

        @Override
        public String[] getPropertyKeys()
        {
            return null;
        }

        @Override
        public Bundle getBundle()
        {
            return null;
        }

        @Override
        public Bundle[] getUsingBundles()
        {
            return null;
        }

        @Override
        public boolean isAssignableTo(final Bundle bundle, final String className)
        {
            return false;
        }

        @Override
        public int compareTo(final Object reference)
        {
            return name.compareTo(name);
        }

        public String getName()
        {
            return name;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + (name == null ? 0 : name.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final ServiceReferenceStubImpl other = (ServiceReferenceStubImpl) obj;
            if (name == null)
            {
                if (other.name != null)
                {
                    return false;
                }
            }
            else if (!name.equals(other.name))
            {
                return false;
            }
            return true;
        }

        @Override
        public String toString()
        {
            return "[Ref " + name + "]";
        }
    }

}
