package cloud.chlora.management.user.repository;

import cloud.chlora.management.user.domain.entity.User;
import cloud.chlora.management.user.domain.enums.UserRole;
import cloud.chlora.management.user.domain.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final JdbcClient jdbcClient;

    private static final Set<String> allowedSortColumns = Set.of("u.name", "u.role", "u.email", "u.created_at");
    private static final Set<String> allowedDirections = Set.of("ASC", "DESC");

    public List<User> findAllWithFilter(
            String search,
            UserRole role,
            UserStatus status,
            String sortColumn,
            String sortDirection,
            int limit,
            int offset
    ) {
        var sql = new StringBuilder("""
            SELECT *
            FROM users u
            WHERE 1=1
            """);

        var params = new HashMap<String, Object>();
        appendFilters(sql, params, search, role, status);

        var sorting = resolveSorting(sortColumn, sortDirection);

        sql.append(" ORDER BY ").append(sorting.getFirst()).append(" ").append(sorting.getSecond());
        sql.append(" LIMIT :limit OFFSET :offset");

        params.put("limit", limit);
        params.put("offset", offset);

        return jdbcClient.sql(sql.toString())
                .params(params)
                .query(User.class)
                .list();
    }

    public long countUsers(String search, UserRole role, UserStatus status) {
        var sql = new StringBuilder("""
            SELECT COUNT(*)
            FROM users u
            WHERE 1=1
            """);

        var params = new HashMap<String, Object>();
        appendFilters(sql, params, search, role, status);

        return jdbcClient.sql(sql.toString())
                .params(params)
                .query(Long.class)
                .single();
    }

    private Pair<String, String> resolveSorting(String sortColumn, String sortDirection) {
        String column = allowedSortColumns.contains(sortColumn) ? sortColumn : "u.created_at";
        String direction = allowedDirections.contains(
                sortDirection != null ? sortDirection.toUpperCase() : ""
        ) ? sortDirection.toUpperCase() : "DESC";

        return Pair.of(column, direction);
    }

    private void appendFilters(
            StringBuilder sql,
            Map<String, Object> params,
            String search,
            UserRole role,
            UserStatus status
    ) {
        switch (status) {
            case ACTIVE -> sql.append(" AND u.deleted_at IS NULL");
            case DELETED -> sql.append(" AND u.deleted_at IS NOT NULL");
        }

        if (role != null) {
            sql.append(" AND u.role = :role");
            params.put("role", role.name());
        }

        if (search != null && !search.isBlank()) {
            sql.append("""
                 AND (
                    LOWER(u.name) LIKE LOWER(:search)
                    OR LOWER(u.role) LIKE LOWER(:search)
                    OR LOWER(u.email) LIKE LOWER(:search)
                    OR LOWER(u.user_id) LIKE LOWER(:search)
                )
                """);
            params.put("search", "%" + search + "%");
        }
    }

    public Optional<User> findOneByUserId(String userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        return jdbcClient.sql(sql)
                .param(userId)
                .query(User.class)
                .optional();
    }

    public Optional<User> findOneByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        return jdbcClient.sql(sql)
                .param(email)
                .query(User.class)
                .optional();
    }

    public User save(User user) {
        String sql = """
                INSERT INTO users (name, email, role, password)
                VALUES (:name, :email, :role, :password)
                RETURNING id, user_id, email, password, name, role, created_at, updated_at, deleted_at
                """;

        return jdbcClient.sql(sql)
                .param("name", user.getName())
                .param("email", user.getEmail())
                .param("role", user.getRole().name())
                .param("password", user.getPassword())
                .query(User.class)
                .single();
    }

    public int delete(String userId) {
        String sql = "UPDATE users SET deleted_at = NOW() WHERE user_id = ?";
        return jdbcClient.sql(sql)
                .param(userId)
                .update();
    }

    public int restoreUser(String userId) {
        String sql = "UPDATE users SET deleted_at = NULL WHERE user_id = ?";
        return jdbcClient.sql(sql)
                .param(userId)
                .update();
    }
}
