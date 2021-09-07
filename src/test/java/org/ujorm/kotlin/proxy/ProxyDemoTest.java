package org.ujorm.kotlin.proxy;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class ProxyDemoTest {

    @Test
    public void testProxy1() {
        final Class<JDuck> targetClass = JDuck.class;
        final InvocationHandler handler = (proxy, method, args) -> {
            if (method.isDefault()) {
                final Constructor<Lookup> constructor = Lookup.class.getDeclaredConstructor(Class.class);
                constructor.setAccessible(true);
                return constructor.newInstance(targetClass)
                        .in(targetClass)
                        .unreflectSpecial(method, targetClass)
                        .bindTo(proxy)
                        .invokeWithArguments();
            } else switch (method.getName()) {
                case "name":
                    return "XYZ";
                default:
                    return null;
            }
        };

        final JDuck duck = newProxy(targetClass, handler);
        String value = duck.quack();
        String name = duck.name();
        Integer age = duck.age();

        Assertions.assertEquals("QUACK", value);
        Assertions.assertEquals("XYZ", name);
        Assertions.assertEquals(null, age);
    }

    @Test
    public void testProxy2() {
        final Class<KDuck> targetClass = KDuck.class;
        final InvocationHandler handler = (proxy, method, args) -> {
            if (method.isDefault()) {
                final Constructor<Lookup> constructor = Lookup.class.getDeclaredConstructor(Class.class);
                constructor.setAccessible(true);
                return constructor.newInstance(targetClass)
                        .in(targetClass)
                        .unreflectSpecial(method, targetClass)
                        .bindTo(proxy)
                        .invokeWithArguments();
            } else switch (method.getName()) {
                case "name":
                    return "XYZ";
                default:
                    return null;
            }
        };

        final KDuck duck = newProxy(targetClass, handler);
        String value = duck.quack();
        String name = duck.name();
        Integer age = duck.age();

        Assertions.assertEquals("QUACK", value);
        Assertions.assertEquals("XYZ", name);
        Assertions.assertEquals(null, age);
    }

    @NotNull
    private <T> T newProxy(Class<T> targetClass, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(
                targetClass.getClassLoader(),
                new Class[]{targetClass}, handler);
    }

}
