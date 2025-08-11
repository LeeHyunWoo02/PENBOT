package Project.PENBOT.Host.Converter;

import Project.PENBOT.Host.Dto.BlockDateRequestDTO;
import Project.PENBOT.Host.Entity.BlockedDate;

public class BlockedDateConverter {

    public static BlockedDate toEntity(BlockDateRequestDTO requestDTO){
        return BlockedDate.builder()
                .startDate(requestDTO.getStartDate())
                .endDate(requestDTO.getEndDate())
                .reason(requestDTO.getReason())
                .build();
    }
}
