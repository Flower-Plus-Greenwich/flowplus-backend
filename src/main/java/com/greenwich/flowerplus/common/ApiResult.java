package com.greenwich.flowerplus.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;


@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResult<T> {

    private T data;
    private String message;
    private Pagination pagination;
    private ErrorDetail error;

    @Getter
    @Builder
    public static class Pagination {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
    }

    @Getter
    @Builder
    public static class ErrorDetail {
        private String code;
        private String message;
        private String traceId;
        private Object details;
    }

    public static <T> ApiResult<T> success(T data) {
        return ApiResult.<T>builder().data(data).build();
    }

    public static <T> ApiResult<T> success(T data, String message) {
        return ApiResult.<T>builder().data(data).message(message).build();
    }

    public static <T> ApiResult<T> success() {
        return ApiResult.<T>builder().build();
    }

    public static <T> ApiResult<List<T>> success(List<T> items, int page, int size, long total) {
        return ApiResult.<List<T>>builder()
                .data(items)
                .pagination(Pagination.builder()
                        .page(page)
                        .size(size)
                        .totalElements(total)
                        .totalPages((int) Math.ceil((double) total / size))
                        .build())
                .build();
    }

    public static ApiResult<?> error(String code, String message, String traceId) {
        return error(code, message, traceId, null);
    }

    public static ApiResult<?> error(String code, String message, String traceId, Object details) {
        return ApiResult.builder()
                .error(ErrorDetail.builder()
                        .code(code)
                        .message(message)
                        .traceId(traceId)
                        .details(details)
                        .build())
                .build();
    }
}