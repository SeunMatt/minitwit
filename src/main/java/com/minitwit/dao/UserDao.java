package com.minitwit.dao;

import static com.minitwit.db.Tables.FOLLOWER;
import static com.minitwit.db.Tables.USER;
import static org.jooq.SQLDialect.H2;
import static org.jooq.impl.DSL.selectOne;

import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.RecordMapper;
import org.jooq.impl.DSL;

import com.minitwit.model.User;

public class UserDao {

	private DSLContext sql;

	public UserDao(DataSource ds) {
		sql = DSL.using(ds, H2);
	}

	public User getUserbyUsername(String username) {
		return sql.select()
				  .from(USER)
				  .where(USER.USERNAME.eq(username))
				  .fetchOne(userMapper);
	}

	public void insertFollower(User follower, User followee) {
		sql.insertInto(FOLLOWER)
		   .columns(FOLLOWER.FOLLOWER_ID, FOLLOWER.FOLLOWEE_ID)
		   .values(follower.getId(), followee.getId())
		   .execute();
	}

	public void deleteFollower(User follower, User followee) {
		sql.deleteFrom(FOLLOWER)
		   .where(FOLLOWER.FOLLOWER_ID.eq(follower.getId()))
		   .and(FOLLOWER.FOLLOWEE_ID.eq(followee.getId()))
		   .execute();
	}

	public boolean isUserFollower(User follower, User followee) {
		return sql.fetchExists(
				selectOne()
				.from(FOLLOWER)
				.where(FOLLOWER.FOLLOWER_ID.eq(follower.getId()))
				.and(FOLLOWER.FOLLOWEE_ID.eq(followee.getId()))
		);
	}

	public void registerUser(User user) {
		sql.insertInto(USER)
		   .columns(USER.USERNAME, USER.EMAIL, USER.PW)
		   .values(user.getUsername(), user.getEmail(), user.getPassword())
		   .execute();
	}

	private RecordMapper<Record, User> userMapper = r -> {
		User u = new User();

		u.setId(r.getValue(USER.USER_ID));
		u.setEmail(r.getValue(USER.EMAIL));
		u.setUsername(r.getValue(USER.USERNAME));
		u.setPassword(r.getValue(USER.PW));

		return u;
	};
}
