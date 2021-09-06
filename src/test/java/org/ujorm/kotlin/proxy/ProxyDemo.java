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
        Duck duck = (Duck) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[]{Duck.class}, (proxy, method, args) -> {
                    Constructor<Lookup> constructor = Lookup.class.getDeclaredConstructor(Class.class);
                    constructor.setAccessible(true);
                    return constructor.newInstance(Duck.class)
                            .in(Duck.class)
                            .unreflectSpecial(method, Duck.class)
                            .bindTo(proxy)
                            .invokeWithArguments();
                }
        );

        String value = duck.quack();
        System.out.println(value);
    }
}