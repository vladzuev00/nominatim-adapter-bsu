package by.aurorasoft.nominatim.crud.mapper;

import by.aurorasoft.nominatim.crud.model.dto.SearchingCitiesProcess;
import by.aurorasoft.nominatim.crud.model.entity.SearchingCitiesProcessEntity;
import by.nhorushko.crudgeneric.v2.mapper.AbsMapperEntityDto;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public final class SearchingCitiesProcessMapper
        extends AbsMapperEntityDto<SearchingCitiesProcessEntity, SearchingCitiesProcess> {

    public SearchingCitiesProcessMapper(ModelMapper modelMapper) {
        super(modelMapper, SearchingCitiesProcessEntity.class, SearchingCitiesProcess.class);
    }

    @Override
    protected SearchingCitiesProcess create(SearchingCitiesProcessEntity entity) {
        return new SearchingCitiesProcess(entity.getId(), entity.getGeometry(), entity.getSearchStep(),
                entity.getTotalPoints(), entity.getHandledPoints(), entity.getStatus());
    }
}
