package com.app.tools.debug;
import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;


/**This is a utility class which will capture thread dump
 * we are using reflection framework here 
 *
 * @author Swanand I.
 *
 */

public class HeapDumper {
    // This is the name of the HotSpot Diagnostic MBean
    private static final String HOTSPOT_BEAN_CLASS_NAME =
         "com.sun.management:type=HotSpotDiagnostic";

    // field to store the hotspot diagnostic MBean 
    private static volatile Object hotspotMBean;

    /**
     * Call this method from your application whenever you 
     * want to dump the heap snapshot into a file.
     *
     * @param fileName name of the heap dump file
     * @param live flag that tells whether to dump
     *             only the live objects
     */
    static void dumpHeap(String fileName, boolean live) {
        // initialize hotspot diagnostic MBean
        initHotspotMBean();
        try {
            Class clazz = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
            Method m = clazz.getMethod("dumpHeap", String.class, boolean.class);
            m.invoke( hotspotMBean , fileName, live);
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }

    /**
     * initialize the hotspot diagnostic MBean field
     */
    private static void initHotspotMBean() {
        if (hotspotMBean == null) {
            synchronized (HeapDumper.class) {
                if (hotspotMBean == null) {
                    hotspotMBean = getHotspotMBean();
                }
            }
        }
    }


    /**Get 'HotSpotDiagnosticMXBean' bean object
     * @return
     */
    private static Object getHotspotMBean() {
        try {
            Class clazz = Class.forName("com.sun.management.HotSpotDiagnosticMXBean");
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            Object bean = 
                ManagementFactory.newPlatformMXBeanProxy(server,
                HOTSPOT_BEAN_CLASS_NAME, clazz);
            return bean;
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }

    public static void main(String[] args) {
        // default heap dump file name
        String fileName = "heap_dump.bin";
        // by default dump only the live objects
        boolean live = true;

        // simple command line options
        switch (args.length) {
            case 2:
                live = args[1].equals("true");
            case 1:
                fileName = args[0];
        }
        dumpHeap(fileName, live);
    }
}