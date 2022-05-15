package com.example.nijiScraping;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NijidbRepository extends JpaRepository<Member, String> {
}
