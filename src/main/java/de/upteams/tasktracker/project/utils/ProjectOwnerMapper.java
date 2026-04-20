package de.upteams.tasktracker.project.utils;

import de.upteams.tasktracker.project.dto.response.ProjectOwnerDto;
import de.upteams.tasktracker.user.entity.AppUser;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProjectOwnerMapper {

    ProjectOwnerDto mapToDto(AppUser user);
}
