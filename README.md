osgi-tracker
============

Provide handy way for getting service from OSGi tracker. Major class is ```kodstark.osgi.tracker.TrackerRegister``` for getting services on demand from internal stored OSGi tracker.

Standard usecase is to create and close ```TrackerRegister``` in bundle activator and query activator for register instance. In that situation each bundle has own register and OSGi trackers are released when bundle is stopped.

```
public class Activator implements BundleActivator {

    private volatile static TrackerRegister register; 
    
    @Override
	public void start(BundleContext context) throws Exception {
        register = new TrackerRegister(context);
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		register.close();
	}
	
	public static TrackerRegister getRegister()
    {
        return register;
    }
}

[...]

public class SomeWhere
{
    public void foo(String[] args)
    {
        SomeType service = Activator.getRegister().getService(SomeType.class);
        service.bar();
    }
}
```

It is alternative approach to using OSGi tracker with white board pattern.

Build
------

First create local p2 site for artifacts which don't have public available site but exists in maven central repository. Normally they should exist on corporate p2 site.

```mvn package -Pmake-local-p2 -N```

Launch build

```mvn clean install```

Generate coverage report

```mvn clean install -Pcoverage```
