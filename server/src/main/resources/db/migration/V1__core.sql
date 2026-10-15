CREATE TABLE users (
    uid           TEXT PRIMARY KEY,
    email         TEXT,
    display_name  TEXT,
    created_at    BIGINT NOT NULL
);

CREATE TABLE spaces (
    id           UUID PRIMARY KEY,
    owner_uid    TEXT NOT NULL REFERENCES users(uid) ON DELETE CASCADE,
    name         TEXT NOT NULL,
    is_personal  BOOLEAN NOT NULL,
    created_at   BIGINT NOT NULL
);
CREATE UNIQUE INDEX spaces_one_personal ON spaces(owner_uid) WHERE is_personal;

CREATE TABLE space_members (
    space_id   UUID NOT NULL REFERENCES spaces(id) ON DELETE CASCADE,
    uid        TEXT NOT NULL REFERENCES users(uid) ON DELETE CASCADE,
    role       TEXT NOT NULL CHECK (role IN ('owner', 'editor', 'viewer')),
    joined_at  BIGINT NOT NULL,
    PRIMARY KEY (space_id, uid)
);
CREATE INDEX space_members_uid ON space_members(uid);

CREATE TABLE invites (
    id          UUID PRIMARY KEY,
    space_id    UUID NOT NULL REFERENCES spaces(id) ON DELETE CASCADE,
    token_hash  TEXT NOT NULL UNIQUE,
    role        TEXT NOT NULL CHECK (role IN ('editor', 'viewer')),
    created_by  TEXT NOT NULL REFERENCES users(uid) ON DELETE CASCADE,
    created_at  BIGINT NOT NULL,
    expires_at  BIGINT NOT NULL,
    used_by     TEXT REFERENCES users(uid) ON DELETE SET NULL,
    used_at     BIGINT
);

CREATE TABLE files (
    id           UUID PRIMARY KEY,
    space_id     UUID NOT NULL REFERENCES spaces(id) ON DELETE CASCADE,
    uploaded_by  TEXT REFERENCES users(uid) ON DELETE SET NULL,
    path         TEXT NOT NULL,
    mime         TEXT NOT NULL,
    size         BIGINT NOT NULL,
    created_at   BIGINT NOT NULL
);
CREATE INDEX files_space ON files(space_id);
