package test;

import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;

import org.junit.Test;

import play.mvc.Result;

public class HomeControllerTest {

	@Test
	public void testIndex() {
		running(fakeApplication(), new Runnable() {
			public void run() {
				Result result = callAction(controllers.routes.ref.App.index());
				assertThat(status(result)).isEqualTo(OK);
				assertThat(contentType(result)).isEqualTo("text/html");
				assertThat(charset(result)).isEqualTo("utf-8");
				assertThat(contentAsString(result)).contains("home");
			}
		});
	}

}
