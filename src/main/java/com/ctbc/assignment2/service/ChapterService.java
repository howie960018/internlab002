package com.ctbc.assignment2.service;

import com.ctbc.assignment2.bean.Chapter;

import java.util.List;

public interface ChapterService {
    List<Chapter> findByCourse(Long courseId);

    Chapter save(Chapter chapter);

    Chapter findById(Long chapterId);

    void delete(Long chapterId);
}
