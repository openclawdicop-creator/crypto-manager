package org.acme.resource;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    public boolean error;
    public String message;
    public T data;
    public PageMetadata pagination;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.error = false;
        response.message = "Operação realizada com sucesso";
        response.data = data;
        return response;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.error = false;
        response.message = message;
        response.data = data;
        return response;
    }

    public static <T> ApiResponse<T> successWithPagination(T data, PageMetadata pagination) {
        ApiResponse<T> response = success(data);
        response.pagination = pagination;
        return response;
    }

    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.error = true;
        response.message = message;
        return response;
    }

    public static class PageMetadata {
        public int page;
        public int size;
        public long totalElements;
        public int totalPages;

        public PageMetadata(int page, int size, long totalElements, int totalPages) {
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
        }
    }
}
