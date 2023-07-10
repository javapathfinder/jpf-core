/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * The Java Pathfinder core (jpf-core) platform is licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.function.Supplier;

/**
 * MJI model class for java.util.ServiceLoader
 *
 * Model class whose primary objective is to eliminate
 * the ServiceLoader class' dependencies on any internal
 * classes belonging to packages like <i>jdk.internal</i> or <i>sun</i>
 * @param <S>
 */
public class ServiceLoader<S> implements Iterable<S> {
    //service configuration file to configure service providers
    private static final String prefix = "META-INF/services/";
    private final Class<S> service;
    private ClassLoader classLoader;
    private final Map<String, S> providers;

    /**
     * Represents a service provider located by {@code ServiceLoader}.
     *
     * @param  <S> The service type
     * @since 9
     * @spec JPMS
     */
    public static interface Provider<S> extends Supplier<S> {
        Class<? extends S> type();
        @Override S get();
    }

    /**
     * Initializes a new instance of this class for locating service providers
     * for the given service via the classloader
     * @param service
     * @param classLoader
     */

    private ServiceLoader(Class service, ClassLoader classLoader) {
        this.service = service;
        this.classLoader = classLoader;
        this.providers = new LinkedHashMap<>();
    }

    /**
     * Creates a new service loader for the given service type
     *
     * @param <S> the class of the service type
     *
     * @param service interface or abstract class representing the service
     *
     * @return A new service loader
     */
    public static <S> ServiceLoader<S> load(Class<S> service) {
        return load(service, Thread.currentThread().getContextClassLoader());
    }

    /**
     *
     * Creates a new service loader for the given service type. The
     * service loader makes use of the given class loader as the starting
     * point to locate service providers for the service.
     *
     * @param <S> the class of the service type
     *
     * @param service interface or abstract class representing the service
     *
     * @param loader the classloader to be used to load provider-configuration files
     *               and provider classes
     *
     * @return A new service loader
     */
    public static <S> ServiceLoader<S> load(Class<S> service, ClassLoader loader)
    {
        return new ServiceLoader<>(service, loader);
    }

    /**
     *
     * Creates a new service loader for the given service type, using the
     * {@linkplain ClassLoader#getPlatformClassLoader() platform class loader}
     *
     * <p> This convenience method is equivalent to: </p>
     *
     * <pre>{@code
     *     ServiceLoader.load(service, ClassLoader.getPlatformClassLoader())
     * }</pre>
     *
     *
     * @param <S> the class of the service type
     *
     * @param service the interface or abstract class representing the service
     *
     * @return A new service loader
     *
     */
    public static <S> ServiceLoader<S> loadInstalled(Class<S> service) {
        ClassLoader cl = ClassLoader.getPlatformClassLoader();
        return new ServiceLoader<>(service, cl);
    }

    /**
     * Returns an iterator to lazily load and instantiate the
     * available providers of this loader's service
     *
     * @return An iterator that lazily loads providers for this
     * loader's service
     *
     */
    @Override
    public Iterator<S> iterator() {
        return new LazyIterator();
    }

    private class LazyIterator implements Iterator<S> {

        private Enumeration<URL> configFiles;
        private Iterator<String> providerNames;
        private S nextProvider;

        public LazyIterator() {
            try {
                String configFile = prefix + service.getName();
                // ensure classloader is correcltly initialized before use
                if(classLoader == null) {
                    classLoader = Thread.currentThread().getContextClassLoader();
                    if(classLoader == null) {
                        classLoader = ClassLoader.getSystemClassLoader();
                    }
                }
                // get enum of URLs matching the config file name.
                configFiles = classLoader.getResources(configFile);
                providerNames = Collections.emptyIterator();
            } catch (IOException e) {
                throw new ServiceConfigurationError("Service provider config error", e);
            }
        }

        @Override
        public boolean hasNext() {
            if(nextProvider != null) {
                return true;
            }

            // loop until provider is found or all config files and provider names are visited.
            while((nextProvider == null) && (configFiles.hasMoreElements()) || providerNames.hasNext()) {
                if(!providerNames.hasNext()) {
                    URL url = configFiles.nextElement();
                    try {
                        // attempt to read the provider from the config file via the helper method.
                        providerNames = getProviders(url);
                    } catch (IOException e) {
                        throw new ServiceConfigurationError("Error loading service file", e);
                    }
                }

                if(providerNames.hasNext()) {
                    String providerName = providerNames.next();
                    try {
                        Class<?> providerClass = Class.forName(providerName, false, classLoader);
                        // check to make sure the provider class implements service.
                        if(!service.isAssignableFrom(providerClass)) {
                            throw new ServiceConfigurationError("Provider class " + providerName + "does not implement service provider" );
                        }
                        nextProvider = service.cast(providerClass.getDeclaredConstructor().newInstance());
                        // add provider to provider cache
                        providers.put(providerName, nextProvider);
                        return true;
                    } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        throw new ServiceConfigurationError("Error with service provider " + providerName, e);
                    }
                }
            }
            return false;
        }

        @Override
        public S next() {
            if(!hasNext()) {
                throw new NoSuchElementException();
            }

            S provider = nextProvider;
            nextProvider = null;
            return provider;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        // Reads the providers from the provided config file
        private Iterator<String> getProviders(URL url) throws IOException {
            try(InputStream is = url.openStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
                List<String> lines = new ArrayList<>();
                String line;
                while((line = reader.readLine()) != null) {
                    line = line.trim();
                    if(!line.isEmpty()) {
                        lines.add(line);
                    }
                }
                return lines.iterator();
            }
        }
    }
}
