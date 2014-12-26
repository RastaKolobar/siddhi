/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.query;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.config.QueryContext;
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.query.input.stream.StreamRuntime;
import org.wso2.siddhi.core.query.output.callback.OutputCallback;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.query.output.rateLimit.OutputRateLimiter;
import org.wso2.siddhi.core.query.selector.QuerySelector;
import org.wso2.siddhi.core.stream.StreamJunction;
import org.wso2.siddhi.core.util.parser.OutputParser;
import org.wso2.siddhi.core.util.parser.helper.QueryParserHelper;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.exception.DuplicateAnnotationException;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.input.stream.JoinInputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.SingleInputStream;
import org.wso2.siddhi.query.api.util.AnnotationHelper;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public class QueryRuntime {

    private StreamRuntime streamRuntime;
    private OutputRateLimiter outputRateLimiter;
    private String queryId;
    private Query query;
    private QueryContext queryContext;
    private OutputCallback outputCallback;
    private StreamDefinition outputStreamDefinition;
    private boolean toLocalStream;
    private QuerySelector selector;
    private MetaStateEvent metaStateEvent;

    public QueryRuntime(Query query, ExecutionPlanContext executionPlanContext, StreamRuntime streamRuntime, QuerySelector selector,
                        OutputRateLimiter outputRateLimiter, MetaStateEvent metaStateEvent) {
        this.query = query;
        this.streamRuntime = streamRuntime;
        this.selector = selector;

        this.queryContext = new QueryContext();
        queryContext.setExecutionPlanContext(executionPlanContext);

        setOutputRateLimiter(outputRateLimiter);
        setMetaStateEvent(metaStateEvent);
        setId();
        init();
    }

    private void setId() {
        try {
            Element element = AnnotationHelper.getAnnotationElement("info", "name", query.getAnnotations());
            if (element != null) {
                queryId = element.getValue();

            }
        } catch (DuplicateAnnotationException e) {
            throw new ExecutionPlanCreationException(e.getMessage() + " for the same Query " + query.toString());
        }
        if (queryId == null) {
            queryId = UUID.randomUUID().toString();
        }

    }

    public String getQueryId() {
        return queryId;
    }

    public void addCallback(QueryCallback callback) {
        outputRateLimiter.addQueryCallback(callback);
    }

    public OutputRateLimiter getOutputRateManager() {
        return outputRateLimiter;
    }

    public StreamDefinition getOutputStreamDefinition() {
        return outputStreamDefinition;
    }

    public List<String> getInputStreamId() {
        return query.getInputStream().getAllStreamIds();
    }

    public boolean isToLocalStream() {
        return toLocalStream;
    }

    public void setToLocalStream(boolean toLocalStream) {
        this.toLocalStream = toLocalStream;
    }

    public boolean isFromLocalStream() {
        if (query.getInputStream() instanceof SingleInputStream) {
            return ((SingleInputStream) query.getInputStream()).isInnerStream();
        } else if (query.getInputStream() instanceof JoinInputStream) {
            //TODO for join ,pattern and sequence streams
        }
        return false;
    }

    public QueryRuntime clone(String key, ConcurrentMap<String, StreamJunction> localStreamJunctionMap) {

        StreamRuntime clonedStreamRuntime = this.streamRuntime.clone(key);
        QuerySelector clonedSelector = this.selector.clone(key);
        OutputRateLimiter clonedOutputRateLimiter = outputRateLimiter.clone(key);

        QueryRuntime queryRuntime = new QueryRuntime(query, queryContext.getExecutionPlanContext(), clonedStreamRuntime, clonedSelector,
                clonedOutputRateLimiter, this.metaStateEvent);
        QueryParserHelper.initStreamRuntime(clonedStreamRuntime, metaStateEvent);

        queryRuntime.queryId = this.queryId + key;
        queryRuntime.setToLocalStream(toLocalStream);

        if (!toLocalStream) {
            queryRuntime.outputRateLimiter.setOutputCallback(outputCallback);
            queryRuntime.outputCallback = this.outputCallback;
        } else {
            OutputCallback clonedQueryOutputCallback = OutputParser.constructOutputCallback(query.getOutputStream(), key,
                    localStreamJunctionMap, outputStreamDefinition, queryContext.getExecutionPlanContext());
            queryRuntime.outputRateLimiter.setOutputCallback(clonedQueryOutputCallback);
            queryRuntime.outputCallback = clonedQueryOutputCallback;
        }
        return queryRuntime;

    }

    private void setOutputRateLimiter(OutputRateLimiter outputRateLimiter) {
        this.outputRateLimiter = outputRateLimiter;
        selector.setNextProcessor(outputRateLimiter);
    }

    public StreamRuntime getStreamRuntime() {
        return streamRuntime;
    }

    public MetaStateEvent getMetaStateEvent() {
        return metaStateEvent;
    }

    private void setMetaStateEvent(MetaStateEvent metaStateEvent) {
        outputStreamDefinition = metaStateEvent.getOutputStreamDefinition();
        this.metaStateEvent = metaStateEvent;
    }

    public Query getQuery() {
        return query;
    }

    public void setOutputCallback(OutputCallback outputCallback) {
        this.outputCallback = outputCallback;
        outputRateLimiter.setOutputCallback(outputCallback);
    }

    public void init() {
        streamRuntime.setCommonProcessor(selector);
    }

    public QuerySelector getSelector() {
        return selector;
    }
}
