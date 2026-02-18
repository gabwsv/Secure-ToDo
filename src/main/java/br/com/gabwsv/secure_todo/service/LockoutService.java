package br.com.gabwsv.secure_todo.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LockoutService {
    //Utilize redis para expiração automática
    private final Map<String, Integer> attempts = new ConcurrentHashMap<>();

    public void registerFailure(String email) {
        attempts.put(email, attempts.getOrDefault(email, 0) + 1);
    }

    public boolean isLocked(String email) {
        return attempts.getOrDefault(email, 0) >= 5;
    }

    public void clearAttempts(String email){
        attempts.remove(email);
    }
}
