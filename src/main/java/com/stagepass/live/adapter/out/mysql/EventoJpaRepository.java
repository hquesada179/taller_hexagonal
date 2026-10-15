package com.stagepass.live.adapter.out.mysql;

import com.stagepass.live.adapter.out.mysql.entity.EventoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoJpaRepository extends JpaRepository<EventoEntity, String> {
}
