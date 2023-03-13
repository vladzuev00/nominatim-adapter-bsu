package by.aurorasoft.nominatim.trigger;

import by.aurorasoft.nominatim.base.AbstractContextTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.Assert.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

public final class TriggerSearchingCitiesProcessTest extends AbstractContextTest {
    private static final String SQL_QUERY_TO_FIND_PROCESS_UPDATED_TIME
            = "SELECT updated_time FROM searching_cities_process WHERE id = :id";
    private static final String SQL_QUERY_TO_INCREASE_PROCESS_HANDLED_POINTS
            = "UPDATE searching_cities_process SET handled_points = handled_points + :delta WHERE id = :id";

    private static final String NAME_NAMED_PARAMETER_PROCESS_ID = "id";
    private static final String NAME_NAMED_PARAMETER_DELTA = "delta";

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    @Transactional(propagation = NOT_SUPPORTED)
    @Sql(statements = "INSERT INTO searching_cities_process "
            + "(id, bounds, search_step, total_points, handled_points, status) "
            + "VALUES(255, ST_GeomFromText('POLYGON((1 2, 3 4, 5 6, 6 7, 1 2))', 4326), 0.01, 10000, 1000, 'HANDLING')")
    @Sql(statements = "DELETE FROM searching_cities_process", executionPhase = AFTER_TEST_METHOD)
    public void updatedTimeOfProcessShouldBeNullAndUpdatedOnUpdateProcess() {
        final Long givenProcessId = 255L;
        final Long givenDeltaToIncreaseHandledPoints = 100L;

        final LocalDateTime updatedDateTimeBeforeUpdating = this.findUpdatedDateTimeOfProcessByProcessId(
                givenProcessId);

        this.increaseHandledPoints(givenDeltaToIncreaseHandledPoints, givenProcessId);

        final LocalDateTime updatedDateTimeAfterUpdating = this.findUpdatedDateTimeOfProcessByProcessId(
                givenProcessId);

        assertTrue(updatedDateTimeAfterUpdating.isAfter(updatedDateTimeBeforeUpdating));
    }

    private LocalDateTime findUpdatedDateTimeOfProcessByProcessId(Long processId) {
        final SqlParameterSource source = new MapSqlParameterSource()
                .addValue(NAME_NAMED_PARAMETER_PROCESS_ID, processId);
        return this.jdbcTemplate.queryForObject(SQL_QUERY_TO_FIND_PROCESS_UPDATED_TIME, source,
                LocalDateTime.class);
    }

    private void increaseHandledPoints(Long delta, Long processId) {
        final SqlParameterSource source = new MapSqlParameterSource()
                .addValue(NAME_NAMED_PARAMETER_DELTA, delta)
                .addValue(NAME_NAMED_PARAMETER_PROCESS_ID, processId);
        this.jdbcTemplate.update(SQL_QUERY_TO_INCREASE_PROCESS_HANDLED_POINTS, source);
    }
}
