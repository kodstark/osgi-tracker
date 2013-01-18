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
package kodstark.osgi.tracker.internal.testsupport;

public class ClassTestedRegister implements InterfaceTestedRegister, InterfaceTestedRegister01, InterfaceTestedRegister02,
        InterfaceTestedRegister03, InterfaceTestedRegister04, InterfaceTestedRegister05
{
    private Integer number;

    public ClassTestedRegister(Integer number)
    {
        this.number = number;
    }

    @Override
    public int action()
    {
        return number;
    }
}