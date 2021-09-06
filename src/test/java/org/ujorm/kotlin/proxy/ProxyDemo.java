package org.ujorm.kotlin.proxy;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;

interface Duck {
    default String quack() {
        return "QUACK";
    }
    String name();
    Integer age();
}

public class ProxyDemo {

    public static void main(String[] a) {
        final Class<?> targetClass = Duck.class;
        Duck duck = (Duck) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[]{targetClass}, (proxy, method, args) -> {
                    if (method.isDefault()) {
                        final Constructor<Lookup> constructor = Lookup.class.getDeclaredConstructor(Class.class);
                        constructor.setAccessible(true);
                        return constructor.newInstance(targetClass)
                                .in(targetClass)
                                .unreflectSpecial(method, targetClass)
                                .bindTo(proxy)
                                .invokeWithArguments();
                    } else switch (method.getName()) {
                        case "name": return "XYZ";
                        default: return null;
                    }
                }
        );

        String value = duck.quack();
        System.out.println(value);

        String name = duck.name();
        System.out.println(name);

        Integer age = duck.age();
        System.out.println(age);

    }
}