package org.asdtm.goodweather.task;

public class TaskOutput {
    ParseResult parseResult;
    TaskResult taskResult;

    public enum ParseResult {OK, PARSE_JSON_EXCEPTION}
    public enum TaskResult {SUCCESS, ERROR}
}
