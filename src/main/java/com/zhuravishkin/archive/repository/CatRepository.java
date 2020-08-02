package com.zhuravishkin.archive.repository;

import com.zhuravishkin.archive.model.Cat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CatRepository extends JpaRepository<Cat, Long> {
    List<Cat> findAllByNameAndAgeAndDateTimeGreaterThanEqual(String name, Integer age, LocalDateTime dateTime);
}
