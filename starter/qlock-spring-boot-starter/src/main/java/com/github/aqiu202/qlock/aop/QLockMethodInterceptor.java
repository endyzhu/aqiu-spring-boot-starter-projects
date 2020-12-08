package com.github.aqiu202.qlock.aop;

import com.github.aqiu202.aop.pointcut.AbstractKeyAnnotationInterceptor;
import com.github.aqiu202.aop.spel.EvaluationFiller;
import com.github.aqiu202.lock.base.Lock;
import com.github.aqiu202.qlock.anno.QLock;
import java.util.concurrent.TimeUnit;

/**
 * <pre>QLockMethodInterceptor</pre>
 *
 * @author aqiu 2020/12/2 13:26
 **/
public class QLockMethodInterceptor extends AbstractKeyAnnotationInterceptor<QLock> {

    private final Lock lock;

    public QLockMethodInterceptor(Lock lock) {
        this.lock = lock;
    }

    public QLockMethodInterceptor(Lock lock, EvaluationFiller evaluationFiller) {
        super(evaluationFiller);
        this.lock = lock;
    }

    @Override
    public String getKeyGeneratorName(QLock annotation) {
        return annotation.keyGenerator();
    }

    @Override
    public String getKey(QLock annotation) {
        return annotation.key();
    }

    @Override
    protected void beforeIntercept(QLock qLock, String key) {
        long timeout = qLock.timeout();
        TimeUnit timeUnit = qLock.timeUnit();
        final Boolean getLock = this.lock.acquire(key, timeout, timeUnit);
        if (!getLock) {
            throw new IllegalArgumentException(qLock.message());
        }
    }

    @Override
    protected void afterIntercept(QLock qLock, String key, Throwable throwable) {
        this.lock.release(key, qLock.timeout(), qLock.timeUnit());
    }
}
