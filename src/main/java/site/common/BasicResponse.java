package site.common;

/**
 * Created by Repnox on 4/16/2018.
 */
public class BasicResponse {

    public BasicResponse(String status) {
        this.status = status;
    }

    private final String status;

    public String getStatus() {
        return status;
    }
}
