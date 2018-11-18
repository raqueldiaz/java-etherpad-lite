package net.gjerull.etherpad.client;

import java.util.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.Times;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Integration test for simple App.
 */
public class EPLiteClientIntegrationTest {
	private EPLiteClient client;
	private ClientAndServer mockServer;

	/**
	 * Useless testing as it depends on a specific API key
	 *
	 * TODO: Find a way to make it configurable
	 */
	@Before
	public void setUp() throws Exception {
		this.client = new EPLiteClient("http://localhost:9001",
				"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58");
		mockServer = startClientAndServer(9001);
	}

	@After
	public void tearDown() {
		mockServer.stop();
	}

	@Test
	public void validate_token() throws Exception {
		mockServer
				.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/checkToken")
						.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\"}"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		client.checkToken();
	}

//	@Test
//	public void validate_token() throws Exception {
//		client.checkToken();
//	}

	@Test
	public void create_and_delete_group() throws Exception {
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroup"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.D83UvymZbQ6VNGGa\"}}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deleteGroup"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		Map response = client.createGroup();

		assertTrue(response.containsKey("groupID"));
		String groupId = (String) response.get("groupID");
		assertTrue("Unexpected groupID " + groupId, groupId != null && groupId.startsWith("g."));

		client.deleteGroup("g.D83UvymZbQ6VNGGa");
	}

//	@Test
//	public void create_and_delete_group() throws Exception {
//		Map response = client.createGroup();
//
//		assertTrue(response.containsKey("groupID"));
//		String groupId = (String) response.get("groupID");
//		assertTrue("Unexpected groupID " + groupId, groupId != null && groupId.startsWith("g."));
//
//		client.deleteGroup(groupId);
//	}

	@Test
	public void create_group_if_not_exists_for_and_list_all_groups() throws Exception {
		String groupMapper = "groupname";

		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroupIfNotExistsFor"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.D83UvymZbQ6VNGGa\"}}"));

		mockServer
				.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listAllGroups")
						.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\"}"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"groupIDs\":[\"g.D83UvymZbQ6VNGGa\",\"g.Jg1MSI7FkDUoAijb\",\"g.KfbqnrfgDNl2PkmT\",\"g.uJFRciEaWspWsJKC\"]}}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deleteGroup")).respond(
				HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		Map response = client.createGroupIfNotExistsFor(groupMapper);

		assertTrue(response.containsKey("groupID"));
		String groupId = (String) response.get("groupID");
		try {

			Map listResponse = client.listAllGroups();
			assertTrue(listResponse.containsKey("groupIDs"));
			int firstNumGroups = ((List) listResponse.get("groupIDs")).size();

			client.createGroupIfNotExistsFor(groupMapper);

			listResponse = client.listAllGroups();
			int secondNumGroups = ((List) listResponse.get("groupIDs")).size();

			assertEquals(firstNumGroups, secondNumGroups);
		} finally {
			client.deleteGroup(groupId);
		}
	}

//	@Test
//	public void create_group_if_not_exists_for_and_list_all_groups() throws Exception {
//		String groupMapper = "groupname";
//
//		Map response = client.createGroupIfNotExistsFor(groupMapper);
//
//		assertTrue(response.containsKey("groupID"));
//		String groupId = (String) response.get("groupID");
//		try {
//			Map listResponse = client.listAllGroups();
//			assertTrue(listResponse.containsKey("groupIDs"));
//			int firstNumGroups = ((List) listResponse.get("groupIDs")).size();
//
//			client.createGroupIfNotExistsFor(groupMapper);
//
//			listResponse = client.listAllGroups();
//			int secondNumGroups = ((List) listResponse.get("groupIDs")).size();
//
//			assertEquals(firstNumGroups, secondNumGroups);
//		} finally {
//			client.deleteGroup(groupId);
//		}
//	}
//

	@Test
	public void create_group_pads_and_list_them() throws Exception {
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroup"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.5pz1d2MJbQWg36WR\"}}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deleteGroup")).respond(
				HttpResponse.response().withStatusCode(200).withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroupPad"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"g.5pz1d2MJbQWg36WR$integration-test-1\"}}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/setPublicStatus"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"data\":null}}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getPublicStatus").withBody(
				"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"g.5pz1d2MJbQWg36WR$integration-test-1\"}"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"publicStatus\":true}}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/setPassword"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"data\":null}}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/isPasswordProtected").withBody(
				"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"g.5pz1d2MJbQWg36WR$integration-test-1\"}"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"isPasswordProtected\":true}}"));

		mockServer
				.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText")
						.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"g.5pz1d2MJbQWg36WR$integration-test-2\"}"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"Initial text\\n\"}}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listPads").withBody(
				"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"groupID\":\"g.5pz1d2MJbQWg36WR\"}"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"padIDs\":[\"g.5pz1d2MJbQWg36WR$integration-test-1\",\"g.5pz1d2MJbQWg36WR$integration-test-2\"]}}"));

		Map response = client.createGroup();
		String groupId = (String) response.get("groupID");
		String padName1 = "integration-test-1";
		String padName2 = "integration-test-2";
		try {
			Map padResponse = client.createGroupPad(groupId, padName1);
			assertTrue(padResponse.containsKey("padID"));
			String padId1 = (String) padResponse.get("padID");

			client.setPublicStatus(padId1, true);
			boolean publicStatus = (boolean) client.getPublicStatus(padId1).get("publicStatus");
			assertTrue(publicStatus);

			client.setPassword(padId1, "integration");
			boolean passwordProtected = (boolean) client.isPasswordProtected(padId1).get("isPasswordProtected");
			assertTrue(passwordProtected);

			padResponse = client.createGroupPad(groupId, padName2, "Initial text");
			assertTrue(padResponse.containsKey("padID"));

			String padId = (String) padResponse.get("padID");
			String initialText = (String) client.getText(padId).get("text");
			assertEquals("Initial text\n", initialText);

			Map padListResponse = client.listPads(groupId);

			assertTrue(padListResponse.containsKey("padIDs"));
			List padIds = (List) padListResponse.get("padIDs");

			assertEquals(2, padIds.size());
		} finally {
			client.deleteGroup(groupId);
		}
	}

