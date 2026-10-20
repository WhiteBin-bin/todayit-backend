package com.todayit.course.controller;

import com.todayit.course.controller.docs.CourseApiDocs;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 코스 API 요청을 처리합니다. */
@RestController
@RequestMapping("/api/v1/courses")
public class CourseController implements CourseApiDocs {}
