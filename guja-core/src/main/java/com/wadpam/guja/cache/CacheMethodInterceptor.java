package com.wadpam.guja.cache;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.wadpam.guja.exceptions.InternalServerErrorRestException;
import com.wadpam.guja.util.Triplet;
import net.sf.mardao.dao.Cached;
import net.sf.mardao.dao.Crud;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Method interceptor for adding caching to core crud methods.
 *
 * @author sosandstrom
 * @author mattiaslevin
 */
public class CacheMethodInterceptor implements MethodInterceptor {
  static final Logger LOGGER = LoggerFactory.getLogger(CacheMethodInterceptor.class);

  private final ConcurrentMap<String, Cache<Triplet, Optional<?>>> namespaces = new ConcurrentHashMap<>();

  private final Provider<CacheBuilder> cacheBuilderProvider;
  public CacheMethodInterceptor(Provider<CacheBuilder> cacheBuilderProvider) {
    this.cacheBuilderProvider = cacheBuilderProvider;
  }

  private static final String PUT_METHOD_NAME = "put";
  private static final String DELETE_METHOD_NAME = "delete";
  private static final String GET_METHOD_NAME = "get";
  private static final String QUERY_PAGE_METHOD_NAME = "queryPage";

  @Override
  public Object invoke(final MethodInvocation invocation) throws Throwable {

    final Class clazz = getClass(invocation);
    Cache<Triplet, Optional<?>> cache = getCacheInstance(clazz);

    final Object[] args = invocation.getArguments();
    final Triplet triple = Triplet.fromArray(args);
    final Method method = invocation.getMethod();

    if (!method.isAnnotationPresent(Crud.class)) {
      throw new InternalServerErrorRestException("Could not find Cached annotation");
    }
    LOGGER.trace("invoking on {}", method);

    if (PUT_METHOD_NAME.equals(method.getName())) {
      LOGGER.trace("   put");
      final Object id = invocation.proceed();
      args[1] = id;
      final Object entity = args[2];
      args[2] = null;
      checkNotNull(id);
      checkNotNull(entity);
      cache.put(Triplet.fromArray(args), Optional.of(entity));
      return id;
    }

    if (DELETE_METHOD_NAME.equals(method.getName())) {
      LOGGER.info("   delete");
      invocation.proceed();
      final Object id = args[1];
      checkNotNull(id);
      cache.put(triple, Optional.absent());
      return null;
    }

    if (GET_METHOD_NAME.equals(method.getName()) ||
        (QUERY_PAGE_METHOD_NAME.equals(method.getName()) && shouldCachePages(clazz))) {

      final Optional<?> optionalEntity = cache.get(triple, new Callable<Optional<?>>() {
        @Override
        public Optional<?> call() throws Exception {
          LOGGER.trace("Loading for {}({})", method.getName(), triple);
          try {
            final Object entity = invocation.proceed();
            return null != entity ? Optional.of(entity) : Optional.absent();
          } catch (Throwable throwable) {
            LOGGER.error("Failed to populate cache {} {}", clazz.getName(), throwable);
            throw new ExecutionException("During invocation: ", throwable);
          }
        }
      });
      return null != optionalEntity && optionalEntity.isPresent() ? optionalEntity.get() : null;

    }

    // Do nothing, pass through
    return invocation.proceed();
  }

  private boolean shouldCachePages(Class clazz) {
    return ((Cached)clazz.getAnnotation(Cached.class)).cachePages();
  }

  private Cache<Triplet, Optional<?>> getCacheInstance(Class clazz) {
    final String className = clazz.getName();

    Cache<Triplet, Optional<?>> cache = namespaces.get(className);
    if (null == cache) {
      final Cached annotation = (Cached) clazz.getAnnotation(Cached.class);
      LOGGER.debug("Build new dao cache for {}", className);
      CacheBuilder<Triplet, Optional<?>> cacheBuilder = cacheBuilderProvider.get();
      if (null != annotation.from() || "".equals(annotation.from())) {
        cacheBuilder.from(annotation.from());
      } else {
        cacheBuilder.from(className);
      }
      if (annotation.size() > 0) {
        cacheBuilder.maximumSize(annotation.size());
      }
      if (annotation.expiresAfterSeconds() > 0) {
        cacheBuilder.expireAfterWrite(annotation.expiresAfterSeconds());
      }
      cache = cacheBuilder.build();
      Cache<Triplet, Optional<?>> existingCache = namespaces.putIfAbsent(className, cache);
      if (null != existingCache) {
        cache = existingCache;
      }
    }

    return cache;
  }

  private Class getClass(MethodInvocation invocation) {
    Class clazz = invocation.getThis().getClass();
    if (clazz.getName().contains("$$EnhancerByGuice$$")) {
      // Workaround for Guice adding some kind of enhancer object before the actual DAO
      // Check the superclass
      clazz = clazz.getSuperclass();
      if (!clazz.isAnnotationPresent(Cached.class)) {
        throw new InternalServerErrorRestException("Could not find Cached annotation");
      }
    }
    return clazz;
  }

}
