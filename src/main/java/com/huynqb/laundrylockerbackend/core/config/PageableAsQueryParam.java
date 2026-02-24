package com.huynqb.laundrylockerbackend.core.config;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for Pageable parameters in Swagger documentation. Use this annotation on
 * controller methods that accept Pageable parameter to properly document pagination in Swagger UI.
 *
 * <p>Example usage:
 *
 * <pre>
 * &#64;GetMapping
 * &#64;PageableAsQueryParam
 * public Page&lt;User&gt; getUsers(&#64;Parameter(hidden = true) Pageable pageable) {
 *     return userService.findAll(pageable);
 * }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Parameter(
    in = ParameterIn.QUERY,
    description = "Page number (0-indexed)",
    name = "page",
    schema = @Schema(type = "integer", defaultValue = "0"))
@Parameter(
    in = ParameterIn.QUERY,
    description = "Number of records per page",
    name = "size",
    schema = @Schema(type = "integer", defaultValue = "20"))
@Parameter(
    in = ParameterIn.QUERY,
    description = "Sorting criteria in format: property,asc|desc. Example: createdAt,desc",
    name = "sort",
    array = @ArraySchema(schema = @Schema(type = "string", example = "createdAt,desc")))
public @interface PageableAsQueryParam {}
