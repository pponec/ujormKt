package org.ujorm.kotlin.proxy;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;

interface Duck {
    default String quack() {
        return "QUACK";
    }
}

public class ProxyDemo {

    public static void main(String[] a) {
        final Class<?> targetClass = Duck.class;
        Duck duck = (Duck) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[]{targetClass}, (proxy, method, args) -> {
                    Constructor<Lookup> constructor = Lookup.class.getDeclaredConstructor(Class.class);
                    constructor.setAccessible(true);
                    return constructor.newInstance(targetClass)
                            .in(targetClass)
                            .unreflectSpecial(method, targetClass)
                            .bindTo(proxy)
                            .invokeWithArguments();
                }
        );

        String value = duck.quack();
        System.out.println(value);
    }
}