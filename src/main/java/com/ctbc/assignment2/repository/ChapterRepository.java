package com.ctbc.assignment2.repository;

import com.ctbc.assignment2.bean.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findByCourseIdOrderBySortOrder(Long courseId);

    void deleteByCourseId(Long courseId);
}
