package com.minitwit.dao;

import static com.minitwit.db.Tables.FOLLOWER;
import static com.minitwit.db.Tables.MESSAGE;
import static com.minitwit.db.Tables.USER;
import static org.jooq.SQLDialect.H2;
import static org.jooq.SQLDialect.HSQLDB;
import static org.jooq.impl.DSL.select;

import java.sql.Timestamp;
import java.util.List;

import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import com.minitwit.model.Message;
import com.minitwit.model.User;
import com.minitwit.util.GravatarUtil;

public class MessageDao {
	private static final String GRAVATAR_DEFAULT_IMAGE_TYPE = "monsterid";
	private static final int GRAVATAR_SIZE = 48;
	private DSLContext sql;

	public MessageDao(DataSource ds) {
		sql = DSL.using(ds, H2);
	}

	public List<Message> getUserTimelineMessages(User user) {
		return sql.select()
				  .from(MESSAGE, USER)
				  .where(USER.USER_ID.eq(MESSAGE.AUTHOR_ID))
				  .and(USER.USER_ID.eq(user.getId()))
				  .orderBy(MESSAGE.PUB_DATE.desc())
				  .fetch(messageMapper);
	}

	public List<Message> getUserFullTimelineMessages(User user) {
		return sql.select()
				  .from(MESSAGE, USER)
				  .where(MESSAGE.AUTHOR_ID.eq(USER.USER_ID))
				  .and(USER.USER_ID.eq(user.getId())
					  .or(USER.USER_ID.in(select(FOLLOWER.FOLLOWEE_ID).from(FOLLOWER).where(FOLLOWER.FOLLOWER_ID.eq(user.getId()))))
				  )
				  .orderBy(MESSAGE.PUB_DATE.desc())
				  .fetch(messageMapper);
	}

	public List<Message> getPublicTimelineMessages() {
		return sql.select()
				  .from(MESSAGE, USER)
				  .where(MESSAGE.AUTHOR_ID.eq(USER.USER_ID))
				  .orderBy(MESSAGE.PUB_DATE.desc())
				  .fetch(messageMapper);
	}

	public void insertMessage(Message m) {
		sql.insertInto(MESSAGE)
		   .columns(MESSAGE.AUTHOR_ID, MESSAGE.TEXT, MESSAGE.PUB_DATE)
		   .values(m.getUserId(), m.getText(), new Timestamp(m.getPubDate().getTime()))
		   .execute();
	}

	private RecordMapper<Record, Message> messageMapper = r -> {
		Message m = new Message();

		m.setId(r.getValue(MESSAGE.MESSAGE_ID));
		m.setUserId(r.getValue(MESSAGE.AUTHOR_ID));
		m.setUsername(r.getValue(USER.USERNAME));
		m.setText(r.getValue(MESSAGE.TEXT));
		m.setPubDate(r.getValue(MESSAGE.PUB_DATE));
		m.setGravatar(GravatarUtil.gravatarURL(r.getValue(USER.EMAIL), GRAVATAR_DEFAULT_IMAGE_TYPE, GRAVATAR_SIZE));

		return m;
	};
}
