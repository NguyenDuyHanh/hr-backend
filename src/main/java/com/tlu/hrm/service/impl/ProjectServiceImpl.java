package com.tlu.hrm.service.impl;

import com.tlu.hrm.dto.request.ProjectCreateRequest;
import com.tlu.hrm.dto.request.ProjectStaffRequest;
import com.tlu.hrm.dto.request.StaffDto;
import com.tlu.hrm.dto.response.ProjectResponse;
import com.tlu.hrm.dto.search.ProjectSearchRequest;
import com.tlu.hrm.exception.ResourceNotFoundException;
import com.tlu.hrm.enums.ProjectRole;
import com.tlu.hrm.model.*;
import com.tlu.hrm.repository.*;
import com.tlu.hrm.security.SecurityUtils;
import com.tlu.hrm.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectStaffRepository projectStaffRepository;

    @Autowired
    private ProjectWorkingStatusRepository projectWorkingStatusRepository;

    @Autowired
    private StaffRepository staffRepository;

    @Autowired
    private SecurityUtils securityUtils;

    @Override
    public Page<ProjectResponse> getAllProjects(ProjectSearchRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        boolean isManager = securityUtils.isManagerOrAdmin(currentUser);
        Staff currentStaff = (currentUser != null) ? currentUser.getStaff() : null;

        List<Project> filteredList = projectRepository.findAll().stream()
                .filter(p -> p.getVoided() == null || !p.getVoided())
                .filter(p -> {
                    // Phân quyền: Employee chỉ xem được dự án họ tham gia
                    if (!isManager) {
                        if (currentStaff == null) {
                            return false;
                        }
                        boolean isMember = p.getProjectStaffs().stream()
                                .filter(ps -> ps.getVoided() == null || !ps.getVoided())
                                .anyMatch(ps -> ps.getStaff() != null
                                        && ps.getStaff().getId().equals(currentStaff.getId()));
                        if (!isMember) {
                            return false;
                        }
                    }
                    return true;
                })
                .filter(p -> {
                    if (request != null) {
                        // 1. Tìm kiếm theo keyword (tên hoặc mã dự án)
                        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
                            String kw = request.getKeyword().toLowerCase();
                            boolean match = (p.getName() != null && p.getName().toLowerCase().contains(kw))
                                    || (p.getCode() != null && p.getCode().toLowerCase().contains(kw));
                            if (!match)
                                return false;
                        }
                        // 2. Lọc theo khoảng ngày bắt đầu
                        if (request.getStartDate() != null) {
                            if (p.getStartDate() == null || p.getStartDate().isBefore(request.getStartDate())) {
                                return false;
                            }
                        }
                        // 3. Lọc theo khoảng ngày kết thúc
                        if (request.getEndDate() != null) {
                            if (p.getEndDate() == null || p.getEndDate().isAfter(request.getEndDate())) {
                                return false;
                            }
                        }
                        // 4. Lọc theo trạng thái kết thúc
                        if (request.getIsFinished() != null) {
                            if (!request.getIsFinished().equals(p.getIsFinished())) {
                                return false;
                            }
                        }
                        // 5. Lọc theo một nhân viên cụ thể
                        if (request.getStaffId() != null) {
                            boolean hasStaff = p.getProjectStaffs().stream()
                                    .filter(ps -> ps.getVoided() == null || !ps.getVoided())
                                    .anyMatch(ps -> ps.getStaff() != null
                                            && ps.getStaff().getId().equals(request.getStaffId()));
                            if (!hasStaff)
                                return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());

        int total = filteredList.size();
        int pageNum = 0;
        int size = 10;

        if (request != null) {
            pageNum = request.getPageIndex() >= 1 ? request.getPageIndex() - 1 : 0;
            size = request.getPageSize() > 0 ? request.getPageSize() : 10;
        }

        int fromIndex = pageNum * size;
        int toIndex = Math.min(fromIndex + size, total);

        List<ProjectResponse> pageContent = new ArrayList<>();
        if (fromIndex < total) {
            pageContent = filteredList.subList(fromIndex, toIndex).stream()
                    .map(ProjectResponse::new)
                    .collect(Collectors.toList());
        }

        return new PageImpl<>(pageContent, PageRequest.of(pageNum, size), total);
    }

    @Override
    public List<ProjectResponse> getAllProjectsUnpaginated() {
        User currentUser = securityUtils.getCurrentUser();
        boolean isManager = securityUtils.isManagerOrAdmin(currentUser);
        Staff currentStaff = (currentUser != null) ? currentUser.getStaff() : null;

        return projectRepository.findAll().stream()
                .filter(p -> p.getVoided() == null || !p.getVoided())
                .filter(p -> {
                    if (!isManager) {
                        if (currentStaff == null) {
                            return false;
                        }
                        boolean isMember = p.getProjectStaffs().stream()
                                .filter(ps -> ps.getVoided() == null || !ps.getVoided())
                                .anyMatch(ps -> ps.getStaff() != null
                                        && ps.getStaff().getId().equals(currentStaff.getId()));
                        if (!isMember) {
                            return false;
                        }
                    }
                    return true;
                })
                .map(ProjectResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectResponse getProjectById(UUID id) {
        Project project = projectRepository.findById(id)
                .filter(p -> p.getVoided() == null || !p.getVoided())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dự án với ID: " + id));
        return new ProjectResponse(project);
    }

    @Override
    public ProjectResponse saveProject(ProjectCreateRequest request) {
        Project project;
        if (request.getId() != null) {
            project = projectRepository.findById(request.getId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Không tìm thấy dự án với ID: " + request.getId()));
        } else {
            project = new Project();
            project.setIsFinished(false);
        }

        project.setName(request.getName());
        project.setCode(request.getCode());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now());
        project.setEndDate(request.getEndDate());

        Project saved = projectRepository.save(project);

        // Lưu thành viên dự án nếu được truyền lên
        if (request.getStaffs() != null) {
            // Đánh dấu các thành viên cũ là voided
            List<ProjectStaff> oldMembers = projectStaffRepository.findByProjectId(saved.getId());
            for (ProjectStaff om : oldMembers) {
                om.setVoided(true);
                projectStaffRepository.save(om);
            }

            // Thêm mới hoặc kích hoạt lại thành viên
            for (ProjectStaffRequest pmReq : request.getStaffs()) {
                if (pmReq.getStaffId() == null)
                    continue;

                Staff staff = staffRepository.findById(pmReq.getStaffId()).orElse(null);
                if (staff != null) {
                    ProjectStaff member = oldMembers.stream()
                            .filter(om -> om.getStaff().getId().equals(pmReq.getStaffId()))
                            .findFirst()
                            .orElse(new ProjectStaff());

                    member.setProject(saved);
                    member.setStaff(staff);
                    member.setProjectRole(pmReq.getProjectRole() != null ? pmReq.getProjectRole() : ProjectRole.MEMBER);
                    member.setJoinedDate(pmReq.getJoinedDate() != null ? pmReq.getJoinedDate() : LocalDate.now());
                    member.setVoided(false);
                    projectStaffRepository.save(member);
                }
            }
        }

        // Tạo mặc định các trạng thái Kanban nếu là dự án mới và chưa cấu hình
        List<ProjectWorkingStatus> existingStatuses = projectWorkingStatusRepository.findByProjectId(saved.getId());
        if (existingStatuses.isEmpty()) {
            String[][] defaultStatuses = {
                    { "Todo", "TODO", "#1976d2", "1" },
                    { "Doing", "DOING", "#2e7d32", "2" },
                    { "Resolved", "RESOLVED", "#ed6c02", "3" },
                    { "Deployed", "DEPLOYED", "#9c27b0", "4" },
                    { "Tested", "TESTED", "#d32f2f", "5" },
                    { "Feedback", "FEEDBACK", "#0288d1", "6" },
                    { "Released", "RELEASED", "#009688", "7" },
                    { "Completed", "COMPLETED", "#757575", "8" }
            };
            for (String[] ds : defaultStatuses) {
                ProjectWorkingStatus ws = new ProjectWorkingStatus();
                ws.setProject(saved);
                ws.setName(ds[0]);
                ws.setCode(ds[1]);
                ws.setColor(ds[2]);
                ws.setDisplayOrder(Integer.parseInt(ds[3]));
                ws.setVoided(false);
                projectWorkingStatusRepository.save(ws);
            }
        }

        return new ProjectResponse(projectRepository.findById(saved.getId()).orElse(saved));
    }

    @Override
    public void deleteProject(UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dự án với ID: " + id));
        project.setVoided(true);
        projectRepository.save(project);
    }

    @Override
    public void finishProject(UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dự án với ID: " + id));
        project.setIsFinished(true);
        project.setEndDate(LocalDate.now());
        projectRepository.save(project);
    }

    @Override
    public void unfinishProject(UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dự án với ID: " + id));
        project.setIsFinished(false);
        project.setEndDate(null);
        projectRepository.save(project);
    }

    @Override
    public List<StaffDto> getProjectStaffs(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .filter(p -> p.getVoided() == null || !p.getVoided())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dự án với ID: " + projectId));

        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            return new ArrayList<>();
        }

        boolean isSystemManager = securityUtils.isManagerOrAdmin(currentUser);
        Staff currentStaff = currentUser.getStaff();

        boolean isProjectManager = false;
        if (currentStaff != null) {
            isProjectManager = project.getProjectStaffs().stream()
                    .filter(ps -> ps.getVoided() == null || !ps.getVoided())
                    .anyMatch(ps -> ps.getStaff() != null 
                            && ps.getStaff().getId().equals(currentStaff.getId()) 
                            && ps.getProjectRole() == ProjectRole.MANAGER);
        }

        if (isSystemManager || isProjectManager) {
            return project.getProjectStaffs().stream()
                    .filter(ps -> ps.getVoided() == null || !ps.getVoided())
                    .map(ps -> ps.getStaff() != null ? new StaffDto(ps.getStaff()) : null)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } else {
            List<StaffDto> staffs = new ArrayList<>();
            if (currentStaff != null) {
                boolean isMemberOfProject = project.getProjectStaffs().stream()
                        .filter(ps -> ps.getVoided() == null || !ps.getVoided())
                        .anyMatch(ps -> ps.getStaff() != null 
                                && ps.getStaff().getId().equals(currentStaff.getId()));
                if (isMemberOfProject) {
                    staffs.add(new StaffDto(currentStaff));
                }
            }
            return staffs;
        }
    }

}
