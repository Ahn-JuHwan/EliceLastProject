package io.camp.campsite.mapper;

import io.camp.campsite.model.dto.CampSiteAllDto;
import io.camp.campsite.model.dto.CampSiteDto;
import io.camp.campsite.model.entity.Campsite;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CampsiteMapper {

    @Mapping(source = "zones", target = "zones", ignore = true)
    CampSiteDto toCampsiteDto(Campsite campsite);

    Campsite toCampsiteEntity(CampSiteDto campSiteDto);

    //CampSiteAllDto toCampSiteAllDto(Campsite campsite);
}
