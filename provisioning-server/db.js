/**
 * MySQL persistence for peers and location history.
 * Enable by setting MYSQL_HOST, MYSQL_USER, MYSQL_PASSWORD, MYSQL_DATABASE in .env
 */

const mysql = require('mysql2/promise');

let pool = null;

function isEnabled() {
  return !!(process.env.MYSQL_HOST && process.env.MYSQL_USER && process.env.MYSQL_PASSWORD && process.env.MYSQL_DATABASE);
}

async function getPool() {
  if (!pool) {
    pool = mysql.createPool({
      host: process.env.MYSQL_HOST || 'localhost',
      port: parseInt(process.env.MYSQL_PORT || '3306', 10),
      user: process.env.MYSQL_USER,
      password: process.env.MYSQL_PASSWORD,
      database: process.env.MYSQL_DATABASE,
      waitForConnections: true,
      connectionLimit: 10,
      queueLimit: 0,
    });
  }
  return pool;
}

async function initSchema(conn) {
  await conn.query(`
    CREATE TABLE IF NOT EXISTS peers (
      id INT AUTO_INCREMENT PRIMARY KEY,
      public_key VARCHAR(64) NOT NULL UNIQUE,
      client_ip VARCHAR(45) NOT NULL,
      device_name_override VARCHAR(128) NULL,
      hostname VARCHAR(64) NULL,
      model VARCHAR(64) NULL,
      phone_number VARCHAR(32) NULL,
      latitude DOUBLE NULL,
      longitude DOUBLE NULL,
      banned BOOLEAN NOT NULL DEFAULT FALSE,
      banned_at DATETIME NULL,
      created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
      INDEX idx_public_key (public_key),
      INDEX idx_client_ip (client_ip),
      INDEX idx_banned (banned)
    )
  `);
  // Add banned columns if they don't exist (for existing databases)
  // MySQL doesn't support IF NOT EXISTS for ALTER TABLE, so we check first
  try {
    const [columns] = await conn.query(`
      SELECT COLUMN_NAME 
      FROM INFORMATION_SCHEMA.COLUMNS 
      WHERE TABLE_SCHEMA = DATABASE() 
      AND TABLE_NAME = 'peers' 
      AND COLUMN_NAME IN ('banned', 'banned_at')
    `);
    const existingColumns = columns.map(c => c.COLUMN_NAME);
    
    if (!existingColumns.includes('banned')) {
      await conn.query(`ALTER TABLE peers ADD COLUMN banned BOOLEAN NOT NULL DEFAULT FALSE`);
      // Add index after adding column
      await conn.query(`ALTER TABLE peers ADD INDEX idx_banned (banned)`).catch(() => {});
    }
    if (!existingColumns.includes('banned_at')) {
      await conn.query(`ALTER TABLE peers ADD COLUMN banned_at DATETIME NULL`);
    }
  } catch (err) {
    console.error('Error adding banned columns:', err.message);
  }
  try {
    const [cols] = await conn.query(`
      SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'peers'
      AND COLUMN_NAME IN ('device_id', 'app_version')
    `);
    const has = cols.map(c => c.COLUMN_NAME);
    if (!has.includes('device_id')) await conn.query(`ALTER TABLE peers ADD COLUMN device_id VARCHAR(64) NULL`);
    if (!has.includes('app_version')) await conn.query(`ALTER TABLE peers ADD COLUMN app_version VARCHAR(64) NULL`);
  } catch (err) {
    console.error('Error adding device_id/app_version columns:', err.message);
  }
  await conn.query(`
    CREATE TABLE IF NOT EXISTS location_history (
      id INT AUTO_INCREMENT PRIMARY KEY,
      peer_public_key VARCHAR(64) NOT NULL,
      latitude DOUBLE NOT NULL,
      longitude DOUBLE NOT NULL,
      created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
      INDEX idx_peer (peer_public_key),
      INDEX idx_created (created_at)
    )
  `);
  await conn.query(`
    CREATE TABLE IF NOT EXISTS config (
      \`key\` VARCHAR(64) PRIMARY KEY,
      value VARCHAR(255) NOT NULL
    )
  `);
}

async function ensureSchema() {
  if (!isEnabled()) return;
  const conn = await (await getPool()).getConnection();
  try {
    await initSchema(conn);
  } finally {
    conn.release();
  }
}

async function getNextClientIndex() {
  const conn = await (await getPool()).getConnection();
  try {
    const [rows] = await conn.query('SELECT value FROM config WHERE `key` = ?', ['next_client_index']);
    if (rows.length) return parseInt(rows[0].value, 10);
    const [maxRows] = await conn.query(
      "SELECT COALESCE(MAX(CAST(SUBSTRING_INDEX(client_ip, '.', -1) AS UNSIGNED)), 0) + 1 AS next FROM peers"
    );
    const next = maxRows[0]?.next ?? 1;
    await conn.query('INSERT INTO config (`key`, value) VALUES (?, ?) ON DUPLICATE KEY UPDATE value = VALUES(value)', ['next_client_index', String(next)]);
    return next;
  } finally {
    conn.release();
  }
}