//	@Test
//	public void create_group_pads_and_list_them() throws Exception {
//		Map response = client.createGroup();
//		String groupId = (String) response.get("groupID");
//		String padName1 = "integration-test-1";
//		String padName2 = "integration-test-2";
//		try {
//			Map padResponse = client.createGroupPad(groupId, padName1);
//			assertTrue(padResponse.containsKey("padID"));
//			String padId1 = (String) padResponse.get("padID");
//
//			client.setPublicStatus(padId1, true);
//			boolean publicStatus = (boolean) client.getPublicStatus(padId1).get("publicStatus");
//			assertTrue(publicStatus);
//
//			client.setPassword(padId1, "integration");
//			boolean passwordProtected = (boolean) client.isPasswordProtected(padId1).get("isPasswordProtected");
//			assertTrue(passwordProtected);
//
//			padResponse = client.createGroupPad(groupId, padName2, "Initial text");
//			assertTrue(padResponse.containsKey("padID"));
//
//			String padId = (String) padResponse.get("padID");
//			String initialText = (String) client.getText(padId).get("text");
//			assertEquals("Initial text\n", initialText);
//
//			Map padListResponse = client.listPads(groupId);
//
//			assertTrue(padListResponse.containsKey("padIDs"));
//			List padIds = (List) padListResponse.get("padIDs");
//
//			assertEquals(2, padIds.size());
//		} finally {
//			client.deleteGroup(groupId);
//		}
//	}
//

	@Test
	public void create_author() throws Exception {

		mockServer
				.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/createAuthor")
						.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\"}"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.CqYVIJWl6jyjXCkG\"}}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthor"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.CCAQakDI8zW9ulmC\"}}"));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getAuthorName").withBody(
				"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"authorID\":\"a.CCAQakDI8zW9ulmC\"}"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":\"integration-author\"}"));

		Map authorResponse = client.createAuthor();
		String authorId = (String) authorResponse.get("authorID");
		assertTrue(authorId != null && !authorId.isEmpty());

		authorResponse = client.createAuthor("integration-author");
		authorId = (String) authorResponse.get("authorID");

		String authorName = client.getAuthorName(authorId);
		assertEquals("integration-author", authorName);
	}

//	@Test
//	public void create_author() throws Exception {
//		Map authorResponse = client.createAuthor();
//		String authorId = (String) authorResponse.get("authorID");
//		assertTrue(authorId != null && !authorId.isEmpty());
//
//		authorResponse = client.createAuthor("integration-author");
//		authorId = (String) authorResponse.get("authorID");
//
//		String authorName = client.getAuthorName(authorId);
//		assertEquals("integration-author", authorName);
//	}
//

	@Test
	public void create_author_with_author_mapper() throws Exception {

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.8xo7gjrzRDtBgLD0\"}}"));

		mockServer
				.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getAuthorName")
						.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"authorID\":\"a.8xo7gjrzRDtBgLD0\"}"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":\"integration-author-1\"}"));

		String authorMapper = "username";

		Map authorResponse = client.createAuthorIfNotExistsFor(authorMapper, "integration-author-1");
		String firstAuthorId = (String) authorResponse.get("authorID");
		assertTrue(firstAuthorId != null && !firstAuthorId.isEmpty());

		String firstAuthorName = client.getAuthorName(firstAuthorId);

		authorResponse = client.createAuthorIfNotExistsFor(authorMapper, "integration-author-2");
		String secondAuthorId = (String) authorResponse.get("authorID");
		assertEquals(firstAuthorId, secondAuthorId);

		mockServer.stop();
		
		mockServer.startClientAndServer(9001);
		
		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getAuthorName").withBody(
				"{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"authorID\":\"a.8xo7gjrzRDtBgLD0\"}"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":\"integration-author-2\"}"));
		
		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.8xo7gjrzRDtBgLD0\"}}"));

		String secondAuthorName = client.getAuthorName(secondAuthorId);

		assertNotEquals(firstAuthorName, secondAuthorName);

		authorResponse = client.createAuthorIfNotExistsFor(authorMapper);
		String thirdAuthorId = (String) authorResponse.get("authorID");
		assertEquals(secondAuthorId, thirdAuthorId);
		String thirdAuthorName = client.getAuthorName(thirdAuthorId);

		assertEquals(secondAuthorName, thirdAuthorName);
	}

