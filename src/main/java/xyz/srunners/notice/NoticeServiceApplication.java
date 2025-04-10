package xyz.srunners.notice;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NoticeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NoticeServiceApplication.class, args);
    }

    @PostConstruct
    public void init(){
        System.out.println("NoticeServiceApplication.init ::::::::::::::::: 2");
    }
}
