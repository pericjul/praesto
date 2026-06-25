package ch.zhaw.praesto.model;

/**
 * Öffentliche Sicht auf einen User (ohne passwordHash). Wird in Auth-Responses
 * und vom /api/users/me Endpoint verwendet.
 */
public record UserDTO(
        String id,
        String firstName,
        String lastName,
        String email,
        String role,
        String schoolId,
        boolean active) {

    public static UserDTO from(User user) {
        return new UserDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().name() : null,
                user.getSchoolId(),
                user.isActive());
    }
}
