package by.aurorasoft.nominatim.crud.model.entity;

import by.nhorushko.crudgeneric.v2.domain.AbstractEntity;
import org.hibernate.Hibernate;

import java.util.Objects;

import static java.util.Objects.hash;

public abstract class BaseEntity<IdType> implements AbstractEntity<IdType> {
    public abstract IdType getId();

    @Override
    @SuppressWarnings({"unchecked", "EqualsWhichDoesntCheckParameterClass"})
    public final boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null) {
            return false;
        }
        if (Hibernate.getClass(this) != Hibernate.getClass(otherObject)) {
            return false;
        }
        final BaseEntity<IdType> other = (BaseEntity<IdType>) otherObject;
        return Objects.equals(this.getId(), other.getId());
    }

    @Override
    public final int hashCode() {
        return hash(this.getId());
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[id = " + this.getId() + "]";
    }
}