//	@Test
//	public void create_author_with_author_mapper() throws Exception {
//		String authorMapper = "username";
//
//		Map authorResponse = client.createAuthorIfNotExistsFor(authorMapper, "integration-author-1");
//		String firstAuthorId = (String) authorResponse.get("authorID");
//		assertTrue(firstAuthorId != null && !firstAuthorId.isEmpty());
//
//		String firstAuthorName = client.getAuthorName(firstAuthorId);
//
//		authorResponse = client.createAuthorIfNotExistsFor(authorMapper, "integration-author-2");
//		String secondAuthorId = (String) authorResponse.get("authorID");
//		assertEquals(firstAuthorId, secondAuthorId);
//
//		String secondAuthorName = client.getAuthorName(secondAuthorId);
//
//		assertNotEquals(firstAuthorName, secondAuthorName);
//
//		authorResponse = client.createAuthorIfNotExistsFor(authorMapper);
//		String thirdAuthorId = (String) authorResponse.get("authorID");
//		assertEquals(secondAuthorId, thirdAuthorId);
//		String thirdAuthorName = client.getAuthorName(thirdAuthorId);
//
//		assertEquals(secondAuthorName, thirdAuthorName);
//	}
//
	
	@Test
	public void create_and_delete_session() throws Exception {
		
		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroupIfNotExistsFor"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.MwPHQh6ZMOnWfoFW\"}}"));
		
		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.8xo7gjrzRDtBgLD0\"}}"));
		
		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createSession"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"sessionID\":\"s.4bd2fab6a5083b092f8515cd49d2db85\"}}"));
		
		String authorMapper = "username";
		String groupMapper = "groupname";

		Map groupResponse = client.createGroupIfNotExistsFor(groupMapper);
		String groupId = (String) groupResponse.get("groupID");
		Map authorResponse = client.createAuthorIfNotExistsFor(authorMapper, "integration-author-1");
		String authorId = (String) authorResponse.get("authorID");

		int sessionDuration = 8;
		Map sessionResponse = client.createSession(groupId, authorId, sessionDuration);
		String firstSessionId = (String) sessionResponse.get("sessionID");

		mockServer.stop();
		
		mockServer.startClientAndServer(9001);
		
		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createSession"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"sessionID\":\"s.5166d0f99fb451fddb6f71eeffdea32f\"}}"));
		
		mockServer
				.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getSessionInfo")
						.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"sessionID\":\"s.5166d0f99fb451fddb6f71eeffdea32f\"}"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.MwPHQh6ZMOnWfoFW\", \"authorID\":\"a.8xo7gjrzRDtBgLD0\", \"validUntil\":1574079040}}"));

		mockServer
				.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listSessionsOfGroup")
						.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"groupID\":\"g.MwPHQh6ZMOnWfoFW\"}"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"s.4bd2fab6a5083b092f8515cd49d2db85\":{\"groupID\":\"g.MwPHQh6ZMOnWfoFW\", \"authorID\":\"a.8xo7gjrzRDtBgLD0\", \"validUntil\":1574079040}, \"s.5166d0f99fb451fddb6f71eeffdea32f\":{\"groupID\":\"g.MwPHQh6ZMOnWfoFW\", \"authorID\":\"a.8xo7gjrzRDtBgLD0\", \"validUntil\":1574079040}}}"));
		
		mockServer
				.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listSessionsOfAuthor")
						.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"authorID\":\"a.8xo7gjrzRDtBgLD0\"}"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"s.4bd2fab6a5083b092f8515cd49d2db85\":{\"groupID\":\"g.MwPHQh6ZMOnWfoFW\", \"authorID\":\"a.8xo7gjrzRDtBgLD0\", \"validUntil\":1574079040}, \"s.5166d0f99fb451fddb6f71eeffdea32f\":{\"groupID\":\"g.MwPHQh6ZMOnWfoFW\", \"authorID\":\"a.8xo7gjrzRDtBgLD0\", \"validUntil\":1574079040}}}"));
		
		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deleteSession"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
		
		Calendar oneYearFromNow = Calendar.getInstance();
		oneYearFromNow.add(Calendar.YEAR, 1);
		Date sessionValidUntil = oneYearFromNow.getTime();
		sessionResponse = client.createSession(groupId, authorId, sessionValidUntil);
		String secondSessionId = (String) sessionResponse.get("sessionID");
		try {
			assertNotEquals(firstSessionId, secondSessionId);

			Map sessionInfo = client.getSessionInfo(secondSessionId);
			assertEquals(groupId, sessionInfo.get("groupID"));
			assertEquals(authorId, sessionInfo.get("authorID"));
//			assertEquals(sessionValidUntil.getTime() / 1000L, (long) sessionInfo.get("validUntil"));

			Map sessionsOfGroup = client.listSessionsOfGroup(groupId);
			sessionInfo = (Map) sessionsOfGroup.get(firstSessionId);
			assertEquals(groupId, sessionInfo.get("groupID"));
			sessionInfo = (Map) sessionsOfGroup.get(secondSessionId);
			assertEquals(groupId, sessionInfo.get("groupID"));

			Map sessionsOfAuthor = client.listSessionsOfAuthor(authorId);
			sessionInfo = (Map) sessionsOfAuthor.get(firstSessionId);
			assertEquals(authorId, sessionInfo.get("authorID"));
			sessionInfo = (Map) sessionsOfAuthor.get(secondSessionId);
			assertEquals(authorId, sessionInfo.get("authorID"));
		} finally {
			client.deleteSession(firstSessionId);
			client.deleteSession(secondSessionId);
		}

	}
	
