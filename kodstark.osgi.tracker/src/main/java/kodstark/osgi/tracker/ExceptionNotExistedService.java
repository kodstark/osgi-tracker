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

/**
 * Exception used in situation when mandatory service is missed.
 * 
 * @author <a href="mailto:kodstark@gmail.com">Kamil Demecki</a>
 * @since 14 Sep, 2009
 */
public class ExceptionNotExistedService extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    private final String serviceName;

    public String getServiceName()
    {
        return serviceName;
    }

    public ExceptionNotExistedService(final String serviceName)
    {
        super(serviceName);
        this.serviceName = serviceName;
    }
}
