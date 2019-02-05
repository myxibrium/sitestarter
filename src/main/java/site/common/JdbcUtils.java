package site.common;

import java.sql.ResultSet;

/**
 * Created by Repnox on 4/17/2018.
 */
public class JdbcUtils {

    public static String getStringOrNull(ResultSet rs, String name) {
        try {
            return rs.getString(name);
        } catch (Exception e) {
            return null;
        }
    }

}