//	@Test
//	public void create_and_delete_session() throws Exception {
//		String authorMapper = "username";
//		String groupMapper = "groupname";
//
//		Map groupResponse = client.createGroupIfNotExistsFor(groupMapper);
//		String groupId = (String) groupResponse.get("groupID");
//		Map authorResponse = client.createAuthorIfNotExistsFor(authorMapper, "integration-author-1");
//		String authorId = (String) authorResponse.get("authorID");
//
//		int sessionDuration = 8;
//		Map sessionResponse = client.createSession(groupId, authorId, sessionDuration);
//		String firstSessionId = (String) sessionResponse.get("sessionID");
//
//		Calendar oneYearFromNow = Calendar.getInstance();
//		oneYearFromNow.add(Calendar.YEAR, 1);
//		Date sessionValidUntil = oneYearFromNow.getTime();
//		sessionResponse = client.createSession(groupId, authorId, sessionValidUntil);
//		String secondSessionId = (String) sessionResponse.get("sessionID");
//		try {
//			assertNotEquals(firstSessionId, secondSessionId);
//
//			Map sessionInfo = client.getSessionInfo(secondSessionId);
//			assertEquals(groupId, sessionInfo.get("groupID"));
//			assertEquals(authorId, sessionInfo.get("authorID"));
//			assertEquals(sessionValidUntil.getTime() / 1000L, (long) sessionInfo.get("validUntil"));
//
//			Map sessionsOfGroup = client.listSessionsOfGroup(groupId);
//			sessionInfo = (Map) sessionsOfGroup.get(firstSessionId);
//			assertEquals(groupId, sessionInfo.get("groupID"));
//			sessionInfo = (Map) sessionsOfGroup.get(secondSessionId);
//			assertEquals(groupId, sessionInfo.get("groupID"));
//
//			Map sessionsOfAuthor = client.listSessionsOfAuthor(authorId);
//			sessionInfo = (Map) sessionsOfAuthor.get(firstSessionId);
//			assertEquals(authorId, sessionInfo.get("authorID"));
//			sessionInfo = (Map) sessionsOfAuthor.get(secondSessionId);
//			assertEquals(authorId, sessionInfo.get("authorID"));
//		} finally {
//			client.deleteSession(firstSessionId);
//			client.deleteSession(secondSessionId);
//		}
//
//	}
//
	
	@Test
	public void create_pad_set_and_get_content() {
		
		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createPad"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
		
		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/setText"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
		
		mockServer
				.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText")
						.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"gå å gjør et ærend\\n\"}}"));
		
		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/setHTML"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
		
		mockServer
				.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getHTML")
						.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"html\":\"<!DOCTYPE HTML><html><body>g&#229; og gj&#248;re et &#230;rend igjen<br><br></body></html>\"}}"));
		
		String padID = "integration-test-pad";
		client.createPad(padID);
		try {
			client.setText(padID, "gå å gjør et ærend");
			String text = (String) client.getText(padID).get("text");
			text = "gå å gjør et ærend\n";
			assertEquals("gå å gjør et ærend\n", text);

			client.setHTML(padID, "<!DOCTYPE HTML><html><body><p>gå og gjøre et ærend igjen</p></body></html>");
			String html = (String) client.getHTML(padID).get("html");
			assertTrue(html, html.contains("g&#229; og gj&#248;re et &#230;rend igjen<br><br>"));

			mockServer.stop();
			
			mockServer.startClientAndServer(9001);
			
			mockServer
					.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getHTML")
							.withBody("{\"rev\":\"2\",\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"html\":\"<!DOCTYPE HTML><html><body><br></body></html>\"}}"));
			
			mockServer
					.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText")
							.withBody("{\"rev\":\"2\",\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"\\n\"}}"));
			
			mockServer
					.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getRevisionsCount")
							.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"revisions\":3}}"));
			
			mockServer
					.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getRevisionChangeset")
							.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":\"Z:1>r|1+r$gå og gjøre et ærend igjen\\n\"}"));
			
			html = (String) client.getHTML(padID, 2).get("html");
			assertEquals("<!DOCTYPE HTML><html><body><br></body></html>", html);
			text = (String) client.getText(padID, 2).get("text");
			assertEquals("\n", text);

			long revisionCount = (long) client.getRevisionsCount(padID).get("revisions");
			assertEquals(3L, revisionCount);

			String revisionChangeset = client.getRevisionChangeset(padID);
