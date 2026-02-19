package br.com.gabwsv.secure_todo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tasks")
//VULNERABLE [API09:2023]: Improper Inventory Management
//@Deprecated // Sinaliza para outros desenvolvedores
public class OldTaskController {

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.status(HttpStatus.GONE) // Retorna 410 (Gone)
                             .body("Esta versão da API foi descontinuada. Use a V2.");
    }

    // ... métodos antigos sem as novas travas de segurança
}






