package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Value("${app.allowed-root:./safe-root}")
    private String allowedRoot;

    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> delete(@RequestBody Map<String, List<String>> body) {
        List<String> paths = body.getOrDefault("paths", List.of());
        Path root = Paths.get(allowedRoot).toAbsolutePath().normalize();
        List<String> deleted = new ArrayList<>();
        List<Map<String, String>> refused = new ArrayList<>();

        for (String p : paths) {
            try {
                Path target = root.resolve(p).normalize();
                if (!target.startsWith(root)) {
                    refused.add(Map.of("path", p, "reason", "outside allowed root"));
                    continue;
                }
                if (Files.isSymbolicLink(target)) {
                    refused.add(Map.of("path", p, "reason", "symlink not allowed"));
                    continue;
                }
                if (!Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                    refused.add(Map.of("path", p, "reason", "does not exist"));
                    continue;
                }
                if (!Files.isRegularFile(target, LinkOption.NOFOLLOW_LINKS)) {
                    refused.add(Map.of("path", p, "reason", "not a regular file"));
                    continue;
                }
                Files.delete(target);
                deleted.add(p);
            } catch (IOException | InvalidPathException e) {
                refused.add(Map.of("path", p, "reason", e.getMessage() == null ? "error" : e.getMessage()));
            }
        }
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("deleted", deleted);
        resp.put("refused", refused);
        return ResponseEntity.ok(resp);
    }
}