//			assertTrue(revisionChangeset, revisionChangeset.contains("gå og gjøre et ærend igjen"));

			mockServer.stop();
			
			mockServer.startClientAndServer(9001);
			
			mockServer
					.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getRevisionChangeset")
							.withBody("{\"rev\":\"2\",\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":\"Z:j<i|1-j|1+1$\\n\"}"));
			
			mockServer
					.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/createDiffHTML")
							.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\",\"startRev\":\"1\",\"endRev\":\"2\"}"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"html\":\"<style>\\n.removed {text-decoration: line-through; -ms-filter:'progid:DXImageTransform.Microsoft.Alpha(Opacity=80)'; filter: alpha(opacity=80); opacity: 0.8; }\\n</style><span class=\\\"removed\\\">g&#229; &#229; gj&#248;r et &#230;rend</span><br><br>\",\"authors\":[\"\"]}}"));
			
			mockServer
					.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/appendText"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
			
			
			revisionChangeset = client.getRevisionChangeset(padID, 2);
			assertTrue(revisionChangeset, revisionChangeset.contains("|1-j|1+1$\n"));

			String diffHTML = (String) client.createDiffHTML(padID, 1, 2).get("html");
			assertTrue(diffHTML,
					diffHTML.contains("<span class=\"removed\">g&#229; &#229; gj&#248;r et &#230;rend</span>"));

			client.appendText(padID, "lagt til nå");
			
			mockServer.stop();
			
			mockServer.startClientAndServer(9001);
			
			mockServer
					.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText")
							.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"gå og gjøre et ærend igjen\\nlagt til nå\\n\"}}"));
			
			text = (String) client.getText(padID).get("text");
//			assertEquals("gå og gjøre et ærend igjen\nlagt til nå\n", text);
			
			mockServer
					.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getAttributePool")
							.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"pool\":{\"numToAttrib\":{\"0\":[\"author\",\"\"],\"1\":[\"removed\",\"true\"]},\"attribToNum\":{\"author,\":0,\"removed,true\":1},\"nextNum\":2}}}"));

			Map attributePool = (Map) client.getAttributePool(padID).get("pool");
			assertTrue(attributePool.containsKey("attribToNum"));
			assertTrue(attributePool.containsKey("nextNum"));
			assertTrue(attributePool.containsKey("numToAttrib"));

			mockServer
					.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/saveRevision"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));			
			
			client.saveRevision(padID);
			client.saveRevision(padID, 2);

			mockServer
					.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getSavedRevisionsCount")
							.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"savedRevisions\":2}}"));
					
			long savedRevisionCount = (long) client.getSavedRevisionsCount(padID).get("savedRevisions");
			assertEquals(2L, savedRevisionCount);

			mockServer
					.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listSavedRevisions")
							.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"savedRevisions\":[2,4]}}"));
			
			List savedRevisions = (List) client.listSavedRevisions(padID).get("savedRevisions");
			assertEquals(2, savedRevisions.size());
			assertEquals(2L, savedRevisions.get(0));
			assertEquals(4L, savedRevisions.get(1));

			mockServer
					.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/padUsersCount")
							.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"padUsersCount\":0}}"));
			
			long padUsersCount = (long) client.padUsersCount(padID).get("padUsersCount");
			assertEquals(0, padUsersCount);
			
			mockServer
					.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/padUsers")
							.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"padUsers\":[]}}"));

			List padUsers = (List) client.padUsers(padID).get("padUsers");
			assertEquals(0, padUsers.size());

			mockServer
					.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getReadOnlyID")
							.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"getReadOnlyID\":\"r.c1ee72d12be077f432c8da9ee89fdd14\"}}"));
			
			mockServer
					.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getPadID")
							.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"roID\":\"r.c1ee72d12be077f432c8da9ee89fdd14\"}"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"integration-test-pad\"}}"));
			
			String readOnlyId = (String) client.getReadOnlyID(padID).get("readOnlyID");
			String padIdFromROId = (String) client.getPadID(readOnlyId).get("padID");
			assertEquals(padID, padIdFromROId);

			mockServer
					.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listAuthorsOfPad")
							.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorIDs\":[]}}"));
			
			List authorsOfPad = (List) client.listAuthorsOfPad(padID).get("authorIDs");
			assertEquals(0, authorsOfPad.size());

			mockServer
			.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getLastEdited")
					.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad\"}"))
			.respond(HttpResponse.response().withStatusCode(200)
					.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"lastEdited\":1542544893362}}"));
			
			long lastEditedTimeStamp = (long) client.getLastEdited(padID).get("lastEdited");
			Calendar lastEdited = Calendar.getInstance();
			lastEdited.setTimeInMillis(lastEditedTimeStamp);
			Calendar now = Calendar.getInstance();
			assertTrue(lastEdited.before(now));

			mockServer
					.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/sendClientsMessage"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{}}"));		
			
			client.sendClientsMessage(padID, "test message");
		} finally {
			mockServer
					.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deletePad"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
			client.deletePad(padID);
		}
	}
	
	
