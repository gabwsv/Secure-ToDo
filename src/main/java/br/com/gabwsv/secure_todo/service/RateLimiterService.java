package br.com.gabwsv.secure_todo.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    // Cache em memória com os IP's
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String ipAdress){
        return cache.computeIfAbsent(ipAdress, this::newBucket);
    }

    private Bucket newBucket(String apiKey){
        // 5 tentativas, a cada 1 minuto.
        Bandwidth limit = Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1)));

        return Bucket.builder().addLimit(limit).build();
    }

}
