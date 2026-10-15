package com.stagepass.live.adapter.out.mysql;

import com.stagepass.live.adapter.out.mysql.entity.RecintoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecintoJpaRepository extends JpaRepository<RecintoEntity, String> {
}
