CREATE TABLE IF NOT EXISTS facebook_users (
  id varchar(255) PRIMARY KEY,
  ndla_id text,
  first_name text,
  middle_name text,
  last_name text,
  email text,
  created timestamp
);

CREATE TABLE IF NOT EXISTS twitter_users (
  id varchar(255) PRIMARY KEY,
  ndla_id text,
  name text,
  first_name text,
  middle_name text,
  last_name text,
  email text,
  created timestamp
);

CREATE TABLE IF NOT EXISTS google_users (
  id varchar(255) PRIMARY KEY,
  ndla_id text,
  first_name text,
  middle_name text,
  last_name text,
  display_name text,
  etag text,
  object_type text,
  email text,
  verified boolean,
  created timestamp
);

CREATE TABLE IF NOT EXISTS ndla_users (
  id uuid PRIMARY KEY,
  first_name text,
  middle_name text,
  last_name text,
  email text,
  created timestamp,
  facebook_id varchar(255),
  google_id varchar(255),
  twitter_id varchar(255)
);

CREATE TABLE IF NOT EXISTS state (
  id uuid PRIMARY KEY,
  success text,
  failure text,
  created timestamp NOT NULL DEFAULT NOW()
);
