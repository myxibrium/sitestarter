package site.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import site.security.AccessException;
import site.user.AppUser;

import java.net.URLConnection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by Repnox on 4/7/2018.
 */
@Component
public class MediaDao {

    public static final String INSERT_MEDIA = "insert into MEDIA " +
            "(id, created_dt, owner_id, filename, mimetype, content) values " +
            "(? , ?         , ?       , ?       , ?       , ?      )";

    public static final String DELETE_MEDIA = "delete from MEDIA where id = ?";

    @Autowired
    JdbcTemplate jdbcTemplate;

    public void saveMedia(AppUser owner, Media media) {
        media.setId(UUID.randomUUID().toString());
        media.setOwnerId(owner.getId());

        if (media.getMimeType() == null) {
            media.setMimeType(URLConnection.guessContentTypeFromName(media.getFilename()));
        }

        jdbcTemplate.update(INSERT_MEDIA, media.getId(), new Date(), media.getOwnerId(), media.getFilename(), media.getMimeType(), media.getBinary());
    }

    public void deleteMedia(AppUser user, String id) {
        Media media = getMediaById(id);
        if (user.getRoles().contains("ADMIN") || media.getOwnerId().equals(user.getId())) {
            jdbcTemplate.update(DELETE_MEDIA, id);
        } else {
            throw new AccessException();
        }
    }

    public Media getMediaById(String id) {
        return jdbcTemplate.query("select * from MEDIA where id = ?", new Object[]{id}, new ResultSetExtractor<Media>() {

            @Override
            public Media extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                if (resultSet.next()) {
                    Media media = new Media();
                    media.setFilename(resultSet.getString("filename"));
                    media.setMimeType(resultSet.getString("mimetype"));
                    media.setBinary(resultSet.getBinaryStream("content"));
                    return media;
                } else {
                    return null;
                }
            }
        });
    }

    public List<Media> listMediaByOwner(AppUser user) {
        return jdbcTemplate.query("select id, filename, mimetype from MEDIA where owner_id = ?", new Object[]{user.getId()}, new ResultSetExtractor<List<Media>>() {

            @Override
            public List<Media> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                List<Media> list = new ArrayList<>();
                while (resultSet.next()) {
                    Media media = new Media();
                    media.setId(resultSet.getString("id"));
                    media.setFilename(resultSet.getString("filename"));
                    media.setMimeType(resultSet.getString("mimetype"));
                    list.add(media);
                }
                return list;
            }
        });
    }

}
