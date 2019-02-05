package site.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import site.common.NotFoundException;
import site.security.Authorizer;
import site.user.AppUser;
import site.security.AccessException;
import site.user.UserUtils;

import java.util.List;

/**
 * Created by Repnox on 4/7/2018.
 */
@RestController
public class PageController {

    @Autowired
    private Authorizer authorizer;

    @Autowired
    private PageDao pageDao;

    @RequestMapping(value = "/api/page", method = RequestMethod.POST)
    public @ResponseBody String savePage(@RequestBody Page page) {
        AppUser user = authorizer.authorUser();

        page.setName(page.getName().toLowerCase().replaceAll("\\s+", "-").replaceAll("[^A-Za-z0-9$\\-_.+*'(),]", ""));

        page.setOwnerId(user.getId());

        pageDao.savePage(user, page);
        return page.getId();
    }

    @RequestMapping(value = "/api/page", method = RequestMethod.PUT)
    public void updatePage(@RequestBody Page inputPage) {
        AppUser user = authorizer.authorUser();
        Page page = pageDao.getPageById(inputPage.getId());
        if (page.getOwnerId().equals(user.getId())) {
            pageDao.updatePage(inputPage);
        } else {
            throw new AccessException();
        }
    }

    @RequestMapping(value = "/api/user/pages", method = RequestMethod.GET)
    public @ResponseBody List<Page> getPagesForCurrentUser() {
        AppUser user = authorizer.loggedInUser();
        return pageDao.getPagesForUser(user);
    }

    @RequestMapping(value = "/api/page/name_{name}", method = RequestMethod.GET)
    public @ResponseBody Page getPageByName(@PathVariable String name) {
        Page page = pageDao.getPageByName(name);
        if (page != null) {
            return page;
        } else {
            throw new NotFoundException();
        }
    }

    @RequestMapping(value = "/api/page/home", method = RequestMethod.GET)
    public @ResponseBody List<Page> getHomePageContent() {
        return pageDao.getFrontPageContent();
    }

    @RequestMapping(value = "/api/page/id_{id}", method = RequestMethod.GET)
    public Page getPageById(@PathVariable String id) {
        return pageDao.getPageById(id);
    }


}
