package controllers.crud;

import models.User;
import models.dao.UserDAO;
import play.mvc.Call;
import play.utils.crud.CRUDController;

public class UserController extends CRUDController<String, User> {

	public UserController(UserDAO dao) {
		super(dao, form(User.class), String.class, User.class);
	}

	@Override
	protected String templateForForm() {
		return "userForm";
	}

	@Override
	protected String templateForList() {
		return "userList";
	}

	@Override
	protected String templateForShow() {
		return "userShow";
	}

	@Override
	protected Call toIndex() {
		return crudIndex();
	}

	public static Call crudIndex() {
		return CategoryController.crudIndex();
	}

}