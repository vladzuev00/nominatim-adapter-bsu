package by.aurorasoft.nominatim.crud.model.entity;

import by.aurorasoft.nominatim.crud.model.entity.CityEntity.Type;
import org.junit.Test;

import static by.aurorasoft.nominatim.crud.model.entity.CityEntity.Type.*;
import static org.junit.Assert.assertSame;

public final class CityEntityTest {

    @Test
    public void capitalJsonValueShouldBeIdentifiedAsCapital() {
        final String jsonValue = "yes";
        final Type actual = findByCapitalJsonValue(jsonValue);
        assertSame(CAPITAL, actual);
    }

    @Test
    public void capitalJsonValueShouldBeIdentifiedAsRegional() {
        final String jsonValue = "4";
        final Type actual = findByCapitalJsonValue(jsonValue);
        assertSame(REGIONAL, actual);
    }

    @Test
    public void capitalJsonValueShouldBeIdentifiedAsNotDefinedType() {
        final String jsonValue = "any text";
        final Type actual = findByCapitalJsonValue(jsonValue);
        assertSame(NOT_DEFINED, actual);
    }

    @Test
    public void nullAsCapitalJsonValueShouldBeIdentifiedAsNotDefinedType() {
        final Type actual = findByCapitalJsonValue(null);
        assertSame(NOT_DEFINED, actual);
    }
}
