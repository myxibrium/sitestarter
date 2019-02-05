package site.content;

import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.common.NotFoundException;
import site.security.AccessException;
import site.security.Authorizer;
import site.user.AppUser;
import site.user.UserUtils;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Repnox on 4/7/2018.
 */
@RestController
public class MediaController {

    private static Logger logger = LoggerFactory.getLogger(MediaController.class);

    @Autowired
    private Authorizer authorizer;

    @Autowired
    private MediaDao mediaDao;

    public static class MediaPair {
        private String id;
        private String thumbnailId;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getThumbnailId() {
            return thumbnailId;
        }

        public void setThumbnailId(String thumbnailId) {
            this.thumbnailId = thumbnailId;
        }
    }

    @RequestMapping(value = "/api/media", method = RequestMethod.POST, consumes = MediaType.ALL_VALUE)
    public @ResponseBody List<MediaPair> postMedia(HttpServletRequest request, HttpServletResponse response, @RequestParam("files") MultipartFile[] uploads) throws IOException {
        AppUser user = authorizer.authorUser();

        List<MediaPair> mediaPairs = new ArrayList<MediaPair>();

        for (MultipartFile file : uploads) {
            Media media = new Media();
            media.setFilename(file.getOriginalFilename());
            media.setMimeType(file.getContentType());
            media.setBinary(file.getInputStream());
            mediaDao.saveMedia(user, media);

            MediaPair mediaPair = new MediaPair();
            mediaPair.setId(media.getId());
            mediaPairs.add(mediaPair);
        }

        for (MediaPair mediaPair : mediaPairs) {
            Media media = mediaDao.getMediaById(mediaPair.getId());
            BufferedImage image = ImageIO.read(media.getBinary());
            Dimension newSize = calculateNewSize(image);
            if (newSize != null) {
                PipedInputStream thumbnailInput = new PipedInputStream();
                PipedOutputStream thumbnailOutput = new PipedOutputStream(thumbnailInput);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BufferedImage thumbnail = Scalr.resize(image, Scalr.Method.QUALITY, 500, null);
                        String formatName = media.getFilename().substring(media.getFilename().lastIndexOf(".")+1);
                        try {
                            ImageIO.write(thumbnail, formatName, thumbnailOutput);
                            thumbnailOutput.flush();
                            thumbnailOutput.close();
                        } catch (IOException e) {
                            logger.error("Unable to write the thumbnail image.", e);
                        }
                    }
                }).start();
                Media thumbnail = new Media();
                thumbnail.setFilename("thumbnail_"+media.getFilename());
                thumbnail.setMimeType(media.getMimeType());
                thumbnail.setBinary(thumbnailInput);
                mediaDao.saveMedia(user, thumbnail);
                mediaPair.setThumbnailId(thumbnail.getId());
            } else {
                mediaPair.setThumbnailId(mediaPair.getId());
            }
        }

        return mediaPairs;
    }

    private Dimension calculateNewSize(BufferedImage image) {
        int newWidth;
        int newHeight;
        if (image.getWidth() > 500) {
            newWidth = 500;
            newHeight = (int) ( ((double)image.getHeight() / image.getWidth())*newWidth );
        } else if (image.getHeight() > 500) {
            newHeight = 500;
            newWidth = (int) ( ((double) image.getWidth() / image.getHeight())*newHeight );
        } else {
            return null;
        }
        return new Dimension(newWidth, newHeight);
    }

    @RequestMapping(value="/api/media/{id}", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> getMedia(HttpServletResponse response, @PathVariable String id) throws IOException {
        Media media = mediaDao.getMediaById(id);
        if (media == null) {
            throw new NotFoundException();
        }

        response.setContentType(media.getMimeType());

        return ResponseEntity.ok().contentType(MediaType.parseMediaType(media.getMimeType()))
                .body(new InputStreamResource(media.getBinary()));
    }

    @RequestMapping(value="/api/user/media", method = RequestMethod.GET)
    public @ResponseBody List<Media> getUserMedia() throws IOException {
        AppUser user = authorizer.loggedInUser();
        return mediaDao.listMediaByOwner(user);
    }

    @RequestMapping(value = "/api/media/{id}", method = RequestMethod.DELETE)
    public void deleteMedia(@PathVariable String id) {
        AppUser user = authorizer.loggedInUser();
        mediaDao.deleteMedia(user, id);
    }

}
