package com.tlu.hrm.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tlu.hrm.dto.request.AiChatRequest;
import com.tlu.hrm.model.Department;
import com.tlu.hrm.model.Staff;
import com.tlu.hrm.repository.DepartmentRepository;
import com.tlu.hrm.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
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
public class AiController {

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${groq.api.key:}")
    private String defaultApiKey;

    @PostMapping
    public ResponseEntity<StreamingResponseBody> chat(@RequestBody AiChatRequest request) {
        String apiKey = (request.getApiKey() != null && !request.getApiKey().trim().isEmpty()) 
                ? request.getApiKey().trim() : defaultApiKey;
                
        String model = (request.getModel() != null && !request.getModel().trim().isEmpty()) 
                ? request.getModel().trim() : "llama-3.3-70b-versatile";

        StreamingResponseBody responseBody = outputStream -> {
            try {
                if (apiKey == null || apiKey.trim().isEmpty()) {
                    throw new RuntimeException("API Key is missing");
                }

                // 1. Build a clean, token-efficient system prompt instructing AI on tool usage
                String systemPrompt = buildSystemPrompt();

                // 2. Define the tools array metadata for getStaffProfile and getEmployeeCount
                String toolsJson = """
                [
                  {
                    "type": "function",
                    "function": {
                      "name": "getStaffProfile",
                      "description": "Lấy thông tin hồ sơ chi tiết của một nhân viên cụ thể theo mã số nhân viên (staffCode).",
                      "parameters": {
                        "type": "object",
                        "properties": {
                          "staffCode": {
                            "type": "string",
                            "description": "Mã số nhân viên cần tra cứu (ví dụ: NV001, NV002)."
                          }
                        },
                        "required": ["staffCode"]
                      }
                    }
                  },
                  {
                    "type": "function",
                    "function": {
                      "name": "getEmployeeCount",
                      "description": "Lấy tổng số lượng nhân viên/nhân sự hiện có trong toàn hệ thống quản lý.",
                      "parameters": {
                        "type": "object",
                        "properties": {}
                      }
                    }
                  }
                ]
                """;

                // 3. Build payload for Phase 1 (Non-streaming to detect tool call)
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

                            // Execute local Java tool execution
                            String toolResult = "";
                            System.out.println("\n--- [AI TOOL CALL DETECTED] ---");
                            System.out.println("-> Tool Name: " + funcName);
                            System.out.println("-> Arguments: " + argumentsStr);
                             
                            if ("getStaffProfile".equals(funcName)) {
                                JsonNode argsNode = objectMapper.readTree(argumentsStr);
                                String staffCode = argsNode.has("staffCode") ? argsNode.get("staffCode").asText() : "";
                                System.out.println("-> Executing local method: getStaffProfile(\"" + staffCode + "\")");
                                toolResult = executeGetStaffProfile(staffCode);
                            } else if ("getEmployeeCount".equals(funcName)) {
                                System.out.println("-> Executing local method: getEmployeeCount()");
                                toolResult = executeGetEmployeeCount();
                            }
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
                    
                    // If no tool call, stream the content of the messageNode directly with smooth typing simulation
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
                // FALLBACK: If anything fails, trigger the robust local query handler
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

    private String executeGetStaffProfile(String staffCode) {
        if (staffCode == null || staffCode.trim().isEmpty()) {
            return "{\"error\": \"Mã nhân viên không hợp lệ\"}";
        }
        
        List<Staff> staffs = staffRepository.findAll();
        Staff foundStaff = null;
        for (Staff s : staffs) {
            if (s.getStaffCode() != null && s.getStaffCode().trim().equalsIgnoreCase(staffCode.trim())) {
                foundStaff = s;
                break;
            }
        }
        
        if (foundStaff == null) {
            return "{\"status\": \"error\", \"message\": \"Không tìm thấy nhân viên với mã: " + staffCode + "\"}";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"staffCode\":\"").append(escapeJson(foundStaff.getStaffCode())).append("\",");
        sb.append("\"displayName\":\"").append(escapeJson(foundStaff.getDisplayName())).append("\",");
        sb.append("\"email\":\"").append(escapeJson(foundStaff.getEmail())).append("\",");
        sb.append("\"phoneNumber\":\"").append(escapeJson(foundStaff.getPhoneNumber())).append("\",");
        sb.append("\"gender\":\"").append(foundStaff.getGender() != null ? escapeJson(foundStaff.getGender().name()) : "").append("\",");
        sb.append("\"workingStatus\":\"").append(foundStaff.getWorkingStatus() != null ? escapeJson(foundStaff.getWorkingStatus().name()) : "").append("\",");
        sb.append("\"birthDate\":\"").append(foundStaff.getBirthDate() != null ? foundStaff.getBirthDate().toString() : "—").append("\",");
        sb.append("\"startDate\":\"").append(foundStaff.getStartDate() != null ? foundStaff.getStartDate().toString() : "—").append("\",");
        sb.append("\"idNumber\":\"").append(foundStaff.getIdNumber() != null ? escapeJson(foundStaff.getIdNumber()) : "—").append("\",");
        sb.append("\"currentAddress\":\"").append(foundStaff.getCurrentAddress() != null ? escapeJson(foundStaff.getCurrentAddress()) : "—").append("\",");
        sb.append("\"socialInsuranceCode\":\"").append(foundStaff.getSocialInsuranceCode() != null ? escapeJson(foundStaff.getSocialInsuranceCode()) : "—").append("\",");
        sb.append("\"department\":\"").append(foundStaff.getDepartment() != null ? escapeJson(foundStaff.getDepartment().getName()) : "—").append("\",");
        sb.append("\"position\":\"").append(foundStaff.getPosition() != null ? escapeJson(foundStaff.getPosition().getName()) : "—").append("\"");
        sb.append("}");
        return sb.toString();
    }

    private String executeGetEmployeeCount() {
        long count = staffRepository.count();
        return "{\"status\": \"success\", \"totalEmployees\": " + count + "}";
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

            StringBuilder staffTable = new StringBuilder();
            staffTable.append("| Mã NV | Họ và tên | Phòng ban | Chức danh | Email | Trạng thái |\n|---|---|---|---|---|---|\n");
            for (Staff s : staffRepository.findAll()) {
                String deptName = s.getDepartment() != null ? s.getDepartment().getName() : "—";
                String posName = s.getPosition() != null ? s.getPosition().getName() : "—";
                String email = s.getEmail() != null ? s.getEmail() : "—";
                staffTable.append("| ").append(s.getStaffCode()).append(" | ").append(s.getDisplayName()).append(" | ").append(deptName).append(" | ").append(posName).append(" | ").append(email).append(" | ").append(s.getWorkingStatus() != null ? s.getWorkingStatus().name() : "—").append(" |\n");
            }

            StringBuilder responseText = new StringBuilder();
            responseText.append("*(Hệ thống đang hoạt động ở chế độ **Tra cứu Trực tiếp**)*\n\n");

            if (lastQuery.contains("hello") || lastQuery.contains("chào") || lastQuery.contains("hi")) {
                responseText.append("👋 **Xin chào!** Tôi là Trợ lý AI HRM.\n\n");
                responseText.append("Hiện tại hệ thống đã chuẩn bị sẵn sàng dữ liệu của **").append(totalDepts).append(" Phòng ban** và **").append(totalStaffs).append(" Nhân sự**.\n\n");
                responseText.append("💡 **Hướng dẫn tra cứu nhanh**:\n");
                responseText.append("- Gõ **'phòng ban'** để xem toàn bộ cơ cấu tổ chức.\n");
                responseText.append("- Gõ **'nhân viên'** để xem danh sách hồ sơ chi tiết.\n");
                responseText.append("- Hoặc nhấp vào biểu tượng cài đặt ⚙️ ở trên để cấu hình API Key giúp AI đàm thoại tự nhiên!");
            } else if (lastQuery.contains("phòng ban") || lastQuery.contains("cơ cấu")) {
                responseText.append("🏢 **DANH SÁCH PHÒNG BAN HIỆN CÓ**:\n\n");
                responseText.append(deptTable.toString());
            } else if (lastQuery.contains("nhân viên") || lastQuery.contains("nhân sự") || lastQuery.contains("toàn bộ")) {
                responseText.append("👥 **DANH SÁCH NHÂN SỰ TOÀN BỘ**:\n\n");
                responseText.append(staffTable.toString());
            } else {
                responseText.append("📌 **Kết quả cho từ khóa:** '").append(lastQuery).append("'\n\n");
                responseText.append("Hệ thống hiện quản lý **").append(totalStaffs).append(" nhân sự** và **").append(totalDepts).append(" phòng ban**. Vui lòng gõ cụ thể **'phòng ban'** hoặc **'nhân viên'** để xuất bảng dữ liệu tương ứng.");
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
        sb.append("Nhiệm vụ của bạn là hỗ trợ tra cứu thông tin nhân sự và số lượng nhân sự trong hệ thống. Bạn được cung cấp các công cụ (tools) chuyên dụng:\n");
        sb.append("- Sử dụng 'getStaffProfile' khi người dùng muốn xem thông tin chi tiết, email, số điện thoại, phòng ban, chức danh của một nhân sự cụ thể theo mã nhân viên.\n");
        sb.append("- Sử dụng 'getEmployeeCount' khi người dùng muốn biết tổng số lượng nhân sự hiện có trong toàn hệ thống.\n");
        sb.append("Khi thực hiện gọi công cụ, hãy điền đúng tham số và không thêm bất kỳ văn bản giải thích nào ngoài lệnh gọi công cụ.\n");
        sb.append("Khi trả lời người dùng, hãy trình bày bằng tiếng Việt, định dạng Markdown chuyên nghiệp, trực quan, có cấu trúc rõ ràng.\n");
        sb.append("HÃY TRẢ LỜI TẬP TRUNG VÀ ĐÚNG TRỌNG TÂM: Nếu người dùng chỉ hỏi một vài thông tin cụ thể của đối tượng, hãy chỉ trả lời trực tiếp các thông tin được hỏi đó. Không tự ý hiển thị toàn bộ hồ sơ hoặc chi tiết đối tượng trừ khi người dùng yêu cầu xem chi tiết.\n");
        sb.append("ĐỐI VỚI DỮ LIỆU RỖNG HOẶC KHÔNG TỒN TẠI:\n");
        sb.append("- Nếu đối tượng (nhân viên, phòng ban, v.v.) có tồn tại nhưng trường thông tin cụ thể được hỏi trả về giá trị trống hoặc rỗng, hãy trả lời lịch sự rằng thông tin này 'chưa được cập nhật trên hệ thống'.\n");
        sb.append("- Chỉ phản hồi không tìm thấy đối tượng nếu mã hoặc thông tin định danh của đối tượng đó hoàn toàn không tồn tại trong cơ sở dữ liệu.\n");
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
