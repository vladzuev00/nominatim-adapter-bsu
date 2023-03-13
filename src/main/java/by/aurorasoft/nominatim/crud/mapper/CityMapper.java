package by.aurorasoft.nominatim.crud.mapper;

import by.aurorasoft.nominatim.crud.model.dto.City;
import by.aurorasoft.nominatim.crud.model.entity.CityEntity;
import by.nhorushko.crudgeneric.v2.mapper.AbsMapperEntityDto;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public final class CityMapper extends AbsMapperEntityDto<CityEntity, City> {

    public CityMapper(ModelMapper modelMapper) {
        super(modelMapper, CityEntity.class, City.class);
    }

    @Override
    protected City create(CityEntity entity) {
        return new City(
                entity.getId(),
                entity.getName(),
                entity.getGeometry(),
                entity.getType(),
                entity.getBoundingBox()
        );
    }
}
