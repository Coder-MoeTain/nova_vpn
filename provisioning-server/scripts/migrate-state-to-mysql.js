#!/usr/bin/env node
/**
 * Migrate existing state.json peers into MySQL.
 * Run once after enabling MySQL (MYSQL_* in .env).
 *
 * Usage (from provisioning-server directory):
 *   node scripts/migrate-state-to-mysql.js
 *
 * Or from repo root:
 *   node provisioning-server/scripts/migrate-state-to-mysql.js
 */

const path = require('path');
const fs = require('fs');

// Load .env (same logic as server.js)
const rootEnv = path.join(__dirname, '..', '..', '.env');
const localEnv = path.join(__dirname, '..', '.env');
if (fs.existsSync(rootEnv)) require('dotenv').config({ path: rootEnv });
else require('dotenv').config({ path: localEnv });

const db = require('../db');

const STATE_FILE = path.join(__dirname, '..', 'state.json');

async function migrate() {
  if (!db.isEnabled()) {
    console.error('MySQL is not configured. Set MYSQL_HOST, MYSQL_USER, MYSQL_PASSWORD, MYSQL_DATABASE in .env');
    process.exit(1);
  }

  let state = { nextClientIndex: 1, peers: [] };
  try {
    const data = fs.readFileSync(STATE_FILE, 'utf8');
    const obj = JSON.parse(data);
    if (typeof obj.nextClientIndex === 'number') state.nextClientIndex = obj.nextClientIndex;
    if (Array.isArray(obj.peers)) state.peers = obj.peers;
  } catch (err) {
    if (err.code === 'ENOENT') {
      console.log('No state.json found. Creating database tables only...');
      await db.ensureSchema();
      await db.setNextClientIndex(1);
      console.log('Tables created. next_client_index = 1. Ready for provisioning.');
      process.exit(0);
    }
    throw err;
  }

  if (state.peers.length === 0) {
    console.log('state.json has no peers. Setting next_client_index only.');
    await db.ensureSchema();
    await db.setNextClientIndex(state.nextClientIndex);
    console.log('Done. next_client_index =', state.nextClientIndex);
    process.exit(0);
  }

  await db.ensureSchema();

  for (const p of state.peers) {
    await db.upsertPeer({
      publicKey: p.publicKey,
      clientIp: p.clientIp,
      deviceNameOverride: p.deviceName || null,
      hostname: p.hostname || null,
      model: p.model || null,
      phoneNumber: p.phoneNumber || null,
      latitude: p.latitude ?? null,
      longitude: p.longitude ?? null,
    });
    if (p.latitude != null && p.longitude != null) {
      await db.addLocationHistory(p.publicKey, p.latitude, p.longitude);
    }
    console.log('  Migrated peer', (p.publicKey || '').slice(0, 12) + '...', p.clientIp);
  }

  await db.setNextClientIndex(state.nextClientIndex);
  console.log('Migrated', state.peers.length, 'peers. next_client_index =', state.nextClientIndex);
  console.log('You can keep or remove state.json; the server will use MySQL now.');
}

migrate().catch((err) => {
  console.error('Migration failed:', err.message);
  process.exit(1);
});
