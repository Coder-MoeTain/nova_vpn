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
    // Continue anyway - columns might already exist or table might not exist yet
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
      AND COLUMN_NAME IN ('banned', 'banned_at')
    `);
    const hasBannedColumn = columns.some(c => c.COLUMN_NAME === 'banned');
    const hasBannedAtColumn = columns.some(c => c.COLUMN_NAME === 'banned_at');
    
    // Build query based on available columns
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
      AND COLUMN_NAME IN ('banned', 'banned_at')
    `);
    const hasBannedColumn = columns.some(c => c.COLUMN_NAME === 'banned');
    const hasBannedAtColumn = columns.some(c => c.COLUMN_NAME === 'banned_at');
    
    // Build query based on available columns
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
      `INSERT INTO peers (public_key, client_ip, device_name_override, hostname, model, phone_number, latitude, longitude)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?)
       ON DUPLICATE KEY UPDATE
         client_ip = VALUES(client_ip),
         hostname = COALESCE(VALUES(hostname), hostname),
         model = COALESCE(VALUES(model), model),
         phone_number = COALESCE(VALUES(phone_number), phone_number),
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
  addLocationHistory,
  getLocationHistory,
  deletePeer,
  banPeer,
};
