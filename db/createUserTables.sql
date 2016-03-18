CREATE SCHEMA auth;

CREATE TABLE IF NOT EXISTS auth.facebook_users (
  id text PRIMARY KEY,
  ndla_id text,
  first_name text,
  middle_name text,
  last_name text,
  email text,
  created timestamp
);

CREATE TABLE IF NOT EXISTS auth.twitter_users (
  id text PRIMARY KEY,
  ndla_id text,
  name text,
  first_name text,
  middle_name text,
  last_name text,
  email text,
  created timestamp
);

CREATE TABLE IF NOT EXISTS auth.google_users (
  id text PRIMARY KEY,
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

CREATE TABLE IF NOT EXISTS auth.ndla_users (
  id uuid PRIMARY KEY,
  first_name text,
  middle_name text,
  last_name text,
  email text,
  created timestamp,
  facebook_id text,
  google_id text,
  twitter_id text
);

CREATE TABLE IF NOT EXISTS auth.state (
  id uuid PRIMARY KEY,
  success text,
  failure text
);
