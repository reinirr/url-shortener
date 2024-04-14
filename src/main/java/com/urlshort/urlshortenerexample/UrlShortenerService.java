package com.urlshort.urlshortenerexample;

import com.google.common.hash.Hashing;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.nio.charset.StandardCharsets;

@RequestMapping("/")
@RestController
public class UrlShortenerService {

    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    private HttpServletRequest request;


    @GetMapping("/{hash}")
    public ModelAndView redirectToUrl(@PathVariable String hash) {
        String url = redisTemplate.opsForValue().get(hash);

        if (url == null) {
            throw new RuntimeException("URL not found: " + hash);
        }

        return new ModelAndView("redirect:" + url);
    }

    @PostMapping
    public String createShortUrl(@RequestBody String url) {
        UrlValidator urlValidator = new UrlValidator(
                new String[]{"http", "https"}
        );
        if (urlValidator.isValid(url)) {
            String hash = Hashing.murmur3_32_fixed().hashString(url, StandardCharsets.UTF_8).toString();
            System.out.println("URL id generated: " + hash);
            if (redisTemplate.opsForValue().get(hash) == null) {
                redisTemplate.opsForValue().set(hash, url);
            }

            String baseUrl = request.getRequestURL().toString();
            baseUrl = baseUrl.replace(request.getRequestURI(), "");
            return  baseUrl + "/" +hash;
        }
        throw new RuntimeException("URL Invalid: " + url);
    }
}
