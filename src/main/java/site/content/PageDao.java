package site.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import site.user.AppUser;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static site.common.JdbcUtils.getStringOrNull;

/**
 * Created by Repnox on 4/7/2018.
 */
@Component
public class PageDao {

    public static final String INSERT_PAGE = "insert into PAGE " +
                    "(id, created_dt, owner_id, name, title, content) values " +
                    "(? , ?         , ?       , ?   , ?    , ?      )";

    public static final String UPDATE_PAGE = "update PAGE set " +
            "title = ?, name = ?, content = ? where id = ?";

    public static final String SELECT_PAGE_BY_NAME = "select * from PAGE where name = ?";

    public static final String SELECT_PAGE_BY_ID = "select * from PAGE where id = ?";

    public static final String SELECT_PAGES_BY_OWNER = "select id, title, name from PAGE where owner_id = ?";


    @Autowired
    JdbcTemplate jdbcTemplate;

    public List<Page> getPagesForUser(AppUser owner) {
        return jdbcTemplate.query(SELECT_PAGES_BY_OWNER, new Object[]{owner.getId()}, new PageListResultSetExtractor());

    }

    public void savePage(AppUser owner, Page page) {
        page.setId(UUID.randomUUID().toString());
        page.setOwnerId(owner.getId());
        jdbcTemplate.update(INSERT_PAGE,
                page.getId(), new Date(), page.getOwnerId(), page.getName(), page.getTitle(), page.getMainContent());
    }

    public void updatePage(Page page) {
        jdbcTemplate.update(UPDATE_PAGE, page.getTitle(), page.getName(), page.getMainContent(), page.getId());
    }

    public Page getPageByName(String name) {
        return jdbcTemplate.query(SELECT_PAGE_BY_NAME, new Object[]{name}, new ResultSetExtractor<Page>() {
            @Override
            public Page extractData(ResultSet rs) throws SQLException, DataAccessException {
                if (rs.next()) {
                    return mapPage(rs);
                }
                return null;
            }
        });
    }

    public Page getPageById(String id) {
        return jdbcTemplate.query(SELECT_PAGE_BY_ID, new Object[]{id}, new ResultSetExtractor<Page>() {
            @Override
            public Page extractData(ResultSet rs) throws SQLException, DataAccessException {
                if (rs.next()) {
                    return mapPage(rs);
                }
                return null;
            }
        });
    }

    public List<Page> getFrontPageContent() {
        return jdbcTemplate.query("select * from PAGE order by created_dt desc", new PageListResultSetExtractor());
    }

    private static Page mapPage(ResultSet rs) throws SQLException {
        Page page = new Page();
        page.setId(rs.getString("id"));
        page.setOwnerId(getStringOrNull(rs, "owner_id"));
        page.setName(rs.getString("name"));
        page.setTitle(rs.getString("title"));
        page.setMainContent(getStringOrNull(rs, "content"));
        return page;
    }

    private static class PageListResultSetExtractor implements ResultSetExtractor<List<Page>> {
        @Override
        public List<Page> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            List<Page> pages = new ArrayList<>();
            while(resultSet.next()) {
                pages.add(mapPage(resultSet));
            }
            return pages;
        }
    }

}
