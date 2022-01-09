package com.learn.betterreads.userbooks;

import org.springframework.data.cassandra.repository.CassandraRepository;

public interface UserBooksRepo extends CassandraRepository<UserBooks, UserBooksPrimaryKey> {}