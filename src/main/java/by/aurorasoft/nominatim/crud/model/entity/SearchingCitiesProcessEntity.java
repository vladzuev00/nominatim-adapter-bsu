package by.aurorasoft.nominatim.crud.model.entity;

import com.vladmihalcea.hibernate.type.basic.PostgreSQLEnumType;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.locationtech.jts.geom.Geometry;

import javax.persistence.*;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "searching_cities_process")
@TypeDef(
        name = "pgsql_enum",
        typeClass = PostgreSQLEnumType.class
)
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class SearchingCitiesProcessEntity extends BaseEntity<Long> {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "bounds")
    private Geometry geometry;

    @Column(name = "search_step")
    private double searchStep;

    @Column(name = "total_points")
    private long totalPoints;

    @Column(name = "handled_points")
    private long handledPoints;

    @Enumerated(STRING)
    @Column(name = "status")
    @Type(type = "pgsql_enum")
    private Status status;

    public enum Status {
        HANDLING, SUCCESS, ERROR
    }
}
