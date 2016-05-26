package org.zalando.problem.spring.web.advice.http;

/*
 * #%L
 * problem-handling
 * %%
 * Copyright (C) 2015 Zalando SE
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.spring.web.advice.AdviceTrait;

import javax.ws.rs.core.Response.Status;

/**
 * @see HttpMediaTypeNotSupportedException
 * @see Status#UNSUPPORTED_MEDIA_TYPE
 */
public interface UnsupportedMediaTypeAdviceTrait extends AdviceTrait {

    @ExceptionHandler
    default ResponseEntity<Problem> handleMediaTypeNotSupportedException(
            final HttpMediaTypeNotSupportedException exception,
            final NativeWebRequest request) throws HttpMediaTypeNotAcceptableException {

        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(exception.getSupportedMediaTypes());

        return create(Status.UNSUPPORTED_MEDIA_TYPE, exception, request, headers);
    }

}