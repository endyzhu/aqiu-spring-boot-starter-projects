package com.github.aqiu202.aop.pointcut;

import com.github.aqiu202.aop.keygen.KeyGenerator;
import com.github.aqiu202.aop.spel.EvaluationFiller;
import com.github.aqiu202.aop.util.KeyGeneratorUtils;
import com.github.aqiu202.aop.util.ServletRequestUtils;
import com.github.aqiu202.aop.util.SpELUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * <pre>AbstractAnnotationInterceptor</pre>
 *
 * @author aqiu 2020/11/30 9:26
 **/
public abstract class AbstractKeyAnnotationInterceptor<T extends Annotation> implements
        AnnotationMethodInterceptor<T>,
        ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(AnnotationMethodInterceptor.class);

    protected EvaluationFiller evaluationFiller;

    protected ApplicationContext applicationContext;

    protected boolean spEL = true;

    public AbstractKeyAnnotationInterceptor() {
    }

    public AbstractKeyAnnotationInterceptor(EvaluationFiller evaluationFiller) {
        this.evaluationFiller = evaluationFiller;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public EvaluationFiller getEvaluationFiller() {
        return evaluationFiller;
    }

    public void setEvaluationFiller(EvaluationFiller evaluationFiller) {
        this.evaluationFiller = evaluationFiller;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public boolean isSpEL() {
        return spEL;
    }

    public void setSpEL(boolean spEL) {
        this.spEL = spEL;
    }

    protected String processKey(String key, Object target, Method method, Object[] parameters) {
        if (SpELUtils.isSpEL(key)) {
            key = SpELUtils.handleSpEl(key, target, method, parameters, this.evaluationFiller);
        } else {
            key = SpELUtils.handleNormalKey(key);
        }
        return key;
    }

    protected String generatorKey(MethodInvocation invocation, T annotation) {
        String key = this.getKey(annotation);
        if (StringUtils.isEmpty(key)) {
            final KeyGenerator keyGenerator = KeyGeneratorUtils
                    .getKeyGenerator(this.getKeyGeneratorName(annotation), this.applicationContext);
            key = keyGenerator
                    .generate(ServletRequestUtils.getCurrentRequest(), invocation.getThis(),
                            invocation.getMethod(), invocation.getArguments());
        }
        return this.isSpEL() ? this.processKey(key, invocation.getThis(), invocation.getMethod(),
                invocation.getArguments()) : key;
    }

    @Override
    public Object intercept(MethodInvocation invocation, T t) throws Throwable {
        return this.intercept(invocation, t, this.generatorKey(invocation, t));
    }

    protected abstract String getKeyGeneratorName(T annotation);

    protected abstract String getKey(T annotation);

    protected Object intercept(MethodInvocation invocation, T t, String key) throws Throwable {
        this.beforeIntercept(t, key);
        Throwable throwable = null;
        try {
            return this.doIntercept(invocation, t, key);
        } catch (Throwable th) {
            log.error("", th);
            throwable = th;
            throw th;
        } finally {
            this.afterIntercept(t, key, throwable);
        }
    }

    protected void beforeIntercept(T t, String key) {

    }

    protected Object doIntercept(MethodInvocation invocation, T t, String key) throws Throwable {
        return invocation.proceed();
    }

    protected void afterIntercept(T t, String key, @Nullable Throwable throwable) {

    }
}
