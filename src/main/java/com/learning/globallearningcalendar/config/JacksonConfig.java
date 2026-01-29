package com.learning.globallearningcalendar.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.TimeZone;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Disable timestamps for dates
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // Recommended: accept unknown properties gracefully
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // Use a standard ISO date format (can be overridden via properties)
        StdDateFormat dateFormat = new StdDateFormat();
        mapper.setDateFormat(dateFormat);
        mapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        return mapper;
    }
}
