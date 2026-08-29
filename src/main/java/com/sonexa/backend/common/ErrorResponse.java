package com.sonexa.backend.common;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String errCode,
        String errDesc,
        String field
) {
    public static ErrorResponseBuilder builder() {
        return new ErrorResponseBuilder();
    }

    public static class ErrorResponseBuilder {
        private String errCode;
        private String errDesc;
        private String field;

        public ErrorResponseBuilder errCode(String errCode) {
            this.errCode = errCode;
            return this;
        }

        public ErrorResponseBuilder errDesc(String errDesc) {
            this.errDesc = errDesc;
            return this;
        }

        public ErrorResponseBuilder field(String field) {
            this.field = field;
            return this;
        }

        public ErrorResponse build() {
            return new ErrorResponse(errCode, errDesc, field);
        }
    }
}
