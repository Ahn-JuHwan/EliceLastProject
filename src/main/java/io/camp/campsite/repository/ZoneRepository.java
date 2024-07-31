package io.camp.campsite.repository;

import io.camp.campsite.model.entity.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZoneRepository extends JpaRepository<Zone,Long> , ZoneRepositoryCustom {
}
