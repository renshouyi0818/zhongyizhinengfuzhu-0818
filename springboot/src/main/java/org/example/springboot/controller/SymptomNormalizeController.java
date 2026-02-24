package org.example.springboot.controller;

import org.example.springboot.common.Result;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/symptom")
public class SymptomNormalizeController {

    private static final String PYTHON_API = "http://localhost:9001/normalize";

    @PostMapping("/normalize")
    public Result<String> normalize(@RequestBody Map<String, String> body) {
        String symptom = body.get("symptom");
        if (symptom == null || symptom.trim().isEmpty()) {
            return Result.error("症状不能为空");
        }

        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> req = new HashMap<>();
            req.put("symptom", symptom);

            HttpEntity<Map<String, String>> entity =
                    new HttpEntity<>(req, headers);

            ResponseEntity<Map> resp =
                    restTemplate.postForEntity(PYTHON_API, entity, Map.class);

            String standard = (String) resp.getBody().get("standard");
            return Result.success(standard);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("症状标准化失败");
        }
    }
}
