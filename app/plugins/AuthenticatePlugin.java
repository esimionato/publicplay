package plugins;

import java.util.Date;

import models.User;
import models.dao.UserDAO;
import play.Application;
import play.Logger;
import play.Logger.ALogger;

import com.feth.play.module.pa.service.UserServicePlugin;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.user.ExtendedIdentity;

public class AuthenticatePlugin extends UserServicePlugin {
	
	private static ALogger log = Logger.of(AuthenticatePlugin.class);

	private static AuthenticatePlugin instance;// plugin instance

	private UserDAO userDAO;


	public AuthenticatePlugin(Application app) {
		super(app);
		if (log.isInfoEnabled())
			log.debug(getClass().getSimpleName() + " created.");
	}

    @Override
    public Object save(final AuthUser authUser) {
    	if (log.isDebugEnabled())
			log.debug("save <-");
    	if (log.isDebugEnabled())
			log.debug("authUser : " + authUser);
    	String userKey = User.getKey(authUser.getProvider(), authUser.getId());
		User user = userDAO.get(userKey);
		if (log.isDebugEnabled())
			log.debug("user : " + user);
		
        if (user == null) {
        	user = new User();
        	user.setKey(authUser.getProvider(), authUser.getId());
        	
			if (authUser instanceof ExtendedIdentity) {
    			// Remember, even when getting them from FB & Co., emails should be
    			// verified within the application as a security breach there might
    			// break your security as well!
    			//TODO:user.setEmailValidated(false);
    			user.setEmail(((ExtendedIdentity) authUser).getEmail());
				user.setFirstName(((ExtendedIdentity)authUser).getFirstName());
				user.setLastName(((ExtendedIdentity)authUser).getLastName());
				user.setGender(((ExtendedIdentity)authUser).getGender());
			}
    		
        	userDAO.create(user);
            return userKey;
        } else {
            // we have this user already, so return null
			user.setLoginCount(user.getLoginCount() + 1);
			user.setLastLogin(new Date());
			userDAO.update(userKey, user);
            return null;
        }
    }

	@Override
	public AuthUser update(final AuthUser authUser) {
		// User logged in again, bump last login date
    	if (log.isDebugEnabled())
			log.debug("authUser : " + authUser);
    	String userKey = User.getKey(authUser.getProvider(), authUser.getId());
		User user = userDAO.get(userKey);
		if (log.isDebugEnabled())
			log.debug("user : " + user);
        if (user != null) {
			user.setLastLogin(new Date());
			userDAO.update(userKey, user);
        }
    	return super.update(authUser);
	}
	
    @Override
    public Object getLocalIdentity(final AuthUserIdentity identity) {
    	String userKey = User.getKey(identity.getProvider(), identity.getId());
		User user = userDAO.get(userKey);
		if (log.isDebugEnabled())
			log.debug("user : " + user);
        if(user != null) {
            return user.getKey();
        } else {
            return null;
        }
    }

    @Override
    public AuthUser merge(final AuthUser newUser, final AuthUser oldUser) {
        if (!oldUser.equals(newUser)) {
            //TODO: User.merge(oldUser, newUser);
        }
        return oldUser;
    }

    @Override
    public AuthUser link(final AuthUser oldUser, final AuthUser newUser) {
        //TODO: User.addLinkedAccount(oldUser, newUser);
        return null;
    }
    
    
	public User find(String userKey) {
		if (log.isDebugEnabled())
			log.debug("find() <-");
		if (log.isDebugEnabled())
			log.debug("userKey : " + userKey);
		
		final User user = userDAO.get(userKey);
		if (log.isDebugEnabled())
			log.debug("user : " + user);
		return user;
	}

	@Override
	public boolean enabled() {
		return true;
	}

	@Override
	public void onStart() {
		instance = this;
		userDAO = GuicePlugin.getInstance().getInstance(UserDAO.class);

		if (log.isInfoEnabled())
			log.debug(getClass().getSimpleName() + " started.");

	}

	@Override
	public void onStop() {
		instance = null;
		userDAO = null;
		
		if (log.isInfoEnabled())
			log.debug(getClass().getSimpleName() + " stopped.");
	}

	public static AuthenticatePlugin getInstance() {
		return instance;
	}

}
