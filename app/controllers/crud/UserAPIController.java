package controllers.crud;

import static play.libs.Json.toJson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import models.SecurityRole;
import models.User;
import models.UserFollow;
import models.UserFollowPK;
import models.dao.SecurityRoleDAO;
import models.dao.UserActionDAO;
import models.dao.UserDAO;
import models.dao.UserFollowDAO;

import org.codehaus.jackson.JsonNode;

import play.mvc.Result;
import play.utils.crud.APIController;
import play.utils.dao.EntityNotFoundException;

import com.google.common.collect.ImmutableMap;

import controllers.HttpUtils;

public class UserAPIController extends APIController<String, User> {

	private UserDAO userDAO;
	private UserFollowDAO userFollowDAO;
	private SecurityRoleDAO securityRoleDAO;
	private UserActionDAO userActionDAO;

	@Inject
	public UserAPIController(UserDAO userDAO, UserFollowDAO userFollowDAO,
			SecurityRoleDAO securityRoleDAO,UserActionDAO userActionDAO)   {
		super(userDAO);
		this.userDAO = userDAO;
		this.userFollowDAO = userFollowDAO;
		this.securityRoleDAO = securityRoleDAO;
		this.userActionDAO = userActionDAO;
	}

	@Override
	public Result create() {
		/* TODO:
		Result check = checkRequired("url");
		if (check != null) {
			return check;
		}

		String url = jsonText("url");
		try {
			new URL(url );
		} catch (MalformedURLException e) {
			return badRequest(toJson(ImmutableMap.of(
					"status", "error", 
					"message", e.getMessage())));
		}
		
		User m = new User();
		m.setStatus(User.Status.NEW);
		m.setUrl(url);
		Long key = userDAO.create(m);
		*/
		return TODO;
	}

	public Result bulkList() {
		if (log.isDebugEnabled())
			log.debug("bulkList() <-");
		
		//clean cyclic dependencies
		List<User> list = userDAO.all();
		for (User user : list) {
			user.getPosts().clear();
			user.getComments().clear();
		}
		
		return ok(toJson(list));
	}

	public Result follow(String key) {
		if (log.isDebugEnabled())
			log.debug("follow() <- " + key);
		String sourceKey = HttpUtils.loginUser().getKey();
		UserFollowPK followKey = new UserFollowPK(sourceKey, key);
		UserFollow follow = userFollowDAO.get(followKey);
		if (follow == null) {
			follow = new UserFollow();
			follow.setKey(followKey);
			userFollowDAO.create(follow);
			userActionDAO.addUserAction(userDAO.get(key), "followUser");
		}

		return created(toJson(ImmutableMap.of("status", "OK", "key", key,
				"data", follow)));
	}

	public Result unfollow(String key) {
		if (log.isDebugEnabled())
			log.debug("follow() <- " + key);
		String sourceKey = HttpUtils.loginUser().getKey();
		UserFollowPK followKey = new UserFollowPK(sourceKey, key);
		UserFollow follow = userFollowDAO.get(followKey);
		if (follow != null) {
			try {
				userFollowDAO.remove(followKey);
			} catch (EntityNotFoundException e) {
				return ok(toJson(ImmutableMap.of("status", "not found", "key", key,
						"data", follow)));
			}
		}
		
		return created(toJson(ImmutableMap.of("status", "OK", "key", key,
				"data", follow)));
	}

	public Result roleUpdate(String key) {
		if (log.isDebugEnabled())
			log.debug("roleUpdate <- " + key);

		//field name & value
		String name = jsonText("name");
		List<String> values = jsonTextList("value");
		
		if (log.isDebugEnabled())
			log.debug("name : " + name);
		if (log.isDebugEnabled())
			log.debug("values : " + values);

		User u = userDAO.get(key);
		if (log.isDebugEnabled())
			log.debug("m : " + u);
		if (u == null) {
			return notFound(toJson(ImmutableMap.of(
					"status", "OK",
					"key", key,
					"message", "entity with the given key not found")));
		}

		List<SecurityRole> roles = u.getSecurityRoles();
		if (log.isDebugEnabled())
			log.debug("old roles : " + roles);
		for (SecurityRole role : roles) {
			if (log.isDebugEnabled())
				log.debug("old role : " + role);
		}
		roles.clear();
		
		for (String rolekeyStr : values) {
			try {
				Long roleKey = Long.parseLong(rolekeyStr);
				SecurityRole role = securityRoleDAO.get(roleKey);
				if (role != null) roles.add(role);
			} catch (NumberFormatException e) {
				continue;
			}
		}
		if (log.isDebugEnabled())
			log.debug("new roles : " + roles);
		
		userDAO.saveAssociation(u, "securityRoles");
		//userDAO.update(key, u);
		
		if (log.isDebugEnabled())
			log.debug("updated.");
		if (log.isDebugEnabled())
			log.debug("m : " + u);
		
		u = userDAO.get(key);
		List<SecurityRole> newRoles = u.getSecurityRoles();
		if (log.isDebugEnabled())
			log.debug("newRoles : " + newRoles);
		for (SecurityRole role : newRoles) {
			if (log.isDebugEnabled())
				log.debug("new role : " + role);
		}

		return ok(toJson(ImmutableMap.of(
				"status", "OK",
				"key", key,
				"message", "field[" + name +
				"] updated")));
	}

	protected List<String> jsonTextList(String name) {
		JsonNode json = request().body().asJson();
		JsonNode node = json.get(name);
		if (node == null)
			return null;
		List<String> list = new ArrayList<String>();
		for (Iterator<JsonNode> elems = node.getElements(); elems.hasNext();) {
			JsonNode elem = elems.next();
			if (log.isDebugEnabled())
				log.debug("elem : " + elem);
			list.add(elem.asText());
		}
		return list;
	}	

}
