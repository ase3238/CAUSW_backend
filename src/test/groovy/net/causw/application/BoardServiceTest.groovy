package net.causw.application

import net.causw.application.dto.board.BoardCreateRequestDto
import net.causw.application.dto.board.BoardResponseDto
import net.causw.application.dto.board.BoardUpdateRequestDto
import net.causw.application.spi.*
import net.causw.domain.exceptions.BadRequestException
import net.causw.domain.exceptions.UnauthorizedException
import net.causw.domain.model.*
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.modules.junit4.PowerMockRunnerDelegate
import org.spockframework.runtime.Sputnik
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import javax.validation.ConstraintViolationException
import javax.validation.Validation
import javax.validation.Validator

@ActiveProfiles(value = "test")
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Sputnik.class)
@PrepareForTest([BoardDomainModel.class])
class BoardServiceTest extends Specification {
    private BoardPort boardPort = Mock(BoardPort.class)
    private UserPort userPort = Mock(UserPort.class)
    private CirclePort circlePort = Mock(CirclePort.class)
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator()
    private BoardService boardService = new BoardService(
            this.boardPort,
            this.userPort,
            this.circlePort,
            this.validator
    )

    def mockBoardDomainModel

    def setup() {
        this.mockBoardDomainModel = BoardDomainModel.of(
                "test",
                "test",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                "test category",
                false,
                null
        )
    }

    /**
     * Test cases for board create
     */
    @Test
    def "Board create normal case"() {
        given:
        def mockBoardCreateRequestDto = new BoardCreateRequestDto(
                "test",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                "test category",
                null
        )

        def creatorUserDomainModel = UserDomainModel.of(
                "test",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.ACTIVE
        )

        def circleDomainModel = CircleDomainModel.of(
                "test",
                "test",
                null,
                "test_description",
                false,
                creatorUserDomainModel
        )

        this.userPort.findById("test") >> Optional.of(creatorUserDomainModel)
        this.circlePort.findById("test") >> Optional.of(circleDomainModel)
        this.boardPort.create((BoardDomainModel) this.mockBoardDomainModel) >> this.mockBoardDomainModel
        this.boardPort.create((BoardDomainModel) this.mockBoardDomainModel) >> this.mockBoardDomainModel

        when: "create board without circle"
        PowerMockito.mockStatic(BoardDomainModel.class)
        PowerMockito.when(BoardDomainModel.of(
                "test",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                "test category",
                null
        )).thenReturn((BoardDomainModel) this.mockBoardDomainModel)
        def boardResponseDto = this.boardService.create("test", mockBoardCreateRequestDto)

        then:
        boardResponseDto instanceof BoardResponseDto
        with(boardResponseDto) {
            getName() == "test"
            getDescription() == "test_description"
        }

        when: "create board with circle"
        mockBoardCreateRequestDto.setCircleId("test")
        creatorUserDomainModel.setRole(Role.LEADER_CIRCLE)
        PowerMockito.mockStatic(BoardDomainModel.class)
        PowerMockito.when(BoardDomainModel.of(
                "test",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                "test category",
                circleDomainModel
        )).thenReturn((BoardDomainModel) this.mockBoardDomainModel)
        boardResponseDto = this.boardService.create("test", mockBoardCreateRequestDto)

        then:
        boardResponseDto instanceof BoardResponseDto
        with(boardResponseDto) {
            getName() == "test"
            getDescription() == "test_description"
        }
    }

    @Test
    def "Board create invalid data"() {
        given:
        def mockBoardCreateRequestDto = new BoardCreateRequestDto(
                "test",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                "test category",
                null
        )

        def mockCreatorDomainModel = UserDomainModel.of(
                "test",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.ACTIVE
        )

        this.userPort.findById("test") >> Optional.of(mockCreatorDomainModel)
        this.boardPort.create((BoardDomainModel) this.mockBoardDomainModel) >> this.mockBoardDomainModel

        when: "name is blank"
        mockBoardCreateRequestDto.setName("")
        this.mockBoardDomainModel.setName("")
        PowerMockito.mockStatic(BoardDomainModel.class)
        PowerMockito.when(BoardDomainModel.of(
                "",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                "test category",
                null
        )).thenReturn((BoardDomainModel) this.mockBoardDomainModel)
        this.boardService.create("test", mockBoardCreateRequestDto)

        then:
        thrown(ConstraintViolationException)

        when: "create role is null"
        mockBoardCreateRequestDto.setName("test")
        this.mockBoardDomainModel.setName("test")
        mockBoardCreateRequestDto.setCreateRoleList(null)
        this.mockBoardDomainModel.setCreateRoleList(null)
        PowerMockito.mockStatic(BoardDomainModel.class)
        PowerMockito.when(BoardDomainModel.of(
                "test",
                "test_description",
                null,
                "test category",
                null
        )).thenReturn((BoardDomainModel) this.mockBoardDomainModel)
        this.boardService.create("test", mockBoardCreateRequestDto)

        then:
        thrown(ConstraintViolationException)
    }