async function setNextClientIndex(index) {
  const conn = await (await getPool()).getConnection();
  try {
    await conn.query('INSERT INTO config (`key`, value) VALUES (?, ?) ON DUPLICATE KEY UPDATE value = VALUES(value)', ['next_client_index', String(index)]);
  } finally {
    conn.release();
  }
}

async function getPeers() {
  const conn = await (await getPool()).getConnection();
  try {
    // Check if banned columns exist
    const [columns] = await conn.query(`
      SELECT COLUMN_NAME 
      FROM INFORMATION_SCHEMA.COLUMNS 
      WHERE TABLE_SCHEMA = DATABASE() 
      AND TABLE_NAME = 'peers' 
      AND COLUMN_NAME IN ('banned', 'banned_at', 'device_id', 'app_version')
    `);
    const colSet = new Set(columns.map(c => c.COLUMN_NAME));
    const hasBannedColumn = colSet.has('banned');
    const hasBannedAtColumn = colSet.has('banned_at');
    const hasDeviceIdColumn = colSet.has('device_id');
    const hasAppVersionColumn = colSet.has('app_version');
    
    const selectFields = [
      'public_key AS publicKey',
      'client_ip AS clientIp',
      'device_name_override AS deviceNameOverride',
      'hostname',
      'model',
      'phone_number AS phoneNumber',
      'latitude',
      'longitude',
      ...(hasBannedColumn ? ['banned'] : []),
      ...(hasBannedAtColumn ? ['banned_at AS bannedAt'] : []),
      ...(hasDeviceIdColumn ? ['device_id AS deviceId'] : []),
      ...(hasAppVersionColumn ? ['app_version AS appVersion'] : []),
      'created_at AS createdAt'
    ].join(', ');
    
    const [rows] = await conn.query(`SELECT ${selectFields} FROM peers ORDER BY id`);
    return rows.map(r => ({
      publicKey: r.publicKey,
      clientIp: r.clientIp,
      deviceName: null,
      deviceNameOverride: r.deviceNameOverride,
      hostname: r.hostname,
      model: r.model,
      phoneNumber: r.phoneNumber,
      latitude: r.latitude,
      longitude: r.longitude,
      banned: hasBannedColumn ? (r.banned === 1 || r.banned === true) : false,
      bannedAt: hasBannedAtColumn && r.bannedAt ? new Date(r.bannedAt).toISOString() : null,
      deviceId: hasDeviceIdColumn ? r.deviceId : null,
      appVersion: hasAppVersionColumn ? r.appVersion : null,
      createdAt: r.createdAt ? new Date(r.createdAt).toISOString() : null,
    }));
  } finally {
    conn.release();
  }
}

async function getPeerByPublicKey(publicKey) {
  const conn = await (await getPool()).getConnection();
  try {
    // Check if banned columns exist
    const [columns] = await conn.query(`
      SELECT COLUMN_NAME 
      FROM INFORMATION_SCHEMA.COLUMNS 
      WHERE TABLE_SCHEMA = DATABASE() 
      AND TABLE_NAME = 'peers' 
      AND COLUMN_NAME IN ('banned', 'banned_at', 'device_id', 'app_version')
    `);
    const colSet = new Set(columns.map(c => c.COLUMN_NAME));
    const hasBannedColumn = colSet.has('banned');
    const hasBannedAtColumn = colSet.has('banned_at');
    const hasDeviceIdColumn = colSet.has('device_id');
    const hasAppVersionColumn = colSet.has('app_version');
    
    const selectFields = [
      'public_key AS publicKey',
      'client_ip AS clientIp',
      'device_name_override AS deviceNameOverride',
      'hostname',
      'model',
      'phone_number AS phoneNumber',
      'latitude',
      'longitude',
      ...(colSet.has('banned') ? ['banned'] : []),
      ...(colSet.has('banned_at') ? ['banned_at AS bannedAt'] : []),
      ...(hasDeviceIdColumn ? ['device_id AS deviceId'] : []),
      ...(hasAppVersionColumn ? ['app_version AS appVersion'] : []),
      'created_at AS createdAt'
    ].join(', ');
    
    const [rows] = await conn.query(`SELECT ${selectFields} FROM peers WHERE public_key = ?`, [publicKey]);
    if (!rows.length) return null;
    const r = rows[0];
    return {
      publicKey: r.publicKey,
      clientIp: r.clientIp,
      deviceName: null,
      deviceNameOverride: r.deviceNameOverride,
      hostname: r.hostname,
      model: r.model,
      phoneNumber: r.phoneNumber,
      latitude: r.latitude,
      longitude: r.longitude,
      banned: hasBannedColumn ? (r.banned === 1 || r.banned === true) : false,
      bannedAt: hasBannedAtColumn && r.bannedAt ? new Date(r.bannedAt).toISOString() : null,
      deviceId: hasDeviceIdColumn ? r.deviceId : null,
      appVersion: hasAppVersionColumn ? r.appVersion : null,
      createdAt: r.createdAt ? new Date(r.createdAt).toISOString() : null,
    };
  } finally {
    conn.release();
  }
}