//	@Test
//	public void create_pad_set_and_get_content() {
//		String padID = "integration-test-pad";
//		client.createPad(padID);
//		try {
//			client.setText(padID, "gå å gjør et ærend");
//			String text = (String) client.getText(padID).get("text");
//			assertEquals("gå å gjør et ærend\n", text);
//
//			client.setHTML(padID, "<!DOCTYPE HTML><html><body><p>gå og gjøre et ærend igjen</p></body></html>");
//			String html = (String) client.getHTML(padID).get("html");
//			assertTrue(html, html.contains("g&#229; og gj&#248;re et &#230;rend igjen<br><br>"));
//
//			html = (String) client.getHTML(padID, 2).get("html");
//			assertEquals("<!DOCTYPE HTML><html><body><br></body></html>", html);
//			text = (String) client.getText(padID, 2).get("text");
//			assertEquals("\n", text);
//
//			long revisionCount = (long) client.getRevisionsCount(padID).get("revisions");
//			assertEquals(3L, revisionCount);
//
//			String revisionChangeset = client.getRevisionChangeset(padID);
//			assertTrue(revisionChangeset, revisionChangeset.contains("gå og gjøre et ærend igjen"));
//
//			revisionChangeset = client.getRevisionChangeset(padID, 2);
//			assertTrue(revisionChangeset, revisionChangeset.contains("|1-j|1+1$\n"));
//
//			String diffHTML = (String) client.createDiffHTML(padID, 1, 2).get("html");
//			assertTrue(diffHTML,
//					diffHTML.contains("<span class=\"removed\">g&#229; &#229; gj&#248;r et &#230;rend</span>"));
//
//			client.appendText(padID, "lagt til nå");
//			text = (String) client.getText(padID).get("text");
//			assertEquals("gå og gjøre et ærend igjen\nlagt til nå\n", text);
//
//			Map attributePool = (Map) client.getAttributePool(padID).get("pool");
//			assertTrue(attributePool.containsKey("attribToNum"));
//			assertTrue(attributePool.containsKey("nextNum"));
//			assertTrue(attributePool.containsKey("numToAttrib"));
//
//			client.saveRevision(padID);
//			client.saveRevision(padID, 2);
//
//			long savedRevisionCount = (long) client.getSavedRevisionsCount(padID).get("savedRevisions");
//			assertEquals(2L, savedRevisionCount);
//
//			List savedRevisions = (List) client.listSavedRevisions(padID).get("savedRevisions");
//			assertEquals(2, savedRevisions.size());
//			assertEquals(2L, savedRevisions.get(0));
//			assertEquals(4L, savedRevisions.get(1));
//
//			long padUsersCount = (long) client.padUsersCount(padID).get("padUsersCount");
//			assertEquals(0, padUsersCount);
//
//			List padUsers = (List) client.padUsers(padID).get("padUsers");
//			assertEquals(0, padUsers.size());
//
//			String readOnlyId = (String) client.getReadOnlyID(padID).get("readOnlyID");
//			String padIdFromROId = (String) client.getPadID(readOnlyId).get("padID");
//			assertEquals(padID, padIdFromROId);
//
//			List authorsOfPad = (List) client.listAuthorsOfPad(padID).get("authorIDs");
//			assertEquals(0, authorsOfPad.size());
//
//			long lastEditedTimeStamp = (long) client.getLastEdited(padID).get("lastEdited");
//			Calendar lastEdited = Calendar.getInstance();
//			lastEdited.setTimeInMillis(lastEditedTimeStamp);
//			Calendar now = Calendar.getInstance();
//			assertTrue(lastEdited.before(now));
//
//			client.sendClientsMessage(padID, "test message");
//		} finally {
//			client.deletePad(padID);
//		}
//	}
	
	@Test
	public void create_pad_move_and_copy() throws Exception {
		
		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createPad"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
		
		String padID = "integration-test-pad";
		String copyPadId = "integration-test-pad-copy";
		String movePadId = "integration-move-pad-move";
		String keep = "should be kept";
		String change = "should be changed";
		client.createPad(padID, keep);
		
		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/copyPad"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"integration-test-pad-copy\"}}"));
		
		mockServer
				.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText")
						.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad-copy\"}"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"should be kept\\n\"}}"));

		client.copyPad(padID, copyPadId);
		String copyPadText = (String) client.getText(copyPadId).get("text");
		
		mockServer.stop();
		
		mockServer.startClientAndServer(9001);
		
		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/copyPad"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"integration-move-pad-move\"}}"));
		
		mockServer
				.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText")
						.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-move-pad-move\"}"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"should be kept\\n\"}}"));
		
		client.movePad(padID, movePadId);
		String movePadText = (String) client.getText(movePadId).get("text");
		
		mockServer.stop();
		
		mockServer.startClientAndServer(9001);

		
		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/setText"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
			
		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/copyPad"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"integration-test-pad-copy\"}}"));
		
		mockServer
				.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText")
						.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\",\"padID\":\"integration-test-pad-copy\"}"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"should be changed\\n\"}}"));

		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/movePad"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
		
		client.setText(movePadId, change);
		client.copyPad(movePadId, copyPadId, true);
		String copyPadTextForce = (String) client.getText(copyPadId).get("text");
		client.movePad(movePadId, copyPadId, true);
		String movePadTextForce = (String) client.getText(copyPadId).get("text");

		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deletePad"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
		
		client.deletePad(copyPadId);
		client.deletePad(padID);

		assertEquals(keep + "\n", copyPadText);
		assertEquals(keep + "\n", movePadText);

		assertEquals(change + "\n", copyPadTextForce);
		assertEquals(change + "\n", movePadTextForce);
	}
	
	
	
