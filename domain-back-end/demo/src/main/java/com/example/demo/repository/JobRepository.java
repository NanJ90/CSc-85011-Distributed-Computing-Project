package com.example.demo.repository;
//libraries
import com.example.demo.domain.Job;
import com.example.demo.domain.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    Job getById(Long  id);

    Job getByFileName(String fileName);

    List<Job> getAllByStatus(Status status);

    void deleteByFileName(String fileName);
}
