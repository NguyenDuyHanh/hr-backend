package com.tlu.hrm.service;

import com.tlu.hrm.dto.response.TaskHistoryResponse;

import java.util.List;
import java.util.UUID;

public interface TaskHistoryService {
    List<TaskHistoryResponse> getHistoryByTaskId(UUID taskId);
    TaskHistoryResponse addComment(UUID taskId, String comment);
    TaskHistoryResponse updateComment(UUID id, String comment);
    void deleteComment(UUID id);
    void logEvent(UUID taskId, String event);
}