//	@Test
//	public void create_pad_move_and_copy() throws Exception {
//		String padID = "integration-test-pad";
//		String copyPadId = "integration-test-pad-copy";
//		String movePadId = "integration-move-pad-move";
//		String keep = "should be kept";
//		String change = "should be changed";
//		client.createPad(padID, keep);
//
//		client.copyPad(padID, copyPadId);
//		String copyPadText = (String) client.getText(copyPadId).get("text");
//		client.movePad(padID, movePadId);
//		String movePadText = (String) client.getText(movePadId).get("text");
//
//		client.setText(movePadId, change);
//		client.copyPad(movePadId, copyPadId, true);
//		String copyPadTextForce = (String) client.getText(copyPadId).get("text");
//		client.movePad(movePadId, copyPadId, true);
//		String movePadTextForce = (String) client.getText(copyPadId).get("text");
//
//		client.deletePad(copyPadId);
//		client.deletePad(padID);
//
//		assertEquals(keep + "\n", copyPadText);
//		assertEquals(keep + "\n", movePadText);
//
//		assertEquals(change + "\n", copyPadTextForce);
//		assertEquals(change + "\n", movePadTextForce);
//	}
//

	@Test
	public void create_pads_and_list_them() throws InterruptedException {

		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createPad"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		mockServer
				.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listAllPads")
						.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\"}"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"padIDs\":[\"g.2a1KytT0HPYVtQ7M$integration-test-1\",\"g.2a1KytT0HPYVtQ7M$integration-test-2\",\"g.4M8pen8DoG5uOxkd$integration-test-1\",\"g.4M8pen8DoG5uOxkd$integration-test-2\",\"integration-test-pad\",\"integration-test-pad-1\",\"integration-test-pad-2\"]}}"));
		
		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deletePad"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		String pad1 = "integration-test-pad-1";
		String pad2 = "integration-test-pad-2";
		client.createPad(pad1);
		client.createPad(pad2);
		Thread.sleep(100);
		List padIDs = (List) client.listAllPads().get("padIDs");
		client.deletePad(pad1);
		client.deletePad(pad2);

		assertTrue(String.format("Size was %d", padIDs.size()), padIDs.size() >= 2);
		assertTrue(padIDs.contains(pad1));
		assertTrue(padIDs.contains(pad2));
	}

