package com.zhuravishkin.archive.service;

import com.zhuravishkin.archive.model.Cat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class CatServiceTest {
    @Autowired
    private CatService service;

    @Test
    void findAll() {
        List<Cat> cats = service.findAll();
        assertEquals(8, cats.size());
    }

    @Test
    void findCats() {
        List<Cat> cats = service.findCats("Tom", 31, LocalDateTime.parse("2019-06-18T19:15:01"));
        assertEquals(1, cats.size());
    }
}