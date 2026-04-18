package com.ctbc.assignment2.service.impl;

import com.ctbc.assignment2.bean.Chapter;
import com.ctbc.assignment2.exception.ResourceNotFoundException;
import com.ctbc.assignment2.repository.ChapterRepository;
import com.ctbc.assignment2.service.ChapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChapterServiceImpl implements ChapterService {

    @Autowired
    private ChapterRepository repository;

    @Override
    public List<Chapter> findByCourse(Long courseId) {
        return repository.findByCourseIdOrderBySortOrder(courseId);
    }

    @Override
    public Chapter save(Chapter chapter) {
        return repository.save(chapter);
    }

    @Override
    public Chapter findById(Long chapterId) {
        return repository.findById(chapterId)
                .orElseThrow(() -> new ResourceNotFoundException("Chapter not found: " + chapterId));
    }

    @Override
    public void delete(Long chapterId) {
        if (!repository.existsById(chapterId)) {
            throw new ResourceNotFoundException("Chapter not found: " + chapterId);
        }
        repository.deleteById(chapterId);
    }
}
