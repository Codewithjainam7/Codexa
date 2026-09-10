package com.codexa.common.error;

/**
 * Uniform error code constants returned across REST API error responses.
 */
public final class CodexaErrorCodes {

    private CodexaErrorCodes() {}

    public static final String JOB_NOT_FOUND = "JOB_NOT_FOUND";
    public static final String INVALID_INPUT = "INVALID_INPUT";
    public static final String EMPTY_FILE = "EMPTY_FILE";
    public static final String ZIP_SLIP_DETECTED = "ZIP_SLIP_DETECTED";
    public static final String ZIP_DEPTH_EXCEEDED = "ZIP_DEPTH_EXCEEDED";
    public static final String ZIP_BOMB_FILE_COUNT_EXCEEDED = "ZIP_BOMB_FILE_COUNT_EXCEEDED";
    public static final String RATE_LIMIT_EXCEEDED = "RATE_LIMIT_EXCEEDED";
    public static final String REPORT_GENERATION_FAILED = "REPORT_GENERATION_FAILED";
}