async function upsertPeer(peer) {
  const conn = await (await getPool()).getConnection();
  try {
    await conn.query(
      `INSERT INTO peers (public_key, client_ip, device_name_override, hostname, model, phone_number, device_id, app_version, latitude, longitude)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
       ON DUPLICATE KEY UPDATE
         client_ip = VALUES(client_ip),
         hostname = COALESCE(VALUES(hostname), hostname),
         model = COALESCE(VALUES(model), model),
         phone_number = COALESCE(VALUES(phone_number), phone_number),
         device_id = COALESCE(VALUES(device_id), device_id),
         app_version = COALESCE(VALUES(app_version), app_version),
         latitude = COALESCE(VALUES(latitude), latitude),
         longitude = COALESCE(VALUES(longitude), longitude),
         updated_at = CURRENT_TIMESTAMP`,
      [
        peer.publicKey,
        peer.clientIp,
        peer.deviceNameOverride || null,
        peer.hostname || null,
        peer.model || null,
        peer.phoneNumber || null,
        peer.deviceId ?? null,
        peer.appVersion ?? null,
        peer.latitude ?? null,
        peer.longitude ?? null,
      ]
    );
  } finally {
    conn.release();
  }
}

async function updateDeviceName(publicKey, deviceName) {
  const conn = await (await getPool()).getConnection();
  try {
    const [r] = await conn.query('UPDATE peers SET device_name_override = ? WHERE public_key = ?', [deviceName || null, publicKey]);
    return r.affectedRows > 0;
  } finally {
    conn.release();
  }
}

async function updatePeerDeviceInfo(publicKey, { deviceId, appVersion }) {
  const conn = await (await getPool()).getConnection();
  try {
    const updates = [];
    const values = [];
    if (deviceId !== undefined) { updates.push('device_id = ?'); values.push(deviceId || null); }
    if (appVersion !== undefined) { updates.push('app_version = ?'); values.push(appVersion || null); }
    if (updates.length === 0) return true;
    values.push(publicKey);
    const [r] = await conn.query(`UPDATE peers SET ${updates.join(', ')}, updated_at = CURRENT_TIMESTAMP WHERE public_key = ?`, values);
    return r.affectedRows > 0;
  } finally {
    conn.release();
  }
}

async function addLocationHistory(peerPublicKey, latitude, longitude) {
  const conn = await (await getPool()).getConnection();
  try {
    await conn.query('INSERT INTO location_history (peer_public_key, latitude, longitude) VALUES (?, ?, ?)', [peerPublicKey, latitude, longitude]);
  } finally {
    conn.release();
  }
}

async function getLocationHistory(peerPublicKey, limit = 100) {
  const conn = await (await getPool()).getConnection();
  try {
    const [rows] = await conn.query(
      'SELECT id, latitude, longitude, created_at AS createdAt FROM location_history WHERE peer_public_key = ? ORDER BY created_at DESC LIMIT ?',
      [peerPublicKey, Math.min(Number(limit) || 100, 500)]
    );
    return rows.map(r => ({
      id: r.id,
      latitude: r.latitude,
      longitude: r.longitude,
      createdAt: r.createdAt ? new Date(r.createdAt).toISOString() : null,
    }));
  } finally {
    conn.release();
  }
}

async function deletePeer(publicKey) {
  const conn = await (await getPool()).getConnection();
  try {
    await conn.query('DELETE FROM location_history WHERE peer_public_key = ?', [publicKey]);
    const [r] = await conn.query('DELETE FROM peers WHERE public_key = ?', [publicKey]);
    return r.affectedRows > 0;
  } finally {
    conn.release();
  }
}

async function banPeer(publicKey) {
  const conn = await (await getPool()).getConnection();
  try {
    const [r] = await conn.query(
      'UPDATE peers SET banned = TRUE, banned_at = NOW() WHERE public_key = ?',
      [publicKey]
    );
    return r.affectedRows > 0;
  } finally {
    conn.release();
  }
}

module.exports = {
  isEnabled,
  ensureSchema,
  getPool,
  getNextClientIndex,
  setNextClientIndex,
  getPeers,
  getPeerByPublicKey,
  upsertPeer,
  updateDeviceName,
  updatePeerDeviceInfo,
  addLocationHistory,
  getLocationHistory,
  deletePeer,
  banPeer,
};
