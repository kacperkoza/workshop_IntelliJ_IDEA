/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.workshops_IntelliJ_IDEA.lang3.reflect;

import com.example.workshops_IntelliJ_IDEA.lang3.ArrayUtils;
import com.example.workshops_IntelliJ_IDEA.lang3.ClassUtils;
import com.example.workshops_IntelliJ_IDEA.lang3.Validate;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * <p>Utility reflection methods focused on {@link Method}s, originally from Commons BeanUtils.
 * Differences from the BeanUtils version may be noted, especially where similar functionality
 * already existed within Lang.
 * </p>
 *
 * <h2>Known Limitations</h2>
 * <h3>Accessing Public Methods In A Default Access Superclass</h3>
 * <p>There is an issue when invoking {@code public} methods contained in a default access superclass on JREs prior to 1.4.
 * Reflection locates these methods fine and correctly assigns them as {@code public}.
 * However, an {@link IllegalAccessException} is thrown if the method is invoked.</p>
 *
 * <p>{@link MethodUtils} contains a workaround for this situation.
 * It will attempt to call {@link java.lang.reflect.AccessibleObject#setAccessible(boolean)} on this method.
 * If this call succeeds, then the method can be invoked as normal.
 * This call will only succeed when the application has sufficient security privileges.
 * If this call fails then the method may fail.</p>
 *
 * @since 2.5
 */
public class MethodUtils {

    private static final Comparator<Method> METHOD_BY_SIGNATURE = Comparator.comparing(Method::toString);

    /**
     * <p>{@link MethodUtils} instances should NOT be constructed in standard programming.
     * Instead, the class should be used as
     * {@code MethodUtils.getAccessibleMethod(method)}.</p>
     *
     * <p>This constructor is {@code public} to permit tools that require a JavaBean
     * instance to operate.</p>
     */
    public MethodUtils() {
    }

    /**
     * <p>Invokes a named method without parameters.</p>
     *
     * <p>This method delegates the method search to {@link #getMatchingAccessibleMethod(Class, String, Class[])}.</p>
     *
     * <p>This is a convenient wrapper for
     * {@link #invokeMethod(Object object, String methodName, Object[] args, Class[] parameterTypes)}.
     * </p>
     *
     * @param object     invoke method on this object
     * @param methodName get method with this name
     * @return The value returned by the invoked method
     * @throws NoSuchMethodException     if there is no such accessible method
     * @throws InvocationTargetException wraps an exception thrown by the method invoked
     * @throws IllegalAccessException    if the requested method is not accessible via reflection
     * @since 3.4
     */
    public static Object invokeMethod(final Object object, final String methodName) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        return invokeMethod(object, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY, null);
    }

    /**
     * <p>Invokes a named method without parameters.</p>
     *
     * <p>This is a convenient wrapper for
     * {@link #invokeMethod(Object object, boolean forceAccess, String methodName, Object[] args, Class[] parameterTypes)}.
     * </p>
     *
     * @param object      invoke method on this object
     * @param forceAccess force access to invoke method even if it's not accessible
     * @param methodName  get method with this name
     * @return The value returned by the invoked method
     * @throws NoSuchMethodException     if there is no such accessible method
     * @throws InvocationTargetException wraps an exception thrown by the method invoked
     * @throws IllegalAccessException    if the requested method is not accessible via reflection
     * @since 3.5
     */
    public static Object invokeMethod(final Object object, final boolean forceAccess, final String methodName)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return invokeMethod(object, forceAccess, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY, null);
    }

    /**
     * <p>Invokes a named method whose parameter type matches the object type.</p>
     *
     * <p>This method delegates the method search to {@link #getMatchingAccessibleMethod(Class, String, Class[])}.</p>
     *
     * <p>This method supports calls to methods taking primitive parameters
     * via passing in wrapping classes. So, for example, a {@code Boolean} object
     * would match a {@code boolean} primitive.</p>
     *
     * <p>This is a convenient wrapper for
     * {@link #invokeMethod(Object object, String methodName, Object[] args, Class[] parameterTypes)}.
     * </p>
     *
     * @param object     invoke method on this object
     * @param methodName get method with this name
     * @param args       use these arguments - treat null as empty array
     * @return The value returned by the invoked method
     * @throws NoSuchMethodException     if there is no such accessible method
     * @throws InvocationTargetException wraps an exception thrown by the method invoked
     * @throws IllegalAccessException    if the requested method is not accessible via reflection
     */
    public static Object invokeMethod(final Object object, final String methodName,
                                      Object... args) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        final Class<?>[] parameterTypes = ClassUtils.toClass(args);
        return invokeMethod(object, methodName, args, parameterTypes);
    }

    /**
     * <p>Invokes a named method whose parameter type matches the object type.</p>
     *
     * <p>This method supports calls to methods taking primitive parameters
     * via passing in wrapping classes. So, for example, a {@code Boolean} object
     * would match a {@code boolean} primitive.</p>
     *
     * <p>This is a convenient wrapper for
     * {@link #invokeMethod(Object object, boolean forceAccess, String methodName, Object[] args, Class[] parameterTypes)}.
     * </p>
     *
     * @param object      invoke method on this object
     * @param forceAccess force access to invoke method even if it's not accessible
     * @param methodName  get method with this name
     * @param args        use these arguments - treat null as empty array
     * @return The value returned by the invoked method
     * @throws NoSuchMethodException     if there is no such accessible method
     * @throws InvocationTargetException wraps an exception thrown by the method invoked
     * @throws IllegalAccessException    if the requested method is not accessible via reflection
     * @since 3.5
     */
    public static Object invokeMethod(final Object object, final boolean forceAccess, final String methodName,
                                      Object... args) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        final Class<?>[] parameterTypes = ClassUtils.toClass(args);
        return invokeMethod(object, forceAccess, methodName, args, parameterTypes);
    }

    /**
     * <p>Invokes a named method whose parameter type matches the object type.</p>
     *
     * <p>This method supports calls to methods taking primitive parameters
     * via passing in wrapping classes. So, for example, a {@code Boolean} object
     * would match a {@code boolean} primitive.</p>
     *
     * @param object         invoke method on this object
     * @param forceAccess    force access to invoke method even if it's not accessible
     * @param methodName     get method with this name
     * @param args           use these arguments - treat null as empty array
     * @param parameterTypes match these parameters - treat null as empty array
     * @return The value returned by the invoked method
     * @throws NoSuchMethodException     if there is no such accessible method
     * @throws InvocationTargetException wraps an exception thrown by the method invoked
     * @throws IllegalAccessException    if the requested method is not accessible via reflection
     * @since 3.5
     */
    public static Object invokeMethod(final Object object, final boolean forceAccess, final String methodName,
                                      Object[] args, Class<?>[] parameterTypes)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        parameterTypes = ArrayUtils.nullToEmpty(parameterTypes);
        args = ArrayUtils.nullToEmpty(args);

        final String messagePrefix;
        Method method = null;

        if (forceAccess) {
            messagePrefix = "No such method: ";
            method = getMatchingMethod(object.getClass(),
                    methodName, parameterTypes);
            if (method != null && !method.isAccessible()) {
                method.setAccessible(true);
            }
        } else {
            messagePrefix = "No such accessible method: ";
            method = getMatchingAccessibleMethod(object.getClass(),
                    methodName, parameterTypes);
        }

        if (method == null) {
            throw new NoSuchMethodException(messagePrefix
                    + methodName + "() on object: "
                    + object.getClass().getName());
        }
        args = toVarArgs(method, args);

        return method.invoke(object, args);
    }

    /**
     * <p>Invokes a named method whose parameter type matches the object type.</p>
     *
     * <p>This method delegates the method search to {@link #getMatchingAccessibleMethod(Class, String, Class[])}.</p>
     *
     * <p>This method supports calls to methods taking primitive parameters
     * via passing in wrapping classes. So, for example, a {@code Boolean} object
     * would match a {@code boolean} primitive.</p>
     *
     * @param object         invoke method on this object
     * @param methodName     get method with this name
     * @param args           use these arguments - treat null as empty array
     * @param parameterTypes match these parameters - treat null as empty array
     * @return The value returned by the invoked method
     * @throws NoSuchMethodException     if there is no such accessible method
     * @throws InvocationTargetException wraps an exception thrown by the method invoked
     * @throws IllegalAccessException    if the requested method is not accessible via reflection
     */
    public static Object invokeMethod(final Object object, final String methodName,
                                      final Object[] args, final Class<?>[] parameterTypes)
            throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        return invokeMethod(object, false, methodName, args, parameterTypes);
    }

    /**
     * <p>Invokes a method whose parameter types match exactly the object
     * types.</p>
     *
     * <p>This uses reflection to invoke the method obtained from a call to
     * {@link #getAccessibleMethod}(Class, String, Class[])}.</p>
     *
     * @param object     invoke method on this object
     * @param methodName get method with this name
     * @return The value returned by the invoked method
     * @throws NoSuchMethodException     if there is no such accessible method
     * @throws InvocationTargetException wraps an exception thrown by the
     *                                   method invoked
     * @throws IllegalAccessException    if the requested method is not accessible
     *                                   via reflection
     * @since 3.4
     */
    public static Object invokeExactMethod(final Object object, final String methodName) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        return invokeExactMethod(object, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY, null);
    }

    /**
     * <p>Invokes a method with no parameters.</p>
     *
     * <p>This uses reflection to invoke the method obtained from a call to
     * {@link #getAccessibleMethod}(Class, String, Class[])}.</p>
     *
     * @param object     invoke method on this object
     * @param methodName get method with this name
     * @param args       use these arguments - treat null as empty array
     * @return The value returned by the invoked method
     * @throws NoSuchMethodException     if there is no such accessible method
     * @throws InvocationTargetException wraps an exception thrown by the
     *                                   method invoked
     * @throws IllegalAccessException    if the requested method is not accessible
     *                                   via reflection
     */
    public static Object invokeExactMethod(final Object object, final String methodName,
                                           Object... args) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        final Class<?>[] parameterTypes = ClassUtils.toClass(args);
        return invokeExactMethod(object, methodName, args, parameterTypes);
    }

    /**
     * <p>Invokes a method whose parameter types match exactly the parameter
     * types given.</p>
     *
     * <p>This uses reflection to invoke the method obtained from a call to
     * {@link #getAccessibleMethod(Class, String, Class[])}.</p>
     *
     * @param object         invoke method on this object
     * @param methodName     get method with this name
     * @param args           use these arguments - treat null as empty array
     * @param parameterTypes match these parameters - treat {@code null} as empty array
     * @return The value returned by the invoked method
     * @throws NoSuchMethodException     if there is no such accessible method
     * @throws InvocationTargetException wraps an exception thrown by the
     *                                   method invoked
     * @throws IllegalAccessException    if the requested method is not accessible
     *                                   via reflection
     */
    public static Object invokeExactMethod(final Object object, final String methodName,
                                           Object[] args, Class<?>[] parameterTypes)
            throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        parameterTypes = ArrayUtils.nullToEmpty(parameterTypes);
        final Method method = getAccessibleMethod(object.getClass(), methodName,
                parameterTypes);
        if (method == null) {
            throw new NoSuchMethodException("No such accessible method: "
                    + methodName + "() on object: "
                    + object.getClass().getName());
        }
        return method.invoke(object, args);
    }

    /**
     * <p>Invokes a {@code static} method whose parameter types match exactly the parameter
     * types given.</p>
     *
     * <p>This uses reflection to invoke the method obtained from a call to
     * {@link #getAccessibleMethod(Class, String, Class[])}.</p>
     *
     * @param cls            invoke static method on this class
     * @param methodName     get method with this name
     * @param args           use these arguments - treat {@code null} as empty array
     * @param parameterTypes match these parameters - treat {@code null} as empty array
     * @return The value returned by the invoked method
     * @throws NoSuchMethodException     if there is no such accessible method
     * @throws InvocationTargetException wraps an exception thrown by the
     *                                   method invoked
     * @throws IllegalAccessException    if the requested method is not accessible
     *                                   via reflection
     */
    public static Object invokeExactStaticMethod(final Class<?> cls, final String methodName,
                                                 Object[] args, Class<?>[] parameterTypes)
            throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        parameterTypes = ArrayUtils.nullToEmpty(parameterTypes);
        final Method method = getAccessibleMethod(cls, methodName, parameterTypes);
        if (method == null) {
            throw new NoSuchMethodException("No such accessible method: "
                    + methodName + "() on class: " + cls.getName());
        }
        return method.invoke(null, args);
    }

    /**
     * <p>Invokes a named {@code static} method whose parameter type matches the object type.</p>
     *
     * <p>This method delegates the method search to {@link #getMatchingAccessibleMethod(Class, String, Class[])}.</p>
     *
     * <p>This method supports calls to methods taking primitive parameters
     * via passing in wrapping classes. So, for example, a {@code Boolean} class
     * would match a {@code boolean} primitive.</p>
     *
     * <p>This is a convenient wrapper for
     * {@link #invokeStaticMethod(Class, String, Object[], Class[])}.
     * </p>
     *
     * @param cls        invoke static method on this class
     * @param methodName get method with this name
     * @param args       use these arguments - treat {@code null} as empty array
     * @return The value returned by the invoked method
     * @throws NoSuchMethodException     if there is no such accessible method
     * @throws InvocationTargetException wraps an exception thrown by the
     *                                   method invoked
     * @throws IllegalAccessException    if the requested method is not accessible
     *                                   via reflection
     */
    public static Object invokeStaticMethod(final Class<?> cls, final String methodName,
                                            Object... args) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        final Class<?>[] parameterTypes = ClassUtils.toClass(args);
        return invokeStaticMethod(cls, methodName, args, parameterTypes);
    }

    /**
     * <p>Invokes a named {@code static} method whose parameter type matches the object type.</p>
     *
     * <p>This method delegates the method search to {@link #getMatchingAccessibleMethod(Class, String, Class[])}.</p>
     *
     * <p>This method supports calls to methods taking primitive parameters
     * via passing in wrapping classes. So, for example, a {@code Boolean} class
     * would match a {@code boolean} primitive.</p>
     *
     * @param cls            invoke static method on this class
     * @param methodName     get method with this name
     * @param args           use these arguments - treat {@code null} as empty array
     * @param parameterTypes match these parameters - treat {@code null} as empty array
     * @return The value returned by the invoked method
     * @throws NoSuchMethodException     if there is no such accessible method
     * @throws InvocationTargetException wraps an exception thrown by the
     *                                   method invoked
     * @throws IllegalAccessException    if the requested method is not accessible
     *                                   via reflection
     */
    public static Object invokeStaticMethod(final Class<?> cls, final String methodName,
                                            Object[] args, Class<?>[] parameterTypes)
            throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        parameterTypes = ArrayUtils.nullToEmpty(parameterTypes);
        final Method method = getMatchingAccessibleMethod(cls, methodName,
                parameterTypes);
        if (method == null) {
            throw new NoSuchMethodException("No such accessible method: "
                    + methodName + "() on class: " + cls.getName());
        }
        args = toVarArgs(method, args);
        return method.invoke(null, args);
    }

    private static Object[] toVarArgs(final Method method, Object[] args) {
        if (method.isVarArgs()) {
            final Class<?>[] methodParameterTypes = method.getParameterTypes();
            args = getVarArgs(args, methodParameterTypes);
        }
        return args;
    }

    /**
     * <p>Given an arguments array passed to a varargs method, return an array of arguments in the canonical form,
     * i.e. an array with the declared number of parameters, and whose last parameter is an array of the varargs type.
     * </p>
     *
     * @param args                 the array of arguments passed to the varags method
     * @param methodParameterTypes the declared array of method parameter types
     * @return an array of the variadic arguments passed to the method
     * @since 3.5
     */
    static Object[] getVarArgs(final Object[] args, final Class<?>[] methodParameterTypes) {
        if (args.length == methodParameterTypes.length && (args[args.length - 1] == null ||
                args[args.length - 1].getClass().equals(methodParameterTypes[methodParameterTypes.length - 1]))) {
            // The args array is already in the canonical form for the method.
            return args;
        }

        // Construct a new array matching the method's declared parameter types.
        final Object[] newArgs = new Object[methodParameterTypes.length];

        // Copy the normal (non-varargs) parameters
        System.arraycopy(args, 0, newArgs, 0, methodParameterTypes.length - 1);

        // Construct a new array for the variadic parameters
        final Class<?> varArgComponentType = methodParameterTypes[methodParameterTypes.length - 1].getComponentType();
        final int varArgLength = args.length - methodParameterTypes.length + 1;

        Object varArgsArray = Array.newInstance(ClassUtils.primitiveToWrapper(varArgComponentType), varArgLength);
        // Copy the variadic arguments into the varargs array.
        System.arraycopy(args, methodParameterTypes.length - 1, varArgsArray, 0, varArgLength);

        if (varArgComponentType.isPrimitive()) {
            // unbox from wrapper type to primitive type
            varArgsArray = ArrayUtils.toPrimitive(varArgsArray);
        }

        // Store the varargs array in the last position of the array to return
        newArgs[methodParameterTypes.length - 1] = varArgsArray;

        // Return the canonical varargs array.
        return newArgs;
    }

    /**
     * <p>Invokes a {@code static} method whose parameter types match exactly the object
     * types.</p>
     *
     * <p>This uses reflection to invoke the method obtained from a call to
     * {@link #getAccessibleMethod(Class, String, Class[])}.</p>
     *
     * @param cls        invoke static method on this class
     * @param methodName get method with this name
     * @param args       use these arguments - treat {@code null} as empty array
     * @return The value returned by the invoked method
     * @throws NoSuchMethodException     if there is no such accessible method
     * @throws InvocationTargetException wraps an exception thrown by the
     *                                   method invoked
     * @throws IllegalAccessException    if the requested method is not accessible
     *                                   via reflection
     */
    public static Object invokeExactStaticMethod(final Class<?> cls, final String methodName,
                                                 Object... args) throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        args = ArrayUtils.nullToEmpty(args);
        final Class<?>[] parameterTypes = ClassUtils.toClass(args);
        return invokeExactStaticMethod(cls, methodName, args, parameterTypes);
    }

    /**
     * <p>Returns an accessible method (that is, one that can be invoked via
     * reflection) with given name and parameters. If no such method
     * can be found, return {@code null}.
     * This is just a convenience wrapper for
     * {@link #getAccessibleMethod(Method)}.</p>
     *
     * @param cls            get method from this class
     * @param methodName     get method with this name
     * @param parameterTypes with these parameters types
     * @return The accessible method
     */
    public static Method getAccessibleMethod(final Class<?> cls, final String methodName,
                                             final Class<?>... parameterTypes) {
        try {
            return getAccessibleMethod(cls.getMethod(methodName,
                    parameterTypes));
        } catch (final NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * <p>Returns an accessible method (that is, one that can be invoked via
     * reflection) that implements the specified Method. If no such method
     * can be found, return {@code null}.</p>
     *
     * @param method The method that we wish to call
     * @return The accessible method
     */
    public static Method getAccessibleMethod(Method method) {
        if (!MemberUtils.isAccessible(method)) {
            return null;
        }
        // If the declaring class is public, we are done
        final Class<?> cls = method.getDeclaringClass();
        if (Modifier.isPublic(cls.getModifiers())) {
            return method;
        }
        final String methodName = method.getName();
        final Class<?>[] parameterTypes = method.getParameterTypes();

        // Check the implemented interfaces and subinterfaces
        method = getAccessibleMethodFromInterfaceNest(cls, methodName,
                parameterTypes);

        // Check the superclass chain
        if (method == null) {
            method = getAccessibleMethodFromSuperclass(cls, methodName,
                    parameterTypes);
        }
        return method;
    }

    /**
     * <p>Returns an accessible method (that is, one that can be invoked via
     * reflection) by scanning through the superclasses. If no such method
     * can be found, return {@code null}.</p>
     *
     * @param cls            Class to be checked
     * @param methodName     Method name of the method we wish to call
     * @param parameterTypes The parameter type signatures
     * @return the accessible method or {@code null} if not found
     */
    private static Method getAccessibleMethodFromSuperclass(final Class<?> cls,
                                                            final String methodName, final Class<?>... parameterTypes) {
        Class<?> parentClass = cls.getSuperclass();
        while (parentClass != null) {
            if (Modifier.isPublic(parentClass.getModifiers())) {
                try {
                    return parentClass.getMethod(methodName, parameterTypes);
                } catch (final NoSuchMethodException e) {
                    return null;
                }
            }
            parentClass = parentClass.getSuperclass();
        }
        return null;
    }

    /**
     * <p>Returns an accessible method (that is, one that can be invoked via
     * reflection) that implements the specified method, by scanning through
     * all implemented interfaces and subinterfaces. If no such method
     * can be found, return {@code null}.</p>
     *
     * <p>There isn't any good reason why this method must be {@code private}.
     * It is because there doesn't seem any reason why other classes should
     * call this rather than the higher level methods.</p>
     *
     * @param cls            Parent class for the interfaces to be checked
     * @param methodName     Method name of the method we wish to call
     * @param parameterTypes The parameter type signatures
     * @return the accessible method or {@code null} if not found
     */
    private static Method getAccessibleMethodFromInterfaceNest(Class<?> cls,
                                                               final String methodName, final Class<?>... parameterTypes) {
        // Search up the superclass chain
        for (; cls != null; cls = cls.getSuperclass()) {

            // Check the implemented interfaces of the parent class
            final Class<?>[] interfaces = cls.getInterfaces();
            for (final Class<?> anInterface : interfaces) {
                // Is this interface public?
                if (!Modifier.isPublic(anInterface.getModifiers())) {
                    continue;
                }
                // Does the method exist on this interface?
                try {
                    return anInterface.getDeclaredMethod(methodName,
                            parameterTypes);
                } catch (final NoSuchMethodException e) { // NOPMD
                    /*
                     * Swallow, if no method is found after the loop then this
                     * method returns null.
                     */
                }
                // Recursively check our parent interfaces
                final Method method = getAccessibleMethodFromInterfaceNest(anInterface,
                        methodName, parameterTypes);
                if (method != null) {
                    return method;
                }
            }
        }
        return null;
    }

    /**
     * <p>Finds an accessible method that matches the given name and has compatible parameters.
     * Compatible parameters mean that every method parameter is assignable from
     * the given parameters.
     * In other words, it finds a method with the given name
     * that will take the parameters given.</p>
     *
     * <p>This method is used by
     * {@link
     * #invokeMethod(Object object, String methodName, Object[] args, Class[] parameterTypes)}.
     * </p>
     *
     * <p>This method can match primitive parameter by passing in wrapper classes.
     * For example, a {@code Boolean} will match a primitive {@code boolean}
     * parameter.
     * </p>
     *
     * @param cls            find method in this class
     * @param methodName     find method with this name
     * @param parameterTypes find method with most compatible parameters
     * @return The accessible method
     */
    public static Method getMatchingAccessibleMethod(final Class<?> cls,
                                                     final String methodName, final Class<?>... parameterTypes) {
        try {
            final Method method = cls.getMethod(methodName, parameterTypes);
            MemberUtils.setAccessibleWorkaround(method);
            return method;
        } catch (final NoSuchMethodException e) { // NOPMD - Swallow the exception
        }
        // search through all methods
        final Method[] methods = cls.getMethods();
        final List<Method> matchingMethods = new ArrayList<>();
        for (final Method method : methods) {
            // compare name and parameters
            if (method.getName().equals(methodName) &&
                    MemberUtils.isMatchingMethod(method, parameterTypes)) {
                matchingMethods.add(method);
            }
        }

        // Sort methods by signature to force deterministic result
        matchingMethods.sort(METHOD_BY_SIGNATURE);

        Method bestMatch = null;
        for (final Method method : matchingMethods) {
            // get accessible version of method
            final Method accessibleMethod = getAccessibleMethod(method);
            if (accessibleMethod != null && (bestMatch == null || MemberUtils.compareMethodFit(
                    accessibleMethod,
                    bestMatch,
                    parameterTypes) < 0)) {
                bestMatch = accessibleMethod;
            }
        }
        if (bestMatch != null) {
            MemberUtils.setAccessibleWorkaround(bestMatch);
        }

        if (bestMatch != null && bestMatch.isVarArgs() && bestMatch.getParameterTypes().length > 0 && parameterTypes.length > 0) {
            final Class<?>[] methodParameterTypes = bestMatch.getParameterTypes();
            final Class<?> methodParameterComponentType = methodParameterTypes[methodParameterTypes.length - 1].getComponentType();
            final String methodParameterComponentTypeName = ClassUtils.primitiveToWrapper(methodParameterComponentType).getName();

            final Class<?> lastParameterType = parameterTypes[parameterTypes.length - 1];
            final String parameterTypeName = (lastParameterType == null) ? null : lastParameterType.getName();
            final String parameterTypeSuperClassName = (lastParameterType == null) ? null : lastParameterType.getSuperclass().getName();

            if (parameterTypeName != null && parameterTypeSuperClassName != null && !methodParameterComponentTypeName.equals(parameterTypeName)
                    && !methodParameterComponentTypeName.equals(parameterTypeSuperClassName)) {
                return null;
            }
        }

        return bestMatch;
    }

    /**
     * <p>Retrieves a method whether or not it's accessible. If no such method
     * can be found, return {@code null}.</p>
     *
     * @param cls            The class that will be subjected to the method search
     * @param methodName     The method that we wish to call
     * @param parameterTypes Argument class types
     * @return The method
     * @since 3.5
     */
    public static Method getMatchingMethod(final Class<?> cls, final String methodName,
                                           final Class<?>... parameterTypes) {
        Validate.notNull(cls, "cls");
        Validate.notEmpty(methodName, "methodName");

        final List<Method> methods = Arrays.stream(cls.getDeclaredMethods())
                .filter(method -> method.getName().equals(methodName))
                .collect(toList());

        ClassUtils.getAllSuperclasses(cls).stream()
                .map(Class::getDeclaredMethods)
                .flatMap(Arrays::stream)
                .filter(method -> method.getName().equals(methodName))
                .forEach(methods::add);

        for (final Method method : methods) {
            if (Arrays.deepEquals(method.getParameterTypes(), parameterTypes)) {
                return method;
            }
        }

        final TreeMap<Integer, List<Method>> candidates = new TreeMap<>();

        methods.stream()
                .filter(method -> ClassUtils.isAssignable(parameterTypes, method.getParameterTypes(), true))
                .forEach(method -> {
                    final int distance = distance(parameterTypes, method.getParameterTypes());
                    final List<Method> candidatesAtDistance = candidates.computeIfAbsent(distance, k -> new ArrayList<>());
                    candidatesAtDistance.add(method);
                });

        if (candidates.isEmpty()) {
            return null;
        }

        final List<Method> bestCandidates = candidates.values().iterator().next();
        if (bestCandidates.size() == 1) {
            return bestCandidates.get(0);
        }

        throw new IllegalStateException(
                String.format("Found multiple candidates for method %s on class %s : %s",
                        methodName + Arrays.stream(parameterTypes).map(String::valueOf).collect(Collectors.joining(",", "(", ")")),
                        cls.getName(),
                        bestCandidates.stream().map(Method::toString).collect(Collectors.joining(",", "[", "]")))
        );
    }

    /**
     * <p>Returns the aggregate number of inheritance hops between assignable argument class types.  Returns -1
     * if the arguments aren't assignable.  Fills a specific purpose for getMatchingMethod and is not generalized.</p>
     *
     * @param fromClassArray the Class array to calculate the distance from.
     * @param toClassArray   the Class array to calculate the distance to.
     * @return the aggregate number of inheritance hops between assignable argument class types.
     */
    private static int distance(final Class<?>[] fromClassArray, final Class<?>[] toClassArray) {
        int answer = 0;

        if (!ClassUtils.isAssignable(fromClassArray, toClassArray, true)) {
            return -1;
        }
        for (int offset = 0; offset < fromClassArray.length; offset++) {
            // Note InheritanceUtils.distance() uses different scoring system.
            final Class<?> aClass = fromClassArray[offset];
            final Class<?> toClass = toClassArray[offset];
            if (aClass == null || aClass.equals(toClass)) {
                continue;
            } else if (ClassUtils.isAssignable(aClass, toClass, true)
                    && !ClassUtils.isAssignable(aClass, toClass, false)) {
                answer++;
            } else {
                answer = answer + 2;
            }
        }

        return answer;
    }

    /**
     * Gets the hierarchy of overridden methods down to {@code result} respecting generics.
     *
     * @param method             lowest to consider
     * @param interfacesBehavior whether to search interfaces, {@code null} {@code implies} false
     * @return Set&lt;Method&gt; in ascending order from sub- to superclass
     * @throws NullPointerException if the specified method is {@code null}
     * @since 3.2
     */
    public static Set<Method> getOverrideHierarchy(final Method method, final ClassUtils.Interfaces interfacesBehavior) {
        Validate.notNull(method);
        final Set<Method> result = new LinkedHashSet<>();
        result.add(method);

        final Class<?>[] parameterTypes = method.getParameterTypes();

        final Class<?> declaringClass = method.getDeclaringClass();

        final Iterator<Class<?>> hierarchy = ClassUtils.hierarchy(declaringClass, interfacesBehavior).iterator();
        //skip the declaring class :P
        hierarchy.next();
        hierarchyTraversal:
        while (hierarchy.hasNext()) {
            final Class<?> c = hierarchy.next();
            final Method m = getMatchingAccessibleMethod(c, method.getName(), parameterTypes);
            if (m == null) {
                continue;
            }
            if (Arrays.equals(m.getParameterTypes(), parameterTypes)) {
                // matches without generics
                result.add(m);
                continue;
            }
            // necessary to get arguments every time in the case that we are including interfaces
            final Map<TypeVariable<?>, Type> typeArguments = TypeUtils.getTypeArguments(declaringClass, m.getDeclaringClass());
            for (int i = 0; i < parameterTypes.length; i++) {
                final Type childType = TypeUtils.unrollVariables(typeArguments, method.getGenericParameterTypes()[i]);
                final Type parentType = TypeUtils.unrollVariables(typeArguments, m.getGenericParameterTypes()[i]);
                if (!TypeUtils.equals(childType, parentType)) {
                    continue hierarchyTraversal;
                }
            }
            result.add(m);
        }
        return result;
    }

    /**
     * Gets all class level public methods of the given class that are annotated with the given annotation.
     *
     * @param cls           the {@link Class} to query
     * @param annotationCls the {@link Annotation} that must be present on a method to be matched
     * @return an array of Methods (possibly empty).
     * @throws NullPointerException if the class or annotation are {@code null}
     * @since 3.4
     */
    public static Method[] getMethodsWithAnnotation(final Class<?> cls, final Class<? extends Annotation> annotationCls) {
        return getMethodsWithAnnotation(cls, annotationCls, false, false);
    }

    /**
     * Gets all class level public methods of the given class that are annotated with the given annotation.
     *
     * @param cls           the {@link Class} to query
     * @param annotationCls the {@link Annotation} that must be present on a method to be matched
     * @return a list of Methods (possibly empty).
     * @throws IllegalArgumentException if the class or annotation are {@code null}
     * @since 3.4
     */
    public static List<Method> getMethodsListWithAnnotation(final Class<?> cls, final Class<? extends Annotation> annotationCls) {
        return getMethodsListWithAnnotation(cls, annotationCls, false, false);
    }

    /**
     * Gets all methods of the given class that are annotated with the given annotation.
     *
     * @param cls           the {@link Class} to query
     * @param annotationCls the {@link Annotation} that must be present on a method to be matched
     * @param searchSupers  determines if a lookup in the entire inheritance hierarchy of the given class should be performed
     * @param ignoreAccess  determines if non public methods should be considered
     * @return an array of Methods (possibly empty).
     * @throws NullPointerException if the class or annotation are {@code null}
     * @since 3.6
     */
    public static Method[] getMethodsWithAnnotation(final Class<?> cls, final Class<? extends Annotation> annotationCls,
                                                    final boolean searchSupers, final boolean ignoreAccess) {
        final List<Method> annotatedMethodsList = getMethodsListWithAnnotation(cls, annotationCls, searchSupers,
                ignoreAccess);
        return annotatedMethodsList.toArray(ArrayUtils.EMPTY_METHOD_ARRAY);
    }

    /**
     * Gets all methods of the given class that are annotated with the given annotation.
     *
     * @param cls           the {@link Class} to query
     * @param annotationCls the {@link Annotation} that must be present on a method to be matched
     * @param searchSupers  determines if a lookup in the entire inheritance hierarchy of the given class should be performed
     * @param ignoreAccess  determines if non public methods should be considered
     * @return a list of Methods (possibly empty).
     * @throws NullPointerException if either the class or annotation class is {@code null}
     * @since 3.6
     */
    public static List<Method> getMethodsListWithAnnotation(final Class<?> cls,
                                                            final Class<? extends Annotation> annotationCls,
                                                            final boolean searchSupers, final boolean ignoreAccess) {

        Validate.notNull(cls, "cls");
        Validate.notNull(annotationCls, "annotationCls");
        final List<Class<?>> classes = (searchSupers ? getAllSuperclassesAndInterfaces(cls)
                : new ArrayList<>());
        classes.add(0, cls);
        final List<Method> annotatedMethods = new ArrayList<>();
        for (final Class<?> acls : classes) {
            final Method[] methods = (ignoreAccess ? acls.getDeclaredMethods() : acls.getMethods());
            for (final Method method : methods) {
                if (method.getAnnotation(annotationCls) != null) {
                    annotatedMethods.add(method);
                }
            }
        }
        return annotatedMethods;
    }

    /**
     * <p>Gets the annotation object with the given annotation type that is present on the given method
     * or optionally on any equivalent method in super classes and interfaces. Returns null if the annotation
     * type was not present.</p>
     *
     * <p>Stops searching for an annotation once the first annotation of the specified type has been
     * found. Additional annotations of the specified type will be silently ignored.</p>
     *
     * @param <A>           the annotation type
     * @param method        the {@link Method} to query
     * @param annotationCls the {@link Annotation} to check if is present on the method
     * @param searchSupers  determines if a lookup in the entire inheritance hierarchy of the given class is performed
     *                      if the annotation was not directly present
     * @param ignoreAccess  determines if underlying method has to be accessible
     * @return the first matching annotation, or {@code null} if not found
     * @throws NullPointerException if either the method or annotation class is {@code null}
     * @since 3.6
     */
    public static <A extends Annotation> A getAnnotation(final Method method, final Class<A> annotationCls,
                                                         final boolean searchSupers, final boolean ignoreAccess) {

        Validate.notNull(method, "method");
        Validate.notNull(annotationCls, "annotationCls");
        if (!ignoreAccess && !MemberUtils.isAccessible(method)) {
            return null;
        }

        A annotation = method.getAnnotation(annotationCls);

        if (annotation == null && searchSupers) {
            final Class<?> mcls = method.getDeclaringClass();
            final List<Class<?>> classes = getAllSuperclassesAndInterfaces(mcls);
            for (final Class<?> acls : classes) {
                final Method equivalentMethod = (ignoreAccess ? MethodUtils.getMatchingMethod(acls, method.getName(), method.getParameterTypes())
                        : MethodUtils.getMatchingAccessibleMethod(acls, method.getName(), method.getParameterTypes()));
                if (equivalentMethod != null) {
                    annotation = equivalentMethod.getAnnotation(annotationCls);
                    if (annotation != null) {
                        break;
                    }
                }
            }
        }

        return annotation;
    }

    /**
     * <p>Gets a combination of {@link ClassUtils#getAllSuperclasses(Class)} and
     * {@link ClassUtils#getAllInterfaces(Class)}, one from superclasses, one
     * from interfaces, and so on in a breadth first way.</p>
     *
     * @param cls the class to look up, may be {@code null}
     * @return the combined {@code List} of superclasses and interfaces in order
     * going up from this one
     * {@code null} if null input
     */
    private static List<Class<?>> getAllSuperclassesAndInterfaces(final Class<?> cls) {
        if (cls == null) {
            return null;
        }

        final List<Class<?>> allSuperClassesAndInterfaces = new ArrayList<>();
        final List<Class<?>> allSuperclasses = ClassUtils.getAllSuperclasses(cls);
        int superClassIndex = 0;
        final List<Class<?>> allInterfaces = ClassUtils.getAllInterfaces(cls);
        int interfaceIndex = 0;
        while (interfaceIndex < allInterfaces.size() ||
                superClassIndex < allSuperclasses.size()) {
            final Class<?> acls;
            if (interfaceIndex >= allInterfaces.size()) {
                acls = allSuperclasses.get(superClassIndex++);
            } else if ((superClassIndex >= allSuperclasses.size()) || (interfaceIndex < superClassIndex) || !(superClassIndex < interfaceIndex)) {
                acls = allInterfaces.get(interfaceIndex++);
            } else {
                acls = allSuperclasses.get(superClassIndex++);
            }
            allSuperClassesAndInterfaces.add(acls);
        }
        return allSuperClassesAndInterfaces;
    }
}
