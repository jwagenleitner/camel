/**
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
package org.apache.camel.support;

import java.util.concurrent.TimeUnit;

import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.Route;
import org.apache.camel.spi.ExceptionHandler;
import org.apache.camel.spi.RoutePolicy;
import org.apache.camel.util.ServiceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base class for developing custom {@link RoutePolicy} implementations.
 *
 * @version 
 */
public abstract class RoutePolicySupport extends ServiceSupport implements RoutePolicy {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    private ExceptionHandler exceptionHandler;

    public void onInit(Route route) {
        if (exceptionHandler == null) {
            exceptionHandler = new LoggingExceptionHandler(route.getRouteContext().getCamelContext(), getClass());
        }
    }

    public void onRemove(Route route) {
        // noop
    }

    @Override
    public void onStart(Route route) {
        // noop
    }

    @Override
    public void onStop(Route route) {
        // noop
    }

    @Override
    public void onSuspend(Route route) {
        // noop
    }

    @Override
    public void onResume(Route route) {
        // noop
    }

    public void onExchangeBegin(Route route, Exchange exchange) {
        // noop
    }

    public void onExchangeDone(Route route, Exchange exchange) {
        // noop
    }

    public boolean startConsumer(Consumer consumer) throws Exception {
        boolean resumed = ServiceHelper.resumeService(consumer);
        if (resumed) {
            log.debug("Resuming consumer {}", consumer);
        }
        return resumed;
    }

    public boolean stopConsumer(Consumer consumer) throws Exception {
        boolean suspended = ServiceHelper.suspendService(consumer);
        if (suspended) {
            log.debug("Suspended consumer {}", consumer);
        }
        return suspended;
    }

    public void startRoute(Route route) throws Exception {
        route.getRouteContext().getCamelContext().startRoute(route.getId());
    }

    public void resumeRoute(Route route) throws Exception {
        route.getRouteContext().getCamelContext().resumeRoute(route.getId());
    }

    public void suspendRoute(Route route) throws Exception {
        route.getRouteContext().getCamelContext().suspendRoute(route.getId());
    }

    public void suspendRoute(Route route, long timeout, TimeUnit timeUnit) throws Exception {
        route.getRouteContext().getCamelContext().suspendRoute(route.getId(), timeout, timeUnit);
    }

    /**
     * @see #stopRouteAsync(Route)
     */
    public void stopRoute(Route route) throws Exception {
        route.getRouteContext().getCamelContext().stopRoute(route.getId());
    }

    /**
     * @see #stopRouteAsync(Route)
     */
    public void stopRoute(Route route, long timeout, TimeUnit timeUnit) throws Exception {
        route.getRouteContext().getCamelContext().stopRoute(route.getId(), timeout, timeUnit);
    }

    /**
     * Allows to stop a route asynchronously using a separate background thread which can allow any current in-flight exchange
     * to complete while the route is being shutdown.
     * You may attempt to stop a route from processing an exchange which would be in-flight and therefore attempting to stop
     * the route will defer due there is an inflight exchange in-progress. By stopping the route independently using a separate
     * thread ensures the exchange can continue process and complete and the route can be stopped.
     */
    public void stopRouteAsync(final Route route) {
        String threadId = route.getRouteContext().getCamelContext().getExecutorServiceManager().resolveThreadName("StopRouteAsync");
        Runnable task = () -> {
            try {
                route.getRouteContext().getCamelContext().stopRoute(route.getId());
            } catch (Exception e) {
                handleException(e);
            }
        };
        new Thread(task, threadId).start();
    }

    /**
     * Handles the given exception using the {@link #getExceptionHandler()}
     *
     * @param t the exception to handle
     */
    protected void handleException(Throwable t) {
        if (exceptionHandler != null) {
            exceptionHandler.handleException(t);
        }
    }

    @Override
    protected void doStart() throws Exception {
        // noop
    }

    @Override
    protected void doStop() throws Exception {
        // noop
    }

    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

}
