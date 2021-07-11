package net.causw.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.causw.domain.model.BoardDomainModel;

import java.util.List;

@Getter
@NoArgsConstructor
public class BoardDetailDto {
    private String id;
    private String name;
    private String description;
    private List<String> createRoleList;
    private List<String> modifyRoleList;
    private List<String> readRoleList;

    private BoardDetailDto(
            String id,
            String name,
            String description,
            List<String> createRoleList,
            List<String> modifyRoleList,
            List<String> readRoleList
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createRoleList = createRoleList;
        this.modifyRoleList = modifyRoleList;
        this.readRoleList = readRoleList;
    }

    public static BoardDetailDto of(BoardDomainModel board) {
        return new BoardDetailDto(
                board.getId(),
                board.getName(),
                board.getDescription(),
                board.getCreateRoleList(),
                board.getModifyRoleList(),
                board.getReadRoleList()
        );
    }
}