    @Test
    def "Board create invalid role"() {
        given:
        def mockBoardCreateRequestDto = new BoardCreateRequestDto(
                "test",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                "test category",
                null
        )

        def mockCreatorDomainModel = UserDomainModel.of(
                "test",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.AWAIT
        )

        this.userPort.findById("test") >> Optional.of(mockCreatorDomainModel)
        this.boardPort.create((BoardDomainModel) this.mockBoardDomainModel) >> this.mockBoardDomainModel

        when: "invalid creator role"
        mockCreatorDomainModel.setRole(Role.NONE)
        this.boardService.create("test", mockBoardCreateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    @Test
    def "Board create invalid leader"() {
        given:
        def mockBoardCreateRequestDto = new BoardCreateRequestDto(
                "test",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                "test category",
                "test"
        )

        def creator = UserDomainModel.of(
                "test",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.AWAIT
        )

        def mockCircleDomainModel = CircleDomainModel.of(
                "test",
                "test",
                null,
                "test_description",
                false,
                creator
        )

        this.userPort.findById("test") >> Optional.of(creator)
        this.circlePort.findById("test") >> Optional.of(mockCircleDomainModel)
        this.boardPort.create((BoardDomainModel) this.mockBoardDomainModel) >> this.mockBoardDomainModel

        when: "invalid leader id"
        creator.setId("invalid_test")
        this.boardService.create("test", mockBoardCreateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for board update
     */
    @Test
    def "Board update normal case"() {
        given:
        def mockBoardUpdateRequestDto = new BoardUpdateRequestDto(
                "test_update",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                "test category"
        )

        def updater = UserDomainModel.of(
                "test",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.ACTIVE
        )

        def mockCircleDomainModel = CircleDomainModel.of(
                "test",
                "test",
                null,
                "test_description",
                false,
                updater
        )

        def mockUpdatedBoardDomainModel = BoardDomainModel.of(
                "test",
                mockBoardUpdateRequestDto.getName(),
                mockBoardUpdateRequestDto.getDescription(),
                mockBoardUpdateRequestDto.getCreateRoleList(),
                "test category",
                false,
                null
        )

        this.userPort.findById("test") >> Optional.of(updater)
        this.circlePort.findById("test") >> Optional.of(mockCircleDomainModel)
        this.boardPort.findById("test") >> Optional.of(this.mockBoardDomainModel)
        this.boardPort.update("test", (BoardDomainModel)this.mockBoardDomainModel) >> Optional.of(mockUpdatedBoardDomainModel)

        when: "update board without circle"
        def boardResponseDto = this.boardService.update("test", "test", mockBoardUpdateRequestDto)

        then:
        boardResponseDto instanceof BoardResponseDto
        with(boardResponseDto) {
            getName() == "test_update"
            getDescription() == "test_description"
        }

        when: "update board with circle"
        boardResponseDto = this.boardService.update("test", "test", mockBoardUpdateRequestDto)

        then:
        boardResponseDto instanceof BoardResponseDto
        with(boardResponseDto) {
            getName() == "test_update"
            getDescription() == "test_description"
        }
    }

    @Test
    def "Board update already deleted"() {
        given:
        def mockBoardUpdateRequestDto = new BoardUpdateRequestDto(
                "test_update",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                "test category"
        )

        def updater = UserDomainModel.of(
                "test",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.ACTIVE
        )

        this.userPort.findById("test") >> Optional.of(updater)
        this.boardPort.findById("test") >> Optional.of(this.mockBoardDomainModel)

        when: "board already delete"
        this.mockBoardDomainModel.setIsDeleted(true)
        this.boardService.update("test", "test", mockBoardUpdateRequestDto)

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Board update invalid data"() {
        given:
        def mockBoardUpdateRequestDto = new BoardUpdateRequestDto(
                "test_update",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                "test category"
        )

        def updater = UserDomainModel.of(
                "test",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.ACTIVE
        )

        this.userPort.findById("test") >> Optional.of(updater)
        this.boardPort.findById("test") >> Optional.of(this.mockBoardDomainModel)

        when: "name is blank"
        mockBoardUpdateRequestDto.setName("")
        this.mockBoardDomainModel.setName("")
        this.boardService.update("test", "test", mockBoardUpdateRequestDto)

        then:
        thrown(ConstraintViolationException)

        when: "create role is null"
        mockBoardUpdateRequestDto.setName("test")
        this.mockBoardDomainModel.setName("test")
        mockBoardUpdateRequestDto.setCreateRoleList(null)
        this.mockBoardDomainModel.setCreateRoleList(null)
        this.boardService.update("test", "test", mockBoardUpdateRequestDto)

        then:
        thrown(ConstraintViolationException)
    }

    @Test
    def "Board update invalid role"() {
        given:
        def mockBoardUpdateRequestDto = new BoardUpdateRequestDto(
                "test_update",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                "test category"
        )

        def updater = UserDomainModel.of(
                "test",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.AWAIT
        )

        this.userPort.findById("test") >> Optional.of(updater)
        this.boardPort.findById("test") >> Optional.of(this.mockBoardDomainModel)

        when: "invalid creator role"
        updater.setRole(Role.NONE)
        this.boardService.update("test", "test", mockBoardUpdateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    @Test
    def "Board update invalid leader"() {
        given:
        def mockBoardUpdateRequestDto = new BoardUpdateRequestDto(
                "test_update",
                "test_description",
                Arrays.asList("PRESIDENT", "COUNCIL"),
                "test category"
        )

        def updater = UserDomainModel.of(
                "test",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.AWAIT
        )

        def mockCircleDomainModel = CircleDomainModel.of(
                "test",
                "test",
                null,
                "test_description",
                false,
                updater
        )

        this.mockBoardDomainModel.setCircle(mockCircleDomainModel)
        this.userPort.findById("test") >> Optional.of(updater)
        this.circlePort.findById("test") >> Optional.of(mockCircleDomainModel)
        this.boardPort.findById("test") >> Optional.of(this.mockBoardDomainModel)

        when: "invalid leader id"
        updater.setId("invalid_test")
        this.boardService.update("test", "test", mockBoardUpdateRequestDto)

        then:
        thrown(UnauthorizedException)
    }

    /**
     * Test cases for board delete
     */
    @Test
    def "Board delete normal case"() {
        given:
        def deleter = UserDomainModel.of(
                "test",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.ACTIVE
        )

        def mockCircleDomainModel = CircleDomainModel.of(
                "test",
                "test",
                null,
                "test_description",
                false,
                deleter
        )

        def mockDeletedBoardDomainModel = BoardDomainModel.of(
                (String) this.mockBoardDomainModel.getId(),
                (String) this.mockBoardDomainModel.getName(),
                (String) this.mockBoardDomainModel.getDescription(),
                (List<String>) this.mockBoardDomainModel.getCreateRoleList(),
                "test category",
                true,
                mockCircleDomainModel
        )

        this.userPort.findById("test") >> Optional.of(deleter)
        this.circlePort.findById("test") >> Optional.of(mockCircleDomainModel)
        this.boardPort.findById("test") >> Optional.of(this.mockBoardDomainModel)
        this.boardPort.delete("test") >> Optional.of(mockDeletedBoardDomainModel)

        when: "update board without circle"
        def boardResponseDto = this.boardService.delete("test", "test")

        then:
        boardResponseDto instanceof BoardResponseDto
        with(boardResponseDto) {
            getIsDeleted()
        }

        when: "update board with circle"
        this.mockBoardDomainModel.setCircle(mockCircleDomainModel)
        mockDeletedBoardDomainModel.setCircle(mockCircleDomainModel)
        deleter.setRole(Role.LEADER_CIRCLE)
        boardResponseDto = this.boardService.delete("test", "test")

        then:
        boardResponseDto instanceof BoardResponseDto
        with(boardResponseDto) {
            getIsDeleted()
        }
    }

    @Test
    def "Board delete already deleted"() {
        given:
        def deleter = UserDomainModel.of(
                "test",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.ACTIVE
        )

        this.userPort.findById("test") >> Optional.of(deleter)
        this.boardPort.findById("test") >> Optional.of(this.mockBoardDomainModel)

        when: "board already delete"
        this.mockBoardDomainModel.setIsDeleted(true)
        this.boardService.delete("test", "test")

        then:
        thrown(BadRequestException)
    }

    @Test
    def "Board delete invalid role"() {
        given:
        def deleter = UserDomainModel.of(
                "test",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.AWAIT
        )

        this.userPort.findById("test") >> Optional.of(deleter)
        this.boardPort.findById("test") >> Optional.of(this.mockBoardDomainModel)

        when: "invalid creator role"
        deleter.setRole(Role.NONE)
        this.boardService.delete("test", "test")

        then:
        thrown(UnauthorizedException)
    }

    @Test
    def "Board delete invalid leader"() {
        given:
        def deleter = UserDomainModel.of(
                "test",
                "test@cau.ac.kr",
                "test",
                "test1234!",
                "20210000",
                2021,
                Role.PRESIDENT,
                null,
                UserState.AWAIT
        )

        def mockCircleDomainModel = CircleDomainModel.of(
                "test",
                "test",
                null,
                "test_description",
                false,
                deleter
        )

        this.mockBoardDomainModel.setCircle(mockCircleDomainModel)
        this.userPort.findById("test") >> Optional.of(deleter)
        this.circlePort.findById("test") >> Optional.of(mockCircleDomainModel)
        this.boardPort.findById("test") >> Optional.of(this.mockBoardDomainModel)

        when: "invalid leader id"
        deleter.setId("invalid_test")
        this.boardService.delete("test", "test")

        then:
        thrown(UnauthorizedException)
    }
}
