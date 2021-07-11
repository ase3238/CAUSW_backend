package net.causw.infra;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "TB_BOARD")
public class Board extends BaseEntity {
    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "create_role_list")
    private String createRoles;

    @Column(name = "modify_role_list")
    private String modifyRoles;

    @Column(name = "read_role_list")
    private String readRoles;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "post_id")
    private Set<Post> postSet;

    private Board(
            String name,
            String description,
            String createRoles,
            String modifyRoles,
            String readRoles
    ) {
        this.name = name;
        this.description = description;
        this.createRoles = createRoles;
        this.modifyRoles = modifyRoles;
        this.readRoles = readRoles;
    }

    public static Board of (
            String name,
            String description,
            String createRoles,
            String modifyRoles,
            String readRoles
    ) {
        return new Board(
                name,
                description,
                createRoles,
                modifyRoles,
                readRoles
        );
    }
}
