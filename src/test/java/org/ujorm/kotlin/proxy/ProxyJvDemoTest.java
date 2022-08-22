package org.ujorm.kotlin.proxy;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class ProxyJvDemoTest {

    @Disabled("cannot access a member of class java.lang.invoke")
    @Test
    public void testProxy1() {
        final Class<JDuck> entityClass = JDuck.class;
        final InvocationHandler handler = (proxy, method, args) -> {
            if (method.isDefault()) {
                final Constructor<Lookup> constructor = Lookup.class.getDeclaredConstructor(Class.class);
                constructor.setAccessible(true);
                return constructor.newInstance(entityClass)
                        .in(entityClass)
                        .unreflectSpecial(method, entityClass)
                        .bindTo(proxy)
                        .invokeWithArguments();
            } else switch (method.getName()) {
                case "name":
                    return "XYZ";
                default:
                    return null;
            }
        };

        final JDuck duck = newProxy(entityClass, handler);
        String value = duck.quack();
        String name = duck.name();
        Integer age = duck.age();

        Assertions.assertEquals("QUACK", value);
        Assertions.assertEquals("XYZ", name);
        Assertions.assertEquals(null, age);
    }

    @Disabled("cannot access a member of class java.lang.invoke")
    @Test
    public void testProxy2() {
        final Class<KDuck> entityClass = KDuck.class;
        final InvocationHandler handler = (proxy, method, args) -> {
            if (method.isDefault()) {
                final Constructor<Lookup> constructor = Lookup.class.getDeclaredConstructor(Class.class);
                constructor.setAccessible(true);
                return constructor.newInstance(entityClass)
                        .in(entityClass)
                        .unreflectSpecial(method, entityClass)
                        .bindTo(proxy)
                        .invokeWithArguments();
            } else switch (method.getName()) {
                case "name":
                    return "XYZ";
                default:
                    return null;
            }
        };

        final KDuck duck = newProxy(entityClass, handler);
        String value = duck.quack();
        String name = duck.name();
        Integer age = duck.age();

        Assertions.assertEquals("QUACK", value);
        Assertions.assertEquals("XYZ", name);
        Assertions.assertEquals(null, age);
    }

    @NotNull
    private <T> T newProxy(Class<T> entityClass, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(
                entityClass.getClassLoader(),
                new Class[]{entityClass}, handler);
    }

}
