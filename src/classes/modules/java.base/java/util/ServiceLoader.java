package java.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.function.Supplier;

public class ServiceLoader<S> implements Iterable<S> {
    private static final String prefix = "META-INF/services/";
    private final Class<S> service;
    private final ClassLoader classLoader;
    private final Map<String, S> providers;

    public static interface Provider<S> extends Supplier<S> {
        Class<? extends S> type();
        @Override S get();
    }

    private ServiceLoader(Class service, ClassLoader classLoader) {
        this.service = service;
        this.classLoader = classLoader;
        this.providers = new LinkedHashMap<>();
    }

    public static <S> ServiceLoader<S> load(Class<S> service) {
        return load(service, Thread.currentThread().getContextClassLoader());
    }

    public static <S> ServiceLoader<S> load(Class<S> service, ClassLoader loader)
    {
        return new ServiceLoader<>(service, loader);
    }

    public static <S> ServiceLoader<S> loadInstalled(Class<S> service) {
        ClassLoader cl = ClassLoader.getPlatformClassLoader();
        return new ServiceLoader<>(service, cl);
    }

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
