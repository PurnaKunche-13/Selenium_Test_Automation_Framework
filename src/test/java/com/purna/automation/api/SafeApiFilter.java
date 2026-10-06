package com.purna.automation.api;

import com.purna.automation.utilities.JsonUtils;
import com.purna.automation.utilities.SafeDiagnostics;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

/** Captures structural evidence only; never logs headers, cookies or raw payload values. */
public final class SafeApiFilter implements Filter {
    @Override public Response filter(FilterableRequestSpecification request,
                                    FilterableResponseSpecification response, FilterContext context) {
        long started = System.nanoTime();
        Object body = request.getBody();
        String requestShape = JsonUtils.structure(body == null ? "" : body instanceof String text ? text : JsonUtils.toJson(body));
        String location = SafeDiagnostics.location(request.getURI());
        try {
            Response result = context.next(request, response);
            ApiEvidence.record(new ApiEvidence.Exchange(request.getMethod(), location, result.statusCode(),
                    (System.nanoTime() - started) / 1_000_000,
                    SafeDiagnostics.contentType(result.contentType()), requestShape,
                    JsonUtils.structure(result.asString()), ""));
            return result;
        } catch (RuntimeException failure) {
            ApiEvidence.record(new ApiEvidence.Exchange(request.getMethod(), location, 0,
                    (System.nanoTime() - started) / 1_000_000, "[unavailable]", requestShape,
                    "[no response]", failure.getClass().getName()));
            throw failure;
        }
    }
}
