package com.bms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BmsChatbotApplication {
    public static void main(String[] args) {
        SpringApplication.run(BmsChatbotApplication.class, args);
        System.out.println("🚀 BMS Chatbot running at http://localhost:9090");
    }
}
