package com.stagepass.live.adapter.out.mysql;

import com.stagepass.live.adapter.out.mysql.entity.AsientoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AsientoJpaRepository extends JpaRepository<AsientoEntity, String> {

    List<AsientoEntity> findByRecintoId(String recintoId);
}
