package org.openmrs.module.kenyareg.api;

import ke.go.moh.oec.lib.Mediator;
import sun.net.spi.nameservice.dns.DNSNameService;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;

/**
 * Created by gitahi on 16/07/15.
 */
public class Test {

    public static void main(String[] args) {
        Test test = new Test();
        test.test();
    }

    private void test() {
        ClassLoader bootStrap = DNSNameService.class.getClassLoader().getParent();
        ClassLoader ext = Mediator.class.getClassLoader().getParent();

        print("HashMap Class Loader: " + HashMap.class.getClassLoader());
        print("DNSNameService Class Loader: " + DNSNameService.class.getClassLoader());
        print("Mediator Class Loader: " + Mediator.class.getClassLoader());

        MyClassLoader myClassLoader = new MyClassLoader(null);
        try {
            Class mediatorClass = myClassLoader.loadClass("ke.go.moh.oec.lib.Mediator");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void print(String content) {
        System.out.println(content);
    }

    public class MyClassLoader extends URLClassLoader {

        public MyClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        public MyClassLoader(URL[] urls) {
            super(urls);
        }

        public MyClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
            super(urls, parent, factory);
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            return super.loadClass(name);
        }
    }
}
