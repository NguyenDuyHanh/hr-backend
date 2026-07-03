package com.tlu.hrm.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.tlu.hrm.model.User;

/**
 * Interface định nghĩa một AI Tool (Function Calling)
 */
public interface AiTool {
    /**
     * Tên của tool (khớp với LLM function name)
     */
    String getName();

    /**
     * Mô tả chức năng của tool
     */
    String getDescription();

    /**
     * JSON Schema định nghĩa các tham số của tool
     */
    String getParametersJson();

    /**
     * Thực thi logic của tool với các tham số nhận được và thông tin user hiện tại
     */
    String execute(JsonNode arguments, User currentUser);
}
