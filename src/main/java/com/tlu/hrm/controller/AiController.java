package com.tlu.hrm.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tlu.hrm.dto.request.AiChatRequest;
import com.tlu.hrm.model.Department;
import com.tlu.hrm.model.Staff;
import com.tlu.hrm.model.User;
import com.tlu.hrm.repository.DepartmentRepository;
import com.tlu.hrm.repository.StaffRepository;
import com.tlu.hrm.security.SecurityUtils;
import com.tlu.hrm.service.ai.impl.AiServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
@PreAuthorize("isAuthenticated()")
public class AiController {

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private AiServiceImpl aiService;

    @Autowired
    private SecurityUtils securityUtils;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${groq.api.key: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}")
    private String defaultApiKey;

    @PostMapping
    public ResponseEntity<StreamingResponseBody> chat(@RequestBody AiChatRequest request) {
        final String apiKey = (defaultApiKey == null || defaultApiKey.trim().isEmpty()) 
                ? "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" : defaultApiKey;
                
        String model = (request.getModel() != null && !request.getModel().trim().isEmpty()) 
                ? request.getModel().trim() : "llama-3.3-70b-versatile";

        User currentUser = securityUtils.getCurrentUser();

        StreamingResponseBody responseBody = outputStream -> {
            try {
                if (apiKey == null || apiKey.trim().isEmpty()) {
                    throw new RuntimeException("API Key is missing");
                }

                // 1. Build dynamic system prompt
                String systemPrompt = buildSystemPrompt();

                // 2. Fetch tool configurations dynamically from service
                String toolsJson = aiService.buildToolsJsonMetadata();

                // 3. Build payload for Phase 1
                StringBuilder payloadBuilder = new StringBuilder();
                payloadBuilder.append("{\"model\":\"").append(escapeJson(model)).append("\",\"messages\":[");
                
                // Add System message
                payloadBuilder.append("{\"role\":\"system\",\"content\":\"").append(escapeJson(systemPrompt)).append("\"}");

                // Add history messages
                if (request.getMessages() != null) {
                    for (AiChatRequest.ChatMessage msg : request.getMessages()) {
                        String role = msg.getRole() != null ? msg.getRole() : "user";
                        String content = msg.getContent() != null ? msg.getContent() : "";
                        payloadBuilder.append(",{\"role\":\"").append(escapeJson(role))
                                      .append("\",\"content\":\"").append(escapeJson(content)).append("\"}");
                    }
                }
                payloadBuilder.append("],");
                payloadBuilder.append("\"tools\":").append(toolsJson).append(",");
                payloadBuilder.append("\"tool_choice\":\"auto\",");
                payloadBuilder.append("\"temperature\":0.0}");

                String phase1Payload = payloadBuilder.toString();

                // Send non-streaming request
                HttpRequest phase1Request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                        .timeout(java.time.Duration.ofSeconds(10))
                        .header("Authorization", "Bearer " + apiKey)
                        .header("Content-Type", "application/json")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                        .POST(HttpRequest.BodyPublishers.ofString(phase1Payload, StandardCharsets.UTF_8))
                        .build();

                HttpClient httpClient = HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1)
                        .connectTimeout(java.time.Duration.ofSeconds(5))
                        .build();

                HttpResponse<String> phase1Response = httpClient.send(phase1Request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

                if (phase1Response.statusCode() != 200) {
                    throw new RuntimeException("HTTP " + phase1Response.statusCode() + ": " + phase1Response.body());
                }

                String responseBodyStr = phase1Response.body();
                JsonNode root = objectMapper.readTree(responseBodyStr);
                JsonNode choices = root.get("choices");
                
                if (choices != null && choices.isArray() && choices.size() > 0) {
                    JsonNode firstChoice = choices.get(0);
                    JsonNode messageNode = firstChoice.get("message");
                    
                    if (messageNode != null && messageNode.has("tool_calls")) {
                        JsonNode toolCalls = messageNode.get("tool_calls");
                        if (toolCalls != null && toolCalls.isArray() && toolCalls.size() > 0) {
                            JsonNode toolCall = toolCalls.get(0);
                            String callId = toolCall.get("id").asText();
                            String funcName = toolCall.get("function").get("name").asText();
                            String argumentsStr = toolCall.get("function").get("arguments").asText();

                            // Execute AI tool through registration service
                            System.out.println("\n--- [AI TOOL CALL DETECTED] ---");
                            System.out.println("-> Tool Name: " + funcName);
                            System.out.println("-> Arguments: " + argumentsStr);
                            
                            String toolResult = aiService.executeTool(funcName, argumentsStr, currentUser);
                            
                            System.out.println("-> Result Returned to AI: " + toolResult);
                            System.out.println("--------------------------------\n");

                            // Phase 2: Build the stream payload with tool execution results
                            StringBuilder streamPayloadBuilder = new StringBuilder();
                            streamPayloadBuilder.append("{\"model\":\"").append(escapeJson(model)).append("\",\"stream\":true,\"messages\":[");
                            
                            // 1. System Prompt
                            streamPayloadBuilder.append("{\"role\":\"system\",\"content\":\"").append(escapeJson(systemPrompt)).append("\"}");
                            
                            // 2. Chat history
                            if (request.getMessages() != null) {
                                for (AiChatRequest.ChatMessage msg : request.getMessages()) {
                                    String role = msg.getRole() != null ? msg.getRole() : "user";
                                    String content = msg.getContent() != null ? msg.getContent() : "";
                                    streamPayloadBuilder.append(",{\"role\":\"").append(escapeJson(role))
                                                        .append("\",\"content\":\"").append(escapeJson(content)).append("\"}");
                                }
                            }
                            
                            // 3. Assistant Tool Call Message
                            streamPayloadBuilder.append(",{\"role\":\"assistant\",\"content\":null,\"tool_calls\":[")
                                                .append("{\"id\":\"").append(escapeJson(callId)).append("\",")
                                                .append("\"type\":\"function\",")
                                                .append("\"function\":{")
                                                .append("\"name\":\"").append(escapeJson(funcName)).append("\",")
                                                .append("\"arguments\":\"").append(escapeJson(argumentsStr)).append("\"")
                                                .append("}}")
                                                .append("]}");
                                                
                            // 4. Tool Output Message
                            streamPayloadBuilder.append(",{\"role\":\"tool\",")
                                                .append("\"tool_call_id\":\"").append(escapeJson(callId)).append("\",")
                                                .append("\"name\":\"").append(escapeJson(funcName)).append("\",")
                                                .append("\"content\":\"").append(escapeJson(toolResult)).append("\"")
                                                .append("}");
                                                
                            streamPayloadBuilder.append("]}");
                            
                            executeStreamingCall(streamPayloadBuilder.toString(), apiKey, outputStream);
                            return;
                        }
                    }
                    
                    // If no tool call, stream the content directly
                    String directContent = (messageNode != null && messageNode.has("content") && !messageNode.get("content").isNull()) 
                            ? messageNode.get("content").asText() : "";
                    
                    if (!directContent.isEmpty()) {
                        String escapedContent = escapeJson(directContent);
                        String[] words = escapedContent.split(" ");
                        for (int i = 0; i < words.length; i++) {
                            String suffix = (i == words.length - 1) ? "" : " ";
                            outputStream.write(("0:\"" + words[i] + suffix + "\"\n").getBytes(StandardCharsets.UTF_8));
                            outputStream.flush();
                            try { Thread.sleep(25); } catch (InterruptedException ignored) {}
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("=== [AI CHAT ERROR] ===");
                e.printStackTrace();
                executeLocalFallback(request, outputStream);
            }
        };

        return ResponseEntity.ok()
                .header("Content-Type", "text/plain; charset=utf-8")
                .header("X-Accel-Buffering", "no")
                .header("Cache-Control", "no-cache")
                .body(responseBody);
    }

    private void executeStreamingCall(String jsonPayload, String apiKey, OutputStream outputStream) throws Exception {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                .timeout(java.time.Duration.ofSeconds(10))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .header("Accept", "text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                .build();

        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(java.time.Duration.ofSeconds(5))
                .build();
                
        HttpResponse<InputStream> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());

        if (httpResponse.statusCode() != 200) {
            String errBody = new String(httpResponse.body().readAllBytes(), StandardCharsets.UTF_8);
            throw new RuntimeException("HTTP " + httpResponse.statusCode() + ": " + errBody);
        }

        try (InputStreamReader isr = new InputStreamReader(httpResponse.body(), StandardCharsets.UTF_8)) {
            StringBuilder lineBuffer = new StringBuilder();
            int c;
            while ((c = isr.read()) != -1) {
                char ch = (char) c;
                lineBuffer.append(ch);
                
                if (ch == '\n') {
                    String line = lineBuffer.toString().trim();
                    lineBuffer.setLength(0);
                    
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6).trim();
                        if ("[DONE]".equals(data)) break;
                        
                        String target = "\"content\":\"";
                        int idx = data.indexOf(target);
                        if (idx == -1) {
                            target = "\"content\": \"";
                            idx = data.indexOf(target);
                        }
                        
                        if (idx != -1) {
                            int start = idx + target.length() - 1;
                            int end = -1;
                            boolean escaped = false;
                            for (int i = start + 1; i < data.length(); i++) {
                                char curr = data.charAt(i);
                                if (escaped) {
                                    escaped = false;
                                } else if (curr == '\\') {
                                    escaped = true;
                                } else if (curr == '"') {
                                    end = i;
                                    break;
                                }
                            }
                            
                            if (end != -1) {
                                String jsonStringLiteral = data.substring(start, end + 1);
                                outputStream.write(("0:" + jsonStringLiteral + "\n").getBytes(StandardCharsets.UTF_8));
                                outputStream.flush();
                                
                                try { Thread.sleep(25); } catch (InterruptedException ignored) {}
                            }
                        }
                    }
                }
            }
        }
    }

    private void executeLocalFallback(AiChatRequest request, OutputStream outputStream) {
        try {
            String lastQuery = "";
            if (request.getMessages() != null && !request.getMessages().isEmpty()) {
                String raw = request.getMessages().get(request.getMessages().size() - 1).getContent();
                if (raw != null) lastQuery = raw.trim().toLowerCase();
            }

            int totalDepts = (int) departmentRepository.count();
            int totalStaffs = (int) staffRepository.count();

            StringBuilder deptTable = new StringBuilder();
            deptTable.append("| Mã PB | Tên Phòng ban | Mô tả |\n|---|---|---|\n");
            for (Department d : departmentRepository.findAll()) {
                deptTable.append("| ").append(d.getCode()).append(" | ").append(d.getName()).append(" | ").append(d.getDescription() != null ? d.getDescription() : "—").append(" |\n");
            }

            StringBuilder responseText = new StringBuilder();
            responseText.append("*(Hệ thống đang hoạt động ở chế độ **Tra cứu Trực tiếp** do API AI bận)*\n\n");

            if (lastQuery.contains("hello") || lastQuery.contains("chào") || lastQuery.contains("hi")) {
                responseText.append("👋 **Xin chào!** Tôi là Trợ lý AI HRM.\n\n");
                responseText.append("Hiện tại hệ thống đã chuẩn bị sẵn sàng dữ liệu của **").append(totalDepts).append(" Phòng ban** và **").append(totalStaffs).append(" Nhân sự**.\n\n");
                responseText.append("💡 **Hướng dẫn tra cứu nhanh**:\n");
                responseText.append("- Gõ **'phòng ban'** để xem toàn bộ cơ cấu tổ chức.\n");
                responseText.append("- Gõ **'nhân viên'** để tìm kiếm.\n");
            } else if (lastQuery.contains("phòng ban") || lastQuery.contains("cơ cấu")) {
                responseText.append("🏢 **DANH SÁCH PHÒNG BAN HIỆN CÓ**:\n\n");
                responseText.append(deptTable.toString());
            } else {
                responseText.append("📌 **Kết quả cho từ khóa:** '").append(lastQuery).append("'\n\n");
                responseText.append("Hệ thống hiện quản lý **").append(totalStaffs).append(" nhân sự** và **").append(totalDepts).append(" phòng ban**.");
            }

            String safeToken = escapeJson(responseText.toString());
            outputStream.write(("0:\"" + safeToken + "\"\n").getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        } catch (Exception ignored) {}
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    private String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("Bạn là Trợ lý AI của hệ thống Quản lý Nhân sự (HRM).\n");
        sb.append("Nhiệm vụ của bạn là hỗ trợ tra cứu thông tin nhân sự, phòng ban, công việc/dự án, nghỉ phép, phiếu lương trong hệ thống. Bạn được cung cấp các công cụ (tools) chuyên dụng:\n");
        sb.append("- Sử dụng 'getStaffProfile' khi muốn xem thông tin hồ sơ chi tiết của một nhân viên cụ thể theo mã số nhân viên.\n");
        sb.append("- Sử dụng 'getEmployeeCount' khi muốn biết tổng số lượng nhân sự trong hệ thống.\n");
        sb.append("- Sử dụng 'searchStaff' khi muốn tìm kiếm, lọc danh sách nhân viên theo tên, phòng ban (ví dụ: IT, Nhân sự), chức danh.\n");
        sb.append("- Sử dụng 'getDepartments' khi muốn xem danh sách các phòng ban hiện có.\n");
        sb.append("- Sử dụng 'getMyProjects' khi người dùng muốn xem danh sách các dự án tham gia.\n");
        sb.append("- Sử dụng 'getMyTasks' khi muốn xem các công việc/nhiệm vụ (có thể lọc theo dự án).\n");
        sb.append("- Sử dụng 'getMyLeaveRequests' khi muốn xem lịch sử hoặc danh sách đơn nghỉ phép.\n");
        sb.append("- Sử dụng 'getMyLeaveBalance' khi muốn xem số dư ngày nghỉ phép năm còn lại.\n");
        sb.append("- Sử dụng 'getMyPayslips' khi người dùng muốn tra cứu phiếu lương/lịch sử nhận lương.\n");
        sb.append("- Sử dụng 'getMyTimesheet' khi muốn biết số ngày đi làm, số công hoặc giờ tăng ca thực tế của chính họ trong tháng.\n");
        sb.append("- Sử dụng 'getMyProfile' khi người dùng muốn xem thông tin cá nhân của chính họ (ví dụ: tôi thuộc phòng ban nào, mã nhân viên của tôi là gì, xem hồ sơ của tôi, tôi là ai).\n");
        sb.append("- Sử dụng 'getRecruitments' khi muốn xem danh sách tin tuyển dụng, vị trí đang tuyển trong hệ thống.\n");
        sb.append("- Sử dụng 'getCandidates' khi muốn xem danh sách ứng viên tuyển dụng, thông tin chi tiết ứng viên hoặc lọc theo tin tuyển dụng.\n");
        sb.append("- Sử dụng 'getDashboardSummary' khi muốn xem dữ liệu tổng quan dashboard (tổng nhân sự, phòng ban, dự án,...).\n");
        sb.append("- Sử dụng 'getStaffTimesheet' khi muốn tra cứu thông tin chấm công, đi muộn về sớm của nhân viên khác (dành cho quản lý/admin).\n");
        sb.append("- Sử dụng 'getPayrollSummary' khi quản lý muốn xem thông tin tổng quan về các bảng lương và phiếu lương chưa thanh toán.\n");
        sb.append("- Sử dụng 'getPositions' khi muốn xem các chức danh/vị trí công việc đang có trong hệ thống.\n");
        sb.append("- Sử dụng 'getStaffSalaryItems' khi muốn tra cứu mức lương cơ bản hoặc phụ cấp của nhân viên.\n\n");
        sb.append("Khi thực hiện gọi công cụ, hãy điền đúng tham số và không thêm bất kỳ văn bản giải thích nào ngoài lệnh gọi công cụ.\n");
        sb.append("Quy tắc cấu trúc câu trả lời:\n");
        sb.append("1. Tuyệt đối KHÔNG được viết tên của công cụ/hàm kỹ thuật (ví dụ: 'getEmployeeCount', 'getMyProfile', 'searchStaff', 'getMyPayslips', 'tool', 'function'...) ở đầu hoặc bất cứ đâu trong câu trả lời.\n");
        sb.append("2. Tuyệt đối KHÔNG tự tạo các tiêu đề rập khuôn dựa trên tên công cụ (ví dụ: 'Tổng Số Nhân Viên Công Ty:', 'Thông Tin Cá Nhân:', 'Kết quả tìm kiếm:',...).\n");
        sb.append("3. Tuyệt đối KHÔNG được lặp lại lệnh gọi công cụ kèm tham số dưới dạng chữ (ví dụ: 'getMyTimesheet{\"month\":7,\"year\":2026}', 'getMyProfile{}',...) ở đầu hoặc bất cứ đâu trong câu trả lời.\n");
        sb.append("4. Hãy đi thẳng vào nội dung câu trả lời một cách tự nhiên, ngắn gọn và mạch lạc bằng tiếng Việt như một con người thực sự đang trò chuyện.\n");
        sb.append("Lưu ý về bảo mật (RBAC): Các công cụ của bạn đã được thiết lập để tự động lọc kết quả phù hợp với quyền hạn của tài khoản đang trò chuyện. Hãy tự tin trả lời dựa trên kết quả trả về từ công cụ.\n");
        sb.append("Nếu kết quả trả về từ công cụ (tool output) có chứa thông báo lỗi, thất bại hoặc không có quyền (ví dụ: 'status': 'error', 'message': 'Bạn không có quyền...'), bạn BẮT BUỘC phải thông báo rõ ràng cho người dùng rằng họ không có quyền truy cập thông tin này. Tuyệt đối KHÔNG được tự ý sáng tạo hay bịa đặt ra số liệu giả lập để thay thế trong trường hợp bị từ chối quyền truy cập.\n");
        sb.append("Tuyệt đối KHÔNG tự sáng tạo hoặc bịa đặt ra bất kỳ thông tin và số liệu giả nào.\n\n");

        // Inject Real-time Clock Info dynamically
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String dayOfWeekStr = "";
        switch (now.getDayOfWeek()) {
            case MONDAY: dayOfWeekStr = "Thứ Hai"; break;
            case TUESDAY: dayOfWeekStr = "Thứ Ba"; break;
            case WEDNESDAY: dayOfWeekStr = "Thứ Tư"; break;
            case THURSDAY: dayOfWeekStr = "Thứ Năm"; break;
            case FRIDAY: dayOfWeekStr = "Thứ Sáu"; break;
            case SATURDAY: dayOfWeekStr = "Thứ Bảy"; break;
            case SUNDAY: dayOfWeekStr = "Chủ Nhật"; break;
        }
        sb.append("=== THÔNG TIN HỆ THỐNG THỜI GIAN THỰC ===\n");
        sb.append("- Thời gian hiện tại: ").append(now.format(formatter)).append("\n");
        sb.append("- Ngày trong tuần: ").append(dayOfWeekStr).append("\n\n");

        return sb.toString();
    }
}