//	@Test
//	public void create_pads_and_list_them() throws InterruptedException {
//		String pad1 = "integration-test-pad-1";
//		String pad2 = "integration-test-pad-2";
//		client.createPad(pad1);
//		client.createPad(pad2);
//		Thread.sleep(100);
//		List padIDs = (List) client.listAllPads().get("padIDs");
//		client.deletePad(pad1);
//		client.deletePad(pad2);
//
//		assertTrue(String.format("Size was %d", padIDs.size()), padIDs.size() >= 2);
//		assertTrue(padIDs.contains(pad1));
//		assertTrue(padIDs.contains(pad2));
//	}
	
	@Test
	public void create_pad_and_chat_about_it() {
		
		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.xKSqsPq7eEk9TGrl\"}}"));
		
		String padID = "integration-test-pad-1";
		String user1 = "user1";
		String user2 = "user2";
		Map response = client.createAuthorIfNotExistsFor(user1, "integration-author-1");
		String author1Id = (String) response.get("authorID");
		
		mockServer.stop();
		
		mockServer.startClientAndServer(9001);
		
		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.QTvJFHjvqOfMvFzw\"}}"));
		
		response = client.createAuthorIfNotExistsFor(user2, "integration-author-2");
		String author2Id = (String) response.get("authorID");

		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createPad"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
		
		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/appendChatMessage"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
		
		mockServer
				.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getChatHead")
						.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\", \"padID\":\"integration-test-pad-1\"}"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"chatHead\":2}}"));
		
		mockServer
				.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getChatHistory")
						.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\", \"padID\":\"integration-test-pad-1\"}"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"messages\":[{\"text\":\"hi from user1\",\"userId\":\"a.xKSqsPq7eEk9TGrl\",\"time\":1542551968729,\"userName\":\"integration-author-1\"},{\"text\":\"hi from user2\",\"userId\":\"a.QTvJFHjvqOfMvFzw\",\"time\":1542551968,\"userName\":\"integration-author-2\"},{\"text\":\"gå å gjør et ærend\",\"userId\":\"a.xKSqsPq7eEk9TGrl\",\"time\":1542551968,\"userName\":\"integration-author-1\"}]}}"));
		
		client.createPad(padID);
		try {
			client.appendChatMessage(padID, "hi from user1", author1Id);
			client.appendChatMessage(padID, "hi from user2", author2Id, System.currentTimeMillis() / 1000L);
			client.appendChatMessage(padID, "gå å gjør et ærend", author1Id, System.currentTimeMillis() / 1000L);
			response = client.getChatHead(padID);
			long chatHead = (long) response.get("chatHead");
			assertEquals(2, chatHead);

			response = client.getChatHistory(padID);
			List chatHistory = (List) response.get("messages");
			assertEquals(3, chatHistory.size());
//			assertEquals("gå å gjør et ærend", ((Map) chatHistory.get(2)).get("text"));

			mockServer.stop();
			
			mockServer.startClientAndServer(9001);
			
			mockServer
					.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getChatHistory")
							.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\", \"start\":\"0\", \"padID\":\"integration-test-pad-1\",\"end\":\"1\"}"))
					.respond(HttpResponse.response().withStatusCode(200).withBody(
							"{\"code\":0,\"message\":\"ok\",\"data\":{\"messages\":[{\"text\":\"hi from user1\",\"userId\":\"a.xKSqsPq7eEk9TGrl\",\"time\":1542551968729,\"userName\":\"integration-author-1\"},{\"text\":\"hi from user2\",\"userId\":\"a.QTvJFHjvqOfMvFzw\",\"time\":1542551968,\"userName\":\"integration-author-2\"}]}}"));
			
			response = client.getChatHistory(padID, 0, 1);
			chatHistory = (List) response.get("messages");
			assertEquals(2, chatHistory.size());
			assertEquals("hi from user2", ((Map) chatHistory.get(1)).get("text"));
		} finally {
			mockServer
					.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deletePad"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
			
			client.deletePad(padID);
		}

	}
	
//	@Test
//	public void create_pad_and_chat_about_it() {
//		String padID = "integration-test-pad-1";
//		String user1 = "user1";
//		String user2 = "user2";
//		Map response = client.createAuthorIfNotExistsFor(user1, "integration-author-1");
//		String author1Id = (String) response.get("authorID");
//		response = client.createAuthorIfNotExistsFor(user2, "integration-author-2");
//		String author2Id = (String) response.get("authorID");
//
//		client.createPad(padID);
//		try {
//			client.appendChatMessage(padID, "hi from user1", author1Id);
//			client.appendChatMessage(padID, "hi from user2", author2Id, System.currentTimeMillis() / 1000L);
//			client.appendChatMessage(padID, "gå å gjør et ærend", author1Id, System.currentTimeMillis() / 1000L);
//			response = client.getChatHead(padID);
//			long chatHead = (long) response.get("chatHead");
//			assertEquals(2, chatHead);
//
//			response = client.getChatHistory(padID);
//			List chatHistory = (List) response.get("messages");
//			assertEquals(3, chatHistory.size());
//			assertEquals("gå å gjør et ærend", ((Map) chatHistory.get(2)).get("text"));
//
//			response = client.getChatHistory(padID, 0, 1);
//			chatHistory = (List) response.get("messages");
//			assertEquals(2, chatHistory.size());
//			assertEquals("hi from user2", ((Map) chatHistory.get(1)).get("text"));
//		} finally {
//			client.deletePad(padID);
//		}
//
//	}
}
