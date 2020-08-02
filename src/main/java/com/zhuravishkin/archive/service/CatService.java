package com.zhuravishkin.archive.service;

import com.zhuravishkin.archive.model.Cat;
import com.zhuravishkin.archive.repository.CatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CatService {
    private final CatRepository repository;

    @Autowired
    public CatService(CatRepository repository) {
        this.repository = repository;
    }

    public List<Cat> findAll() {
        return repository.findAll();
    }

    public List<Cat> findCats(String name, Integer age, LocalDateTime dateTime) {
        return repository.findAllByNameAndAgeAndDateTimeGreaterThanEqual(name, age, dateTime);
    }
}
